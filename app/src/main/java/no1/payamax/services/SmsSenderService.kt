package no1.payamax.services

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import no1.payamax.utils.SmsAddressUtils
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object SmsSenderService {

    private const val TAG = "SmsSenderService"
    private const val SEND_TIMEOUT_MS = 30_000L
    private val requestCounter = AtomicInteger(1000)

    fun sendSms(
        context: Context,
        destinationAddress: String,
        body: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        val normalizedDest = SmsAddressUtils.normalizeSmsDestination(destinationAddress)
        if (!SmsAddressUtils.canReplyToAddress(normalizedDest)) {
            onResult(false, "Invalid destination address")
            return
        }
        if (body.isBlank()) {
            onResult(false, "Empty message")
            return
        }

        val appContext = context.applicationContext
        val smsManager = getSmsManager(appContext)
        if (smsManager == null) {
            onResult(false, "SmsManager unavailable")
            return
        }

        try {
            val parts = smsManager.divideMessage(body)
            val totalParts = parts.size.coerceAtLeast(1)
            val requestId = requestCounter.incrementAndGet()
            val action = "${appContext.packageName}.SMS_SENT_$requestId"

            val completed = AtomicBoolean(false)
            val remainingParts = AtomicInteger(totalParts)
            val anyFailed = AtomicBoolean(false)
            val mainHandler = Handler(Looper.getMainLooper())

            lateinit var receiver: BroadcastReceiver
            val timeoutRunnable = Runnable {
                if (completed.compareAndSet(false, true)) {
                    try {
                        appContext.unregisterReceiver(receiver)
                    } catch (_: Exception) {
                    }
                    // On some OEMs the sent broadcast may not arrive even after sending.
                    // If no part reported failure, treat timeout as sent only when modem accepted it,
                    // otherwise report failure if any error occurred.
                    onResult(!anyFailed.get(), if (anyFailed.get()) "SMS send failed" else null)
                }
            }

            receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (resultCode != Activity.RESULT_OK) {
                        anyFailed.set(true)
                        Log.w(TAG, "SMS part failed with resultCode=$resultCode")
                    }
                    if (remainingParts.decrementAndGet() <= 0) {
                        if (completed.compareAndSet(false, true)) {
                            mainHandler.removeCallbacks(timeoutRunnable)
                            try {
                                appContext.unregisterReceiver(this)
                            } catch (_: Exception) {
                            }
                            val failed = anyFailed.get()
                            if (!failed && no1.payamax.utils.DefaultSmsHelper.isDefaultSmsApp(appContext)) {
                                persistSentSms(appContext, normalizedDest, body)
                            }
                            onResult(!failed, if (failed) "Failed to send SMS (code $resultCode)" else null)
                        }
                    }
                }
            }

            ContextCompat.registerReceiver(
                appContext,
                receiver,
                IntentFilter(action),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            mainHandler.postDelayed(timeoutRunnable, SEND_TIMEOUT_MS)

            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>(parts.size)
                for (index in parts.indices) {
                    val sentIntent = PendingIntent.getBroadcast(
                        appContext,
                        requestId * 100 + index,
                        Intent(action).setPackage(appContext.packageName),
                        pendingIntentFlags
                    )
                    sentIntents.add(sentIntent)
                }
                smsManager.sendMultipartTextMessage(
                    normalizedDest,
                    null,
                    parts,
                    sentIntents,
                    null
                )
            } else {
                val sentIntent = PendingIntent.getBroadcast(
                    appContext,
                    requestId,
                    Intent(action).setPackage(appContext.packageName),
                    pendingIntentFlags
                )
                smsManager.sendTextMessage(
                    normalizedDest,
                    null,
                    body,
                    sentIntent,
                    null
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing SEND_SMS permission", e)
            onResult(false, e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS to $normalizedDest", e)
            onResult(false, e.message)
        }
    }

    private fun persistSentSms(context: Context, destination: String, body: String) {
        try {
            val values = android.content.ContentValues().apply {
                put(android.provider.Telephony.Sms.ADDRESS, destination)
                put(android.provider.Telephony.Sms.BODY, body)
                put(android.provider.Telephony.Sms.DATE, System.currentTimeMillis())
                put(android.provider.Telephony.Sms.READ, 1)
                put(android.provider.Telephony.Sms.SEEN, 1)
                put(android.provider.Telephony.Sms.TYPE, android.provider.Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            context.contentResolver.insert(android.provider.Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist sent SMS to Sent provider", e)
        }
    }

    @Suppress("DEPRECATION")
    private fun getSmsManager(context: Context): SmsManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
    }
}
