package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.utils.process

@Composable
fun MessageScreen(contentResolver: ContentResolver, navController: NavHostController, payamakId: Long) {
    Column {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            CheckPayamakPermission {
                val inboxUri = "content://sms/inbox"
                val uri = Uri.parse(inboxUri)
                (contentResolver.query(uri, null, "", null, "date desc")
                    ?: throw RuntimeException("")).use { cursor ->
                    val payamak: ReviewableProcessedPayamak = process(payamakId, cursor, PayamakColumns(cursor))
                    MessageDebugComposable(msgValue = payamak, onSelected = {}, onDesiredResultChanged = {})
                }
            }
        }
    }
}