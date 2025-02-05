package no1.payamax.composables

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EnsurePermissions(content: @Composable () -> Unit) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS)
    )

    for (ps in permissionState.permissions) {
        if (!ps.status.isGranted) {
            when (ps.permission) {
                android.Manifest.permission.READ_SMS -> ReadPayamakPermissionMessage(ps)
                android.Manifest.permission.READ_CONTACTS -> ReadContactsPermissionMessage(ps)
            }
            return
        }
    }
    content()
}