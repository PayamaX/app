package no1.payamax.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass
import no1.payamax.contracts.UsabilityRate
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.vm.MessagesViewModel

@Composable
fun MessagesComposable(viewModel: MessagesViewModel) {
    //val messages by viewModel.messages.observeAsState()
    val messages = remember {
        mutableStateListOf(
            ProcessedPayamakModel(
                0L, Payamak(
                    0L, Origin(
                        CellNumber(
                            98, 9123430412L
                        ), null, null
                    ), "Sample"
                ), Usability(
                    UsabilityClass.Usable, UsabilityRate(0.9), emptyList()
                ), null
            )
        )
    }
    //if (messages != null) {
    LazyColumn(modifier = Modifier.padding(5.dp)) {
        items(messages) { message ->
            MessageComposable(msgValue = message) { x, y ->
                x.expectedUsabilityClass = y
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
    //}
}


@Preview(showBackground = true)
@Composable
fun MessagesComposablePreview() {
    MessagesComposable(
        viewModel = MessagesViewModel(
            MutableLiveData(
                mutableListOf(
                )
            )
        )
    )
}