package no1.payamax.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no1.payamax.composables.getThreadKey
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.PayamakThread
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine

data class SmsDeleteResult(
    val deletedSmsCount: Int,
    val deletedThreadKeys: Set<String>,
    val deletedRawAddresses: Set<String>
)

object SmsDeleteHelper {

    private fun buildCandidateAddresses(thread: PayamakThread): Set<String> {
        val candidates = linkedSetOf<String>()
        val rawAddr = thread.address
        val title = thread.origin.title ?: ""
        val displayable = thread.origin.displayable()

        fun addVariants(input: String) {
            if (input.isBlank()) return
            candidates.add(input.trim().lowercase())
            val cleaned = SmsAddressUtils.cleanAddress(input)
            if (cleaned.isNotBlank()) {
                candidates.add(cleaned.lowercase())
                val normalized = SmsAddressUtils.normalizeSmsDestination(input)
                if (normalized.isNotBlank()) {
                    candidates.add(normalized.lowercase())
                }
                val digitsOnly = cleaned.filter { it.isDigit() }
                if (!cleaned.any { it.isLetter() }) {
                    if (digitsOnly.length >= 10) {
                        val last10 = digitsOnly.takeLast(10)
                        candidates.add(last10)
                        candidates.add("0$last10")
                        candidates.add("+98$last10")
                        candidates.add("98$last10")
                    } else if (digitsOnly.length >= 4) {
                        candidates.add(digitsOnly)
                        candidates.add("+98$digitsOnly")
                        candidates.add("98$digitsOnly")
                    }
                }
            }
        }

        addVariants(rawAddr)
        addVariants(title)
        if (thread.origin.contact == null) {
            addVariants(displayable)
        }
        thread.origin.number?.let { num ->
            addVariants(num.main.toString())
            if (num.prefix != null) {
                addVariants("+${num.prefix}${num.main}")
                addVariants("${num.prefix}${num.main}")
            }
        }
        return candidates
    }

    suspend fun deleteSelectedThreads(
        contentResolver: ContentResolver,
        threadsToDelete: List<PayamakThread>
    ): SmsDeleteResult = withContext(Dispatchers.IO) {
        if (threadsToDelete.isEmpty()) {
            return@withContext SmsDeleteResult(0, emptySet(), emptySet())
        }

        val allCandidates = HashSet<String>()
        val explicitIds = HashSet<Long>()
        val threadKeys = LinkedHashSet<String>()

        for (thread in threadsToDelete) {
            threadKeys.add(thread.key)
            explicitIds.add(thread.latestMessage.pp.id)
            allCandidates.addAll(buildCandidateAddresses(thread))
        }

        val smsUri = Uri.parse("content://sms")
        val idsToDelete = ArrayList<Long>()
        val matchedRawAddresses = LinkedHashSet<String>()

        try {
            contentResolver.query(
                smsUri,
                arrayOf("_id", "address"),
                null,
                null,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex("_id")
                val addrIdx = cursor.getColumnIndex("address")
                if (idIdx >= 0 && cursor.moveToFirst()) {
                    do {
                        val id = cursor.getLong(idIdx)
                        val rawAddr = if (addrIdx >= 0) (cursor.getString(addrIdx) ?: "") else ""
                        val rawLower = rawAddr.trim().lowercase()
                        val cleanedLower = SmsAddressUtils.cleanAddress(rawAddr).lowercase()

                        val isMatch = explicitIds.contains(id) ||
                            (rawLower.isNotEmpty() && allCandidates.contains(rawLower)) ||
                            (cleanedLower.isNotEmpty() && allCandidates.contains(cleanedLower))

                        if (isMatch) {
                            idsToDelete.add(id)
                            if (rawAddr.isNotEmpty()) {
                                matchedRawAddresses.add(rawAddr)
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }

            var totalDeleted = 0
            for (chunk in idsToDelete.chunked(200)) {
                val placeholders = chunk.joinToString(separator = ",") { "?" }
                val args = chunk.map { it.toString() }.toTypedArray()
                val deleted = contentResolver.delete(smsUri, "_id IN ($placeholders)", args)
                totalDeleted += deleted
            }

            SmsDeleteResult(
                deletedSmsCount = totalDeleted,
                deletedThreadKeys = threadKeys,
                deletedRawAddresses = matchedRawAddresses
            )
        } catch (e: Exception) {
            Log.e("SmsDeleteHelper", "Failed to delete selected SMS threads", e)
            SmsDeleteResult(0, emptySet(), emptySet())
        }
    }

    suspend fun deleteAllThreadsInCategory(
        contentResolver: ContentResolver,
        loadedThreads: List<PayamakThread>,
        targetTypes: List<PayamakUsabilityClass>,
        searchQuery: String,
        engine: UsabilityProcessorEngine
    ): SmsDeleteResult = withContext(Dispatchers.IO) {
        val knownThreadKeys = HashSet<String>()
        val seenScanRawAddresses = HashSet<String>()
        val threadsToDelete = ArrayList<PayamakThread>()

        fun matchesQuery(thread: PayamakThread): Boolean {
            if (searchQuery.isEmpty()) return true
            return thread.origin.displayable().contains(searchQuery, ignoreCase = true) ||
                thread.address.contains(searchQuery, ignoreCase = true) ||
                thread.latestMessage.pp.payamak.body.contains(searchQuery, ignoreCase = true)
        }

        for (thread in loadedThreads) {
            knownThreadKeys.add(thread.key)
            if (targetTypes.contains(thread.latestMessage.pp.activeUsabilityClass) && matchesQuery(thread)) {
                threadsToDelete.add(thread)
            }
        }

        // Also scan remaining inbox messages in descending date order so unloaded spam threads are included
        try {
            val inboxUri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "date", "address", "body")
            contentResolver.query(inboxUri, projection, null, null, "date desc")?.use { cursor ->
                val payamakColumns = PayamakColumns(cursor)
                if (cursor.moveToFirst()) {
                    do {
                        val addressValue = cursor.getString(payamakColumns.addressIndex) ?: ""
                        if (seenScanRawAddresses.add(addressValue)) {
                            val message = process(cursor, contentResolver, payamakColumns, engine)
                            val key = getThreadKey(message)
                            if (knownThreadKeys.add(key)) {
                                val origin = message.pp.payamak.origin
                                val address = origin.number?.let {
                                    if (it.prefix != null) "+${it.prefix}${it.main}" else it.main.toString()
                                } ?: (origin.title ?: origin.displayable())
                                val thread = PayamakThread(
                                    key = key,
                                    address = address,
                                    origin = origin,
                                    latestMessage = message
                                )
                                if (targetTypes.contains(message.pp.activeUsabilityClass) && matchesQuery(thread)) {
                                    threadsToDelete.add(thread)
                                }
                            }
                        }
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            Log.e("SmsDeleteHelper", "Error scanning inbox for deleteAllThreadsInCategory", e)
        }

        deleteSelectedThreads(contentResolver, threadsToDelete)
    }
}
