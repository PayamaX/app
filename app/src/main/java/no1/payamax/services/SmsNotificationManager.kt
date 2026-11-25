package no1.payamax.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no1.payamax.R
import no1.payamax.cleanCompose.core.MainActivity
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.utils.DefaultSmsHelper

data class IncomingSmsEvent(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val usabilityClass: PayamakUsabilityClass
)

object IncomingSmsEventBus {
    private val _events = MutableSharedFlow<IncomingSmsEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<IncomingSmsEvent> = _events.asSharedFlow()

    fun emit(event: IncomingSmsEvent) {
        _events.tryEmit(event)
    }
}

object SmsNotificationManager {

    private const val TAG = "SmsNotificationManager"
    const val CHANNEL_ID_INCOMING_SMS = "payamax_incoming_sms"
    const val EXTRA_NOTIFICATION_ADDRESS = "extra_notification_address"
    const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"

    /**
     * Determines whether an incoming SMS should trigger a user notification.
     * Only Important, Usable, and Unknown categories are notified; Spam is always silenced.
     */
    fun shouldNotify(usabilityClass: PayamakUsabilityClass): Boolean {
        return when (usabilityClass) {
            PayamakUsabilityClass.Important,
            PayamakUsabilityClass.Usable,
            PayamakUsabilityClass.Unknown -> true
            PayamakUsabilityClass.Spam -> false
        }
    }

    /**
     * Ensures the incoming SMS notification channel exists and respects system sound,
     * vibration, Do Not Disturb, and lock-screen privacy settings.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?: return

        val existing = manager.getNotificationChannel(CHANNEL_ID_INCOMING_SMS)
        if (existing != null) return

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID_INCOMING_SMS,
            context.getString(R.string.notification_channel_sms_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_sms_desc)
            enableVibration(true)
            setSound(defaultSoundUri, audioAttributes)
            setShowBadge(true)
            // Respect OS lock-screen privacy settings (hide sensitive content when locked if OS requires it)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        manager.createNotificationChannel(channel)
    }

    fun showIncomingSmsNotification(
        context: Context,
        address: String,
        displayName: String,
        body: String,
        timestamp: Long,
        usabilityClass: PayamakUsabilityClass
    ) {
        if (!DefaultSmsHelper.isDefaultSmsApp(context)) {
            Log.d(TAG, "Skipping notification because PayamaX is not the default SMS app")
            return
        }

        if (!shouldNotify(usabilityClass)) {
            Log.d(TAG, "Skipping notification for Spam message from '$address'")
            return
        }

        if (!no1.payamax.utils.NotificationPermissionHelper.hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission or channel disabled; cannot display SMS notification")
            return
        }

        ensureChannel(context)

        val notificationId = ((timestamp and 0x7FFFFFFFL).toInt()).let {
            if (it == 0) address.hashCode() else it
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NOTIFICATION_ADDRESS, address)
            putExtra(EXTRA_NOTIFICATION_TITLE, displayName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Redacted version shown automatically by Android on a locked screen when the user's
        // OS settings hide sensitive notification content.
        val publicRedactedNotification = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_SMS)
            .setSmallIcon(R.drawable.ic_notification_sms)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_new_message_hidden))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setWhen(timestamp)
            .setShowWhen(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_SMS)
            .setSmallIcon(R.drawable.ic_notification_sms)
            .setContentTitle(displayName.ifBlank { address })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicRedactedNotification)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException posting SMS notification", e)
        }
    }
}
