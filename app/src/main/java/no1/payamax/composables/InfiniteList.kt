package no1.payamax.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.InstantProviderService

@Composable
fun InfiniteList(
    listItems: List<ReviewableProcessedPayamak>,
    onLoadMore: () -> Unit,
    onSelected: (Boolean) -> Unit,
    onClicked: (Long) -> Unit,
    onDesiredResultChanged: () -> Unit,
    paddings: PaddingValues = PaddingValues(0.dp),
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddings),
    ) {
        items(listItems) { msg ->
            MessageComposable(
                msg,
                InstantProviderService(),
                onSelected,
                onClicked,
                onDesiredResultChanged
            )
        }
    }

    InfiniteListHandler(listState = listState) {
        onLoadMore()
    }
}

/**
 * Handler to make any lazy column (or lazy row) infinite. Will notify the [onLoadMore]
 * callback once needed
 * @param listState state of the list that needs to also be passed to the LazyColumn composable.
 * Get it by calling rememberLazyListState()
 * @param buffer the number of items before the end of the list to call the onLoadMore callback
 * @param onLoadMore will notify when we need to load more
 */
@Composable
fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 10,
    onLoadMore: () -> Unit
) {
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .distinctUntilChanged()
            .collect {
                onLoadMore()
            }
    }
}