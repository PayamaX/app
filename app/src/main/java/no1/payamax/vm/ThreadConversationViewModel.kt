package no1.payamax.vm

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no1.payamax.contracts.Contact
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityRate
import no1.payamax.contracts.cellNumber
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.model.SmsSendStatus
import no1.payamax.services.PayamakColumns
import no1.payamax.services.SmsSenderService
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.ContactCache
import no1.payamax.utils.SmsAddressUtils
import no1.payamax.utils.processFast
import java.util.concurrent.atomic.AtomicLong

class ThreadConversationViewModel : ViewModel() {

    val messages = mutableStateListOf<ReviewableProcessedPayamak>()

    var isLoading by mutableStateOf(true)
        private set

    var isPageLoading by mutableStateOf(false)
        private set

    var cachedContact by mutableStateOf<Contact?>(null)
        private set

    var canReply by mutableStateOf(false)
        private set

    private var loadedAddress: String? = null
    private var currentCursorOffset = 0
    private val pageSize = 25
    var hasMore by mutableStateOf(true)
        private set

    private val tempIdGenerator = AtomicLong(-1L)
    private val smsProjection = arrayOf("_id", "date", "address", "body", "type")
    private val conversationSmsTypes = "type IN (${Payamak.MESSAGE_TYPE_INBOX}, ${Payamak.MESSAGE_TYPE_SENT}, ${Payamak.MESSAGE_TYPE_OUTBOX}, ${Payamak.MESSAGE_TYPE_FAILED}, ${Payamak.MESSAGE_TYPE_QUEUED})"

    private fun buildSelection(address: String, title: String): Pair<String, Array<String>> {
        val cleaned = SmsAddressUtils.cleanAddress(address)
        val normalized = SmsAddressUtils.normalizeSmsDestination(address)
        val digitsOnly = cleaned.filter { it.isDigit() }
        val cleanNum = if (digitsOnly.length >= 10) digitsOnly.takeLast(10) else digitsOnly

        val candidates = linkedSetOf<String>()
        if (address.isNotBlank()) candidates.add(address)
        if (cleaned.isNotBlank()) candidates.add(cleaned)
        if (normalized.isNotBlank()) candidates.add(normalized)

        if (cleanNum.length >= 10) {
            candidates.add(cleanNum)
            candidates.add("0$cleanNum")
            candidates.add("+98$cleanNum")
            candidates.add("98$cleanNum")
        }

        if (candidates.isEmpty() && title.isNotBlank()) {
            candidates.add(title)
        }

        val addressClause = candidates.joinToString(separator = " OR ") { "address = ?" }
        val selection = "$conversationSmsTypes AND ($addressClause)"
        return selection to candidates.toTypedArray()
    }

    fun loadThread(
        contentResolver: ContentResolver,
        address: String,
        title: String,
        engine: UsabilityProcessorEngine
    ) {
        if (loadedAddress == address && (messages.isNotEmpty() || !isLoading)) {
            return
        }

        loadedAddress = address
        canReply = SmsAddressUtils.canReplyToAddress(address)
        isLoading = true
        isPageLoading = true
        messages.clear()
        currentCursorOffset = 0
        hasMore = true

        viewModelScope.launch(Dispatchers.IO) {
            var contactInfo: Contact? = null
            try {
                val cleaned = SmsAddressUtils.cleanAddress(address)
                val digitsOnly = cleaned.filter { it.isDigit() }
                val searchNum = if (digitsOnly.length >= 4) digitsOnly else address
                contactInfo = ContactCache.getContact(searchNum, contentResolver)

                val smsUri = Uri.parse("content://sms")
                val (selection, selectionArgs) = buildSelection(address, title)

                val initialList = mutableListOf<ReviewableProcessedPayamak>()
                contentResolver.query(
                    smsUri,
                    smsProjection,
                    selection,
                    selectionArgs,
                    "date desc"
                )?.use { cursor ->
                    val payamakColumns = PayamakColumns(cursor)
                    if (cursor.moveToFirst()) {
                        var count = 0
                        do {
                            val msg = processFast(cursor, payamakColumns, engine, contactInfo)
                            initialList.add(msg)
                            count++
                        } while (cursor.moveToNext() && count < pageSize)
                    }
                }

                Log.d("ThreadConv", "loadThread finished for address='$address', loaded=${initialList.size}, canReply=$canReply")

                withContext(Dispatchers.Main) {
                    cachedContact = contactInfo
                    messages.addAll(initialList)
                    currentCursorOffset = initialList.size
                    if (initialList.size < pageSize) {
                        hasMore = false
                    }
                }
            } catch (e: Exception) {
                Log.e("ThreadConv", "Error loading thread for address='$address'", e)
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    isPageLoading = false
                }
            }
        }
    }

    fun loadNextPage(
        contentResolver: ContentResolver,
        address: String,
        title: String,
        engine: UsabilityProcessorEngine
    ) {
        if (!hasMore || isPageLoading || isLoading) return
        isPageLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (selection, selectionArgs) = buildSelection(address, title)
                val smsUri = Uri.parse("content://sms")
                val pageList = mutableListOf<ReviewableProcessedPayamak>()

                contentResolver.query(
                    smsUri,
                    smsProjection,
                    selection,
                    selectionArgs,
                    "date desc"
                )?.use { cursor ->
                    val payamakColumns = PayamakColumns(cursor)
                    if (cursor.moveToPosition(currentCursorOffset)) {
                        var count = 0
                        do {
                            val msg = processFast(cursor, payamakColumns, engine, cachedContact)
                            pageList.add(msg)
                            count++
                        } while (cursor.moveToNext() && count < pageSize)
                    }
                }

                Log.d("ThreadConv", "loadNextPage loaded=${pageList.size}, offset=$currentCursorOffset")

                withContext(Dispatchers.Main) {
                    if (pageList.isEmpty()) {
                        hasMore = false
                    } else {
                        val existingIds = messages.map { it.pp.id }.toHashSet()
                        val uniqueItems = pageList.filter { !existingIds.contains(it.pp.id) }
                        messages.addAll(uniqueItems)
                        currentCursorOffset += pageList.size
                        if (pageList.size < pageSize) {
                            hasMore = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ThreadConv", "Error loading next page", e)
                hasMore = false
            } finally {
                withContext(Dispatchers.Main) {
                    isPageLoading = false
                }
            }
        }
    }

    fun sendMessage(
        context: Context,
        address: String,
        body: String,
        onSendFailed: (String?) -> Unit = {}
    ) {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || !SmsAddressUtils.canReplyToAddress(address)) return

        val tempId = tempIdGenerator.getAndDecrement()
        val normalizedDest = SmsAddressUtils.normalizeSmsDestination(address)
        val digitsOnly = normalizedDest.filter { it.isDigit() }
        val cellNum = digitsOnly.toLongOrNull()?.cellNumber()
        val origin = Origin(
            number = cellNum,
            title = if (cellNum == null) normalizedDest else null,
            contact = cachedContact
        )
        val outgoingPayamak = Payamak(
            id = tempId,
            received = System.currentTimeMillis(),
            origin = origin,
            body = trimmed,
            type = Payamak.MESSAGE_TYPE_OUTBOX
        )
        val optimisticMessage = ReviewableProcessedPayamak(
            pp = ProcessedPayamakModel(
                id = tempId,
                payamak = outgoingPayamak,
                usability = Usability(PayamakUsabilityClass.Usable, UsabilityRate(1.0), emptyList())
            ),
            selected = false,
            sendStatus = SmsSendStatus.SENDING
        )

        messages.add(0, optimisticMessage)
        currentCursorOffset += 1

        dispatchSms(context, normalizedDest, tempId, trimmed, onSendFailed)
    }

    fun retryMessage(
        context: Context,
        address: String,
        failedMessage: ReviewableProcessedPayamak,
        onSendFailed: (String?) -> Unit = {}
    ) {
        val normalizedDest = SmsAddressUtils.normalizeSmsDestination(address)
        if (!SmsAddressUtils.canReplyToAddress(normalizedDest)) return

        val msgId = failedMessage.pp.id
        updateMessageSendStatus(msgId, Payamak.MESSAGE_TYPE_OUTBOX, SmsSendStatus.SENDING)
        dispatchSms(context, normalizedDest, msgId, failedMessage.pp.payamak.body, onSendFailed)
    }

    private fun dispatchSms(
        context: Context,
        destination: String,
        messageId: Long,
        body: String,
        onSendFailed: (String?) -> Unit
    ) {
        SmsSenderService.sendSms(
            context = context,
            destinationAddress = destination,
            body = body
        ) { success, error ->
            viewModelScope.launch(Dispatchers.Main) {
                if (success) {
                    updateMessageSendStatus(messageId, Payamak.MESSAGE_TYPE_SENT, SmsSendStatus.SENT)
                } else {
                    updateMessageSendStatus(messageId, Payamak.MESSAGE_TYPE_FAILED, SmsSendStatus.FAILED)
                    onSendFailed(error)
                }
            }
        }
    }

    private fun updateMessageSendStatus(messageId: Long, newType: Int, newStatus: SmsSendStatus) {
        val index = messages.indexOfFirst { it.pp.id == messageId }
        if (index != -1) {
            val current = messages[index]
            messages[index] = current.copy(
                pp = current.pp.copy(
                    payamak = current.pp.payamak.copy(type = newType)
                ),
                sendStatus = newStatus
            )
        }
    }

    fun refreshMessage(updatedMsg: ReviewableProcessedPayamak) {
        val index = messages.indexOfFirst { it.pp.id == updatedMsg.pp.id }
        if (index != -1) {
            messages[index] = updatedMsg
        }
    }

    fun reloadThread(
        contentResolver: ContentResolver,
        address: String,
        title: String,
        engine: UsabilityProcessorEngine
    ) {
        loadedAddress = null
        loadThread(contentResolver, address, title, engine)
    }
}
