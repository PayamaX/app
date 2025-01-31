package no1.payamax.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadContactsPermissionMessage(ps: PermissionState) {
    Column {
        val textToShow = if (ps.status.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "Reading contacts permission is required"
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "Reading contacts permission is required to detect if sender already saved in your contacts or not"
        }
        Text(textToShow)
        Button(onClick = { ps.launchPermissionRequest() }) {
            Text("Request for contacts permission")
        }
    }
}