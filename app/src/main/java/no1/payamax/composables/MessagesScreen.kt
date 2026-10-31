package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import no1.payamax.contracts.PayamakUsabilityClass

@Composable
fun MessagesScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>
) {
    EnsurePermissions {
        val inboxUri = "content://sms/inbox"
        val uri = Uri.parse(inboxUri)
        val cursor = contentResolver.query(uri, null, null, null, "date desc")
        if (cursor != null) {
            MainTabScreen(cursor, contentResolver, navController, types)
        }
    }
}

