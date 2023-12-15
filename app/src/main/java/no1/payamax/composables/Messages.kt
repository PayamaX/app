package no1.payamax.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import no1.payamax.contracts.UsabilityClass
import no1.payamax.model.MessageModel
import no1.payamax.vm.MessagesViewModel

@Composable
fun MessagesComposable(viewModel: MessagesViewModel) {
    val messages by viewModel.messages.observeAsState()
    if (messages != null) {
        LazyColumn(modifier = Modifier.padding(5.dp)) {
            items(messages!!) { message ->
                MessageComposable(message = message)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MessagesComposablePreview() {
    MessagesComposable(
        viewModel = MessagesViewModel(
            MutableLiveData(
                listOf(
                    MessageModel(
                        0L,
                        "9123456789",
                        "Important Message",
                        "",
                        "",
                        UsabilityClass.Important
                    ),
                    MessageModel(
                        0L,
                        "9123456789",
                        "Usable Message",
                        "",
                        "",
                        UsabilityClass.Usable
                    ),
                    MessageModel(
                        0L,
                        "9123456789",
                        "Unknown Message",
                        "",
                        "",
                        UsabilityClass.Unknown
                    ),
                    MessageModel(
                        0L,
                        "9123456789",
                        "Spam Message",
                        "",
                        "",
                        UsabilityClass.Spam
                    ),
                )
            )
        )
    )
}