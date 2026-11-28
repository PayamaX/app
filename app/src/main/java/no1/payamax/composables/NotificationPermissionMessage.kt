package no1.payamax.composables

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no1.payamax.R
import no1.payamax.utils.NotificationPermissionHelper

@Composable
fun NotificationPermissionMessage(
    onPermissionResult: () -> Unit,
    onSkipForSession: () -> Unit
) {
    val context = LocalContext.current
    var requestAttemptCount by remember { mutableStateOf(0) }
    var autoLaunched by remember { mutableStateOf(false) }

    val runtimeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        requestAttemptCount += 1
        onPermissionResult()
    }

    val canUseRuntimeDialog = remember(requestAttemptCount) {
        NotificationPermissionHelper.canRequestRuntimePermission(
            context = context,
            alreadyRequestedOnce = requestAttemptCount > 0
        )
    }

    // On Android 13+, immediately trigger the system permission dialog once when opening the app
    LaunchedEffect(Unit) {
        if (NotificationPermissionHelper.requiresRuntimePermission() && !autoLaunched) {
            autoLaunched = true
            runtimeLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                val textToShow = if (requestAttemptCount > 0 || !NotificationPermissionHelper.requiresRuntimePermission()) {
                    stringResource(R.string.notification_permission_denied)
                } else {
                    stringResource(R.string.notification_permission_comment)
                }

                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (canUseRuntimeDialog) {
                            runtimeLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            NotificationPermissionHelper.openNotificationSettings(context)
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (canUseRuntimeDialog) {
                            stringResource(R.string.notification_permission_command)
                        } else {
                            stringResource(R.string.notification_permission_open_settings)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onSkipForSession
                ) {
                    Text(
                        text = stringResource(R.string.notification_permission_later),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
