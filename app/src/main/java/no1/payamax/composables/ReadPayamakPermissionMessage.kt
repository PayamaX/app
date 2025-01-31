package no1.payamax.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import no1.payamax.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReadPayamakPermissionMessage(ps: PermissionState) {
    Column {
        val textToShow = if (ps.status.shouldShowRationale) {
            stringResource(R.string.sms_permission_denied)
        } else {
            stringResource(R.string.sms_permission_comment)
        }
        Text(textToShow)
        Button(onClick = { ps.launchPermissionRequest() }) {
            Text(stringResource(R.string.sms_permission_command))
        }
    }
}