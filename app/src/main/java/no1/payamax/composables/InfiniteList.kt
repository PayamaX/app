package no1.payamax.composables

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.InstantProviderService

@Composable
fun InfiniteList(
    listItems: List<ReviewableProcessedPayamak>,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState
    ) {
        items(listItems) { msg ->
            MessageComposable(msg, InstantProviderService(), {}, {}, {})
        }
    }

    InfiniteListHandler(listState = listState) {
        onLoadMore()
    }
}