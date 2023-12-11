package no1.payamax.composables

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import no1.payamax.vm.MessagesViewModel

@Composable
fun MessagesComposable(viewModel: MessagesViewModel) {
    val messages by viewModel.messages.observeAsState()
    if (messages != null) {
        LazyColumn {
            items(messages!!) { message ->
                MessageComposable(message = message)
            }
        }
    }
}