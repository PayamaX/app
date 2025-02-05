package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.utils.process


@Composable
fun FilteredPayamaksComposable(cursor: Cursor, contentResolver: ContentResolver, navController: NavHostController, types: List<PayamakUsabilityClass>) {

    val messages = mutableListOf<ReviewableProcessedPayamak>()

    val payamakColumns = PayamakColumns(cursor)
    InfiniteList(messages) {
        var index = 0
        do {
            val message = process(
                cursor,
                contentResolver,
                payamakColumns
            )
            if (types.contains(message.pp.usability.clazz)) {
                messages.add(message)
                index++
            }
        } while (cursor.moveToNext() && index < 20)
    }
}