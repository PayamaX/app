package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import no1.payamax.contracts.PayamakUsabilityClass
import androidx.core.net.toUri

@Composable
fun MessagesScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>
) {
    EnsurePermissions {
        val inboxUri = "content://sms/inbox"
        val uri = inboxUri.toUri()
        val projection = arrayOf("_id", "date", "address", "body")
        val cursor = contentResolver.query(uri, projection, null, null, "date desc")
        if (cursor != null) {
            MainTabScreen(cursor, contentResolver, navController, types)
        }
    }
}

