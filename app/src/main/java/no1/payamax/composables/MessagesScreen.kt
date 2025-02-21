package no1.payamax.composables

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.contracts.PayamakUsabilityClass

@Composable
fun MessagesScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>
) {
    Column {
        PayamaxTheme {
            EnsurePermissions {
                val inboxUri = "content://sms/inbox"
                val uri = Uri.parse(inboxUri)
                val cursor = contentResolver.query(uri, null, "", null, "date desc")
                    ?: throw RuntimeException("")
                if (cursor.moveToFirst()) {
                    FilteredPayamaksComposable(cursor, contentResolver, navController, types)
                } else {
                    Text("EMPTY")
                }
            }
        }
    }
}


private fun packageInfo(context: Context): String {
    val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION") pInfo.versionCode
    }
    return "${pInfo.packageName}:${versionCode}:${pInfo.versionName}"
}