package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import no1.payamax.R
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.process
import org.koin.java.KoinJavaComponent.inject

@Composable
fun MessageScreen(contentResolver: ContentResolver, navController: NavHostController, payamakId: Long) {
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)

    PayamaxScaffold(
        title = stringResource(R.string.message_details_title),
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.nav_back),
        onNavigationIconClick = { navController.navigateUp() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
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
