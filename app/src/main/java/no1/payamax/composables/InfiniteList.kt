package no1.payamax.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.PayamakThread
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.InstantProviderService
import no1.payamax.utils.UserPreferences
import org.koin.java.KoinJavaComponent.inject

@Composable
fun InfiniteList(
    listItems: List<ReviewableProcessedPayamak>,
    onLoadMore: () -> Unit,
    onSelected: (Boolean) -> Unit,
    onClicked: (Long) -> Unit,
    onDesiredResultChanged: () -> Unit,
    paddings: PaddingValues = PaddingValues(0.dp),
    isReportMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onItemLongClicked: ((Long) -> Unit)? = null,
    onItemSelectToggle: ((Long, Boolean) -> Unit)? = null,
    userOpinions: Map<Long, PayamakUsabilityClass> = emptyMap(),
    onUserOpinionChanged: ((Long, PayamakUsabilityClass) -> Unit)? = null,
    userComments: Map<Long, String> = emptyMap(),
    onUserCommentChanged: ((Long, String) -> Unit)? = null,
    isLoading: Boolean = false,
) {
    val listState = rememberLazyListState()
    val instantProvider = remember { InstantProviderService() }
    val userPreferences by inject<UserPreferences>(UserPreferences::class.java)
    val calendarType by userPreferences.calendarType.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddings),
    ) {
        items(
            items = listItems,
            key = { it.pp.id },
            contentType = { "payamak" }
        ) { msg ->
            val msgId = msg.pp.id
            MessageComposable(
                msgValue = msg,
                instantProvider = instantProvider,
                onSelected = { isChecked ->
                    onItemSelectToggle?.invoke(msgId, isChecked) ?: onSelected(isChecked)
                },
                onClicked = onClicked,
                onDesiredResultChanged = onDesiredResultChanged,
                isReportMode = isReportMode,
                isSelected = selectedIds.contains(msgId),
                onLongClicked = { onItemLongClicked?.invoke(msgId) },
                userOpinion = userOpinions[msgId],
                onUserOpinionChanged = { newOpinion ->
                    onUserOpinionChanged?.invoke(msgId, newOpinion)
                },
                userComment = userComments[msgId] ?: "",
                onUserCommentChanged = { newComment ->
                    onUserCommentChanged?.invoke(msgId, newComment)
                },
                calendarType = calendarType
            )
        }

        if (isLoading) {
            item(key = "loading_indicator_footer", contentType = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
    }

    InfiniteListHandler(
        listState = listState,
        buffer = 8,
        isLoading = isLoading
    ) {
        onLoadMore()
    }
}

@Composable
fun ThreadInfiniteList(
    threads: List<PayamakThread>,
    onLoadMore: () -> Unit,
    onThreadClicked: (PayamakThread) -> Unit,
    paddings: PaddingValues = PaddingValues(0.dp),
    isLoading: Boolean = false,
    isReportMode: Boolean = false,
    selectedKeys: Set<String> = emptySet(),
    onThreadLongClicked: ((PayamakThread) -> Unit)? = null,
    onThreadSelectToggle: ((String, Boolean) -> Unit)? = null,
    userOpinions: Map<Long, PayamakUsabilityClass> = emptyMap(),
    onUserOpinionChanged: ((Long, PayamakUsabilityClass) -> Unit)? = null,
    userComments: Map<Long, String> = emptyMap(),
    onUserCommentChanged: ((Long, String) -> Unit)? = null,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddings),
    ) {
        items(
            items = threads,
            key = { it.key },
            contentType = { "thread" }
        ) { thread ->
            val msgId = thread.latestMessage.pp.id
            ThreadComposable(
                thread = thread,
                onThreadClicked = onThreadClicked,
                isReportMode = isReportMode,
                isSelected = selectedKeys.contains(thread.key),
                onThreadLongClicked = onThreadLongClicked,
                onSelectedToggle = { isChecked ->
                    onThreadSelectToggle?.invoke(thread.key, isChecked)
                },
                userOpinion = userOpinions[msgId],
                onUserOpinionChanged = { opinion ->
                    onUserOpinionChanged?.invoke(msgId, opinion)
                },
                userComment = userComments[msgId] ?: "",
                onUserCommentChanged = { comment ->
                    onUserCommentChanged?.invoke(msgId, comment)
                }
            )
        }

        if (isLoading) {
            item(key = "loading_indicator_footer", contentType = "loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
    }

    InfiniteListHandler(
        listState = listState,
        buffer = 5,
        isLoading = isLoading
    ) {
        onLoadMore()
    }
}

/**
 * Handler to make any lazy column (or lazy row) infinite. Will notify the [onLoadMore]
 * callback once needed
 * @param listState state of the list that needs to also be passed to the LazyColumn composable.
 * Get it by calling rememberLazyListState()
 * @param buffer the number of items before the end of the list to call the onLoadMore callback
 * @param isLoading whether loading is currently in progress
 * @param onLoadMore will notify when we need to load more
 */
@Composable
fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 4,
    isLoading: Boolean = false,
    onLoadMore: () -> Unit
) {
    val shouldLoadMore = remember(listState, buffer) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            if (totalItemsNumber == 0) return@derivedStateOf false
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex >= (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect { isNearEnd ->
                if (isNearEnd && !isLoading) {
                    onLoadMore()
                }
            }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading && shouldLoadMore.value) {
            onLoadMore()
        }
    }
}
