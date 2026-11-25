package no1.payamax.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.cellNumber
import no1.payamax.utils.ContactCache
import org.koin.java.KoinJavaComponent

/**
 * Broadcast receiver handling incoming SMS when PayamaX is the default SMS app.
 * Persists incoming messages to the Android SMS Inbox, runs usability classification,
 * and shows a system notification only for Important, Usable, and Unknown messages (never Spam).
 */
class SmsDeliverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isNullOrEmpty()) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Group multipart SMS PDUs by sender address in arrival order
                data class AssembledSms(
                    val address: String,
                    val bodyBuilder: StringBuilder,
                    var timestamp: Long,
                    val serviceCenter: String?
                )

                val grouped = LinkedHashMap<String, AssembledSms>()
                for (sms in smsMessages) {
                    val sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: continue
                    val bodyPart = sms.messageBody ?: ""
                    val timestamp = sms.timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
                    val existing = grouped[sender]
                    if (existing == null) {
                        grouped[sender] = AssembledSms(
                            address = sender,
                            bodyBuilder = StringBuilder(bodyPart),
                            timestamp = timestamp,
                            serviceCenter = sms.serviceCenterAddress
                        )
                    } else {
                        existing.bodyBuilder.append(bodyPart)
                        if (timestamp > existing.timestamp) {
                            existing.timestamp = timestamp
                        }
                    }
                }

                val rankedOriginProcessor = try {
                    KoinJavaComponent.get<RankedOriginProcessor>(RankedOriginProcessor::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to resolve RankedOriginProcessor", e)
                    null
                }
                try {
                    rankedOriginProcessor?.ensureLoaded()
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to refresh ranked origins cache", e)
                }

                val engine = try {
                    KoinJavaComponent.get<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Unable to resolve UsabilityProcessorEngine", e)
                    null
                }

                for (assembled in grouped.values) {
                    val address = assembled.address
                    val fullBody = assembled.bodyBuilder.toString()
                    val timestamp = assembled.timestamp

                    // 1. Persist to Android Telephony Inbox (mandatory responsibility of default SMS app)
                    val insertedId = insertIncomingSmsToInbox(
                        context = appContext,
                        address = address,
                        body = fullBody,
                        timestamp = timestamp,
                        serviceCenter = assembled.serviceCenter
                    )

                    // 2. Classify message with PayamaX usability engine
                    val cellNum = address.toLongOrNull()?.cellNumber()
                    val addressTitle = if (cellNum == null) address else null
                    val contactInfo = if (address.isNotBlank()) {
                        ContactCache.getContact(address, appContext.contentResolver)
                    } else {
                        null
                    }
                    val origin = Origin(cellNum, addressTitle, contactInfo)
                    val payamak = Payamak(
                        id = insertedId,
                        received = timestamp,
                        origin = origin,
                        body = fullBody,
                        type = Payamak.MESSAGE_TYPE_INBOX
                    )

                    val usabilityClass = try {
                        engine?.detect(payamak)?.clazz ?: PayamakUsabilityClass.Unknown
                    } catch (e: Exception) {
                        Log.e(TAG, "Error running usability detection on incoming SMS", e)
                        PayamakUsabilityClass.Unknown
                    }

                    Log.d(
                        TAG,
                        "Incoming SMS from '$address' classified as $usabilityClass (notify=${SmsNotificationManager.shouldNotify(usabilityClass)})"
                    )

                    // 3. Notify in-app observers (e.g. open conversation screen)
                    IncomingSmsEventBus.emit(
                        IncomingSmsEvent(
                            id = insertedId,
                            address = address,
                            body = fullBody,
                            timestamp = timestamp,
                            usabilityClass = usabilityClass
                        )
                    )

                    // 4. Show OS notification (only for Important / Usable / Unknown; Spam is silenced)
                    SmsNotificationManager.showIncomingSmsNotification(
                        context = appContext,
                        address = address,
                        displayName = origin.displayable(),
                        body = fullBody,
                        timestamp = timestamp,
                        usabilityClass = usabilityClass
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process delivered SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun insertIncomingSmsToInbox(
        context: Context,
        address: String,
        body: String,
        timestamp: Long,
        serviceCenter: String?
    ): Long {
        return try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, timestamp)
                put(Telephony.Sms.DATE_SENT, timestamp)
                put(Telephony.Sms.READ, 0)
                put(Telephony.Sms.SEEN, 0)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                if (!serviceCenter.isNullOrBlank()) {
                    put(Telephony.Sms.SERVICE_CENTER, serviceCenter)
                }
            }
            val uri = context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull() ?: timestamp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert incoming SMS into Inbox provider", e)
            timestamp
        }
    }

    companion object {
        private const val TAG = "SmsDeliverReceiver"
    }
}

/**
 * Required broadcast receiver to handle incoming WAP PUSH / MMS when set as the default SMS app.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) return
        // MMS push placeholder
    }
}

/**
 * Required headless SMS service to respond to quick-response actions from external apps/wearables.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
