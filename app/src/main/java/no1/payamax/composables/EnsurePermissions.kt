package no1.payamax.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import no1.payamax.utils.NotificationPermissionHelper
import no1.payamax.utils.NotificationPermissionSession

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnsurePermissions(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionState = rememberMultiplePermissionsState(
        listOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS)
    )

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationPermissionHelper.hasNotificationPermission(context))
    }
    var skippedNotificationForSession by remember {
        mutableStateOf(NotificationPermissionSession.skippedForSession)
    }

    // Re-check notification permission whenever returning to the app (e.g. from OS Settings)
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = NotificationPermissionHelper.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    for (ps in permissionState.permissions) {
        if (!ps.status.isGranted) {
            when (ps.permission) {
                android.Manifest.permission.READ_SMS -> ReadPayamakPermissionMessage(ps)
                android.Manifest.permission.READ_CONTACTS -> ReadContactsPermissionMessage(ps)
            }
            return
        }
    }

    if (!hasNotificationPermission && !skippedNotificationForSession) {
        NotificationPermissionMessage(
            onPermissionResult = {
                hasNotificationPermission = NotificationPermissionHelper.hasNotificationPermission(context)
            },
            onSkipForSession = {
                NotificationPermissionSession.skippedForSession = true
                skippedNotificationForSession = true
            }
        )
        return
    }

    content()
}
