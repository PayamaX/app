package no1.payamax.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import no1.payamax.services.SmsNotificationManager

object NotificationPermissionSession {
    var skippedForSession: Boolean = false
}

object NotificationPermissionHelper {

    /**
     * Checks whether the app is allowed to show SMS notifications across all Android versions
     * from Android 6.0 (API 23) through the latest Android releases.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val runtimeGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!runtimeGranted) return false
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = manager?.getNotificationChannel(SmsNotificationManager.CHANNEL_ID_INCOMING_SMS)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }

        return true
    }

    /**
     * Returns true if the OS supports the runtime POST_NOTIFICATIONS permission dialog (Android 13+).
     */
    fun requiresRuntimePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Returns true when the runtime permission dialog can still be shown to the user.
     */
    fun canRequestRuntimePermission(context: Context, alreadyRequestedOnce: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        val activity = context.findActivity() ?: return !alreadyRequestedOnce
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        )
        return !alreadyRequestedOnce || shouldShowRationale
    }

    /**
     * Opens the appropriate system notification settings screen for Android 6.0 (API 23) and above.
     */
    fun openNotificationSettings(context: Context) {
        val intents = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val channel = manager?.getNotificationChannel(SmsNotificationManager.CHANNEL_ID_INCOMING_SMS)
                val appEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

                if (appEnabled && channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                    add(
                        Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            putExtra(Settings.EXTRA_CHANNEL_ID, SmsNotificationManager.CHANNEL_ID_INCOMING_SMS)
                        }
                    )
                }

                add(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            } else {
                // Android 6.0 - 7.1 (API 23 - 25)
                add(
                    Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                        putExtra("app_package", context.packageName)
                        putExtra("app_uid", context.applicationInfo.uid)
                    }
                )
            }

            // Universal fallback to App Details settings
            add(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            )
        }

        for (intent in intents) {
            try {
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next fallback intent
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
