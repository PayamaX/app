package no1.payamax.vm

import android.content.ContentResolver
import android.net.Uri
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
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.ContactCache
import no1.payamax.utils.processFast

class ThreadConversationViewModel : ViewModel() {

    val messages = mutableStateListOf<ReviewableProcessedPayamak>()

    var isLoading by mutableStateOf(true)
        private set

    var cachedContact by mutableStateOf<Contact?>(null)
        private set

    private var loadedAddress: String? = null
    private var currentCursorOffset = 0
    private val pageSize = 10
    var hasMore by mutableStateOf(true)
        private set

    private var isPageLoading = false

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
        isLoading = true
        isPageLoading = true
        messages.clear()
        currentCursorOffset = 0
        hasMore = true

        viewModelScope.launch(Dispatchers.IO) {
            var contactInfo: Contact? = null
            try {
                val digitsOnly = address.filter { it.isDigit() }
                val searchNum = if (digitsOnly.length >= 4) digitsOnly else address
                contactInfo = ContactCache.getContact(searchNum, contentResolver)

                val inboxUri = "content://sms/inbox"
                val uri = Uri.parse(inboxUri)
                val cleanNum = if (digitsOnly.length >= 10) digitsOnly.takeLast(10) else digitsOnly

                val selection: String
                val selectionArgs: Array<String>

                if (cleanNum.length >= 10) {
                    selection = "address = ? OR address = ? OR address = ? OR address = ?"
                    selectionArgs = arrayOf(
                        address,
                        "0$cleanNum",
                        "+98$cleanNum",
                        "98$cleanNum"
                    )
                } else if (address.isNotEmpty()) {
                    selection = "address = ?"
                    selectionArgs = arrayOf(address)
                } else {
                    selection = "address = ?"
                    selectionArgs = arrayOf(title)
                }

                val initialList = mutableListOf<ReviewableProcessedPayamak>()
                contentResolver.query(
                    uri,
                    null,
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

                withContext(Dispatchers.Main) {
                    cachedContact = contactInfo
                    messages.addAll(initialList)
                    currentCursorOffset = initialList.size
                    if (initialList.size < pageSize) {
                        hasMore = false
                    }
                }
            } catch (e: Exception) {
                // Ignore query errors
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
                val digitsOnly = address.filter { it.isDigit() }
                val cleanNum = if (digitsOnly.length >= 10) digitsOnly.takeLast(10) else digitsOnly

                val selection: String
                val selectionArgs: Array<String>

                if (cleanNum.length >= 10) {
                    selection = "address = ? OR address = ? OR address = ? OR address = ?"
                    selectionArgs = arrayOf(
                        address,
                        "0$cleanNum",
                        "+98$cleanNum",
                        "98$cleanNum"
                    )
                } else if (address.isNotEmpty()) {
                    selection = "address = ?"
                    selectionArgs = arrayOf(address)
                } else {
                    selection = "address = ?"
                    selectionArgs = arrayOf(title)
                }

                val inboxUri = "content://sms/inbox"
                val uri = Uri.parse(inboxUri)
                val pageList = mutableListOf<ReviewableProcessedPayamak>()

                contentResolver.query(
                    uri,
                    null,
                    selection,
                    selectionArgs,
                    "date desc"
                )?.use { cursor ->
                    val payamakColumns = PayamakColumns(cursor)
                    if (cursor.moveToFirst()) {
                        var skipped = 0
                        var count = 0
                        do {
                            if (skipped < currentCursorOffset) {
                                skipped++
                                continue
                            }
                            val msg = processFast(cursor, payamakColumns, engine, cachedContact)
                            pageList.add(msg)
                            count++
                        } while (cursor.moveToNext() && count < pageSize)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (pageList.isEmpty()) {
                        hasMore = false
                    } else {
                        messages.addAll(pageList)
                        currentCursorOffset += pageList.size
                        if (pageList.size < pageSize) {
                            hasMore = false
                        }
                    }
                }
            } catch (e: Exception) {
                hasMore = false
            } finally {
                withContext(Dispatchers.Main) {
                    isPageLoading = false
                }
            }
        }
    }

    fun refreshMessage(updatedMsg: ReviewableProcessedPayamak) {
        val index = messages.indexOfFirst { it.pp.id == updatedMsg.pp.id }
        if (index != -1) {
            messages[index] = updatedMsg
        }
    }
}
