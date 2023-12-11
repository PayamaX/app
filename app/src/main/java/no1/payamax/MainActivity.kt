package no1.payamax

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import no1.payamax.composables.MessagesComposable
import no1.payamax.model.MessageModel
import no1.payamax.ui.theme.PayamaXTheme
import no1.payamax.vm.MessagesViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayamaXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        CheckPayamakPermission {
                            val inboxUri = "content://sms/inbox"
                            val uri = Uri.parse(inboxUri)
                            val projection =
                                arrayOf("_id", "address", "person", "body", "date", "type")
                            (contentResolver.query(
                                uri,
                                projection,
                                "",
                                null,
                                "date desc"
                            ) ?: throw RuntimeException("")).use { cursor ->
                                val messages = mutableListOf<MessageModel>()
                                var index = 0
                                if (cursor.moveToFirst()) {
                                    val idIndex = 0
                                    val addressIndex = 1
                                    val bodyIndex = 3
                                    val typeIndex = 5
                                    do {
                                        messages.add(
                                            MessageModel(
                                                cursor.getLong(idIndex),
                                                cursor.getString(addressIndex),
                                                cursor.getString(bodyIndex),
                                                "",
                                                cursor.getString(typeIndex),
                                            )
                                        )
                                    } while (cursor.moveToNext() && index++ <= 10)
                                }
                                MessagesComposable(
                                    viewModel = MessagesViewModel(
                                        MutableLiveData(
                                            messages
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPayamakPermission(content: @Composable () -> Unit) {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.READ_SMS
    )

    if (permissionState.status.isGranted) {
        content()
    } else {
        Column {
            val textToShow = if (permissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "مجوز دریافت پیامک‌ها لازم است"
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "برای بررسی و تشخیص هرزپیامک‌ها، دسترسی به پیامک‌ها لازم است. "
            }
            Text(textToShow)
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("درخواست محوز دریافت پیامک‌ها")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PayamaXTheme {
        Greeting("Android")
    }
}