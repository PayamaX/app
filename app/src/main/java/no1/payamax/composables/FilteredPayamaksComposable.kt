package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessagesRoute
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.utils.process

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredPayamaksComposable(
    cursor: Cursor,
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>
) {

    val messages = remember { mutableStateListOf<ReviewableProcessedPayamak>() }
    var lastIndex = 0
    val payamakColumns = PayamakColumns(cursor)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(
                            MessagesRoute(
                                listOf(PayamakUsabilityClass.Spam, PayamakUsabilityClass.Unknown)
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.spams)
                        )
                    }
                },
            )
        },
        bottomBar = {
        }
    ) { padding ->
        InfiniteList(
            messages,
            {
                var currentBatchIndex = 0
                var index = 0
                var addedSize = 0
                do {
                    if (currentBatchIndex++ < lastIndex) {
                        continue
                    }
                    Log.i("LOAD", "processing message: ${currentBatchIndex + index++}")
                    val message = process(
                        cursor,
                        contentResolver,
                        payamakColumns
                    )
                    if (types.contains(message.pp.usability.clazz)) {
                        messages.add(message)
                        addedSize++
                    }
                } while (cursor.moveToNext() && addedSize < 20)
                lastIndex = currentBatchIndex
            },
            {},
            { id ->
                navController.navigate(
                    MessageRoute(id)
                )
            },
            {},
            padding
        )
    }
}