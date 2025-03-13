package no1.payamax.cleanCompose.feutureMessagesDetails.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import no1.payamax.cleanCompose.core.common.components.PayamaxTopAppBar
import no1.payamax.cleanCompose.core.common.components.PayamaxTopAppBarData
import no1.payamax.cleanCompose.core.presentation.base.BaseRoute
import no1.payamax.composables.EnsurePermissions
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.process
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailsScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    payamakId: Long,
    navDestination: BaseRoute?,
    onEvent: (MessageDetailsEvents) -> Unit,
    uiState: MessageDetailsUIStates
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)

    Scaffold (
         topBar = {
             PayamaxTopAppBar(
                 scrollBehavior = scrollBehavior,
                 payamaxTopAppBarData = PayamaxTopAppBarData(
                     title = "جزئیات پیام",
                     showBackIcon = true,
                     onBackClick = { navController.popBackStack() })
             )
         },
         modifier = Modifier.fillMaxSize()
     ) { padding ->
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(padding)
         ) {
             EnsurePermissions {
                 val inboxUri = "content://sms/inbox"
                 val uri = Uri.parse(inboxUri)
                 (contentResolver.query(uri, null, "_id = $payamakId", null, "date desc")
                     ?: throw RuntimeException("")).use { cursor ->
                     if (cursor.moveToFirst()) {
                         val payamak: ReviewableProcessedPayamak =
                             process(cursor, contentResolver, PayamakColumns(cursor), engine)

                         MessageDebugComposable(
                             msgValue = payamak,
                             onSelected = {},
                             onDesiredResultChanged = {},
                             onEvent = onEvent,
                         )
                     }
                 }
             }
         }
     }
}