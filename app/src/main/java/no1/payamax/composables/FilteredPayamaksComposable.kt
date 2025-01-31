package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import no1.payamax.BuildConfig
import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityRate
import no1.payamax.model.ProcessResult
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.InstantProviderService
import no1.payamax.services.PayamakColumns
import no1.payamax.utils.process
import no1.payamax.vm.MessagesViewModel


@Composable
fun FilteredPayamaksComposable(cursor: Cursor, contentResolver: ContentResolver, navController: NavHostController, types: List<PayamakUsabilityClass>) {

    val messages = mutableListOf<ReviewableProcessedPayamak>()
    var index = 0

    if (cursor.moveToFirst()) {
        val payamakColumns = PayamakColumns(cursor)
        do {
            messages.add(
                process(
                    cursor,
                    contentResolver,
                    payamakColumns
                )
            )
        } while (cursor.moveToNext() && index++ < 99)
    }
    if (BuildConfig.DEBUG && messages.isEmpty()) {
        messages.add(
            ReviewableProcessedPayamak(
                ProcessedPayamakModel(
                    0,
                    Payamak(
                        0,
                        0, Origin(
                            CellNumber(
                                98, 9123456789
                            ),
                            null,
                            null,
                        ), "Sample"
                    ),
                    Usability(
                        PayamakUsabilityClass.Usable,
                        UsabilityRate(0.9),
                        listOf(ProcessResult("Sample", false, null, ""))
                    ),
                    PayamakUsabilityClass.Usable,
                ), false
            )
        )
    }
    if (messages.isEmpty()) {
        Text(text = "Empty")
    } else {
        MessagesComposable(
            viewModel = MessagesViewModel(
                messages.filter { types.contains(it.pp.usability.clazz) }
            ),
            InstantProviderService(),
            navController
        )
    }
}