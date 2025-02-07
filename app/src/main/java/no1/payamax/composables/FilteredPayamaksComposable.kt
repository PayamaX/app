package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import no1.payamax.constants.Screen
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.utils.process


@Composable
fun FilteredPayamaksComposable(cursor: Cursor, contentResolver: ContentResolver, navController: NavHostController, types: List<PayamakUsabilityClass>) {

    val messages = remember { mutableStateListOf<ReviewableProcessedPayamak>() }
    var lastIndex = 0

    val payamakColumns = PayamakColumns(cursor)

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
        { id -> navController.navigate(Screen.MessageScreen.genUrl(id)) },
        {}
    )
}