package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.process
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

@Composable
fun MessageScreen(contentResolver: ContentResolver, navController: NavHostController, payamakId: Long) {
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            EnsurePermissions {
                val inboxUri = "content://sms/inbox"
                val uri = Uri.parse(inboxUri)
                (contentResolver.query(uri, null, "_id = $payamakId", null, "date desc")
                    ?: throw RuntimeException("")).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val payamak: ReviewableProcessedPayamak = process(cursor, contentResolver, PayamakColumns(cursor), engine)
                        MessageDebugComposable(msgValue = payamak, onSelected = {}, onDesiredResultChanged = {})
                    }
                }
            }
        }
    }
}