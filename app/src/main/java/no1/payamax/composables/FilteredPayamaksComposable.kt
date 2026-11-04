package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.process
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilteredPayamaksComposable(
    cursor: Cursor,
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>,
    modifier: Modifier = Modifier
) {
    val messages = remember { mutableStateListOf<ReviewableProcessedPayamak>() }
    var lastIndex = 0
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
    val payamakColumns = PayamakColumns(cursor)

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Tab categories
    var selectedCategoryIndex by remember {
        mutableIntStateOf(
            if (types.contains(PayamakUsabilityClass.Spam)) 2 else 0
        )
    }

    val activeTypes = when (selectedCategoryIndex) {
        0 -> listOf(
            PayamakUsabilityClass.Important,
            PayamakUsabilityClass.Usable,
            PayamakUsabilityClass.Spam,
            PayamakUsabilityClass.Unknown
        )
        1 -> listOf(PayamakUsabilityClass.Important, PayamakUsabilityClass.Usable)
        2 -> listOf(PayamakUsabilityClass.Spam, PayamakUsabilityClass.Unknown)
        else -> listOf(
            PayamakUsabilityClass.Important,
            PayamakUsabilityClass.Usable,
            PayamakUsabilityClass.Spam,
            PayamakUsabilityClass.Unknown
        )
    }

    DisposableEffect(navController.currentBackStackEntry) {
        for (i in messages.indices) {
            val oldMsg = messages[i]
            val updatedUsability = engine.detect(oldMsg.pp.payamak)
            if (updatedUsability != oldMsg.pp.usability || oldMsg.pp.expectedPayamakUsabilityClass != null) {
                messages[i] = oldMsg.copy(
                    pp = oldMsg.pp.copy(
                        usability = updatedUsability,
                        expectedPayamakUsabilityClass = null
                    )
                )
            }
        }
        onDispose { }
    }

    fun loadNextBatch() {
        if (!cursor.moveToFirst()) return
        var currentBatchIndex = 0
        var addedSize = 0
        do {
            if (currentBatchIndex++ < lastIndex) {
                continue
            }
            val message = process(
                cursor,
                contentResolver,
                payamakColumns,
                engine
            )
            messages.add(message)
            if (activeTypes.contains(message.pp.activeUsabilityClass)) {
                addedSize++
            }
        } while (cursor.moveToNext() && addedSize < 20)
        lastIndex = currentBatchIndex
    }

    val spacing = MaterialTheme.spacing

    LaunchedEffect(selectedCategoryIndex) {
        while (messages.none { activeTypes.contains(it.pp.activeUsabilityClass) } && lastIndex < cursor.count) {
            val prevLastIndex = lastIndex
            loadNextBatch()
            if (lastIndex == prevLastIndex) break
        }
    }

    PayamaxScaffold(
        title = stringResource(R.string.app_title_full),
        modifier = modifier,
        topBarHeader = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.app_title_full),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = stringResource(R.string.nav_search)
                            )
                        }
                    }
                )

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.nav_clear))
                                }
                            }
                        }
                    )
                }

                // Filter Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium, vertical = spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    FilterChip(
                        selected = selectedCategoryIndex == 0,
                        onClick = { selectedCategoryIndex = 0 },
                        label = { Text(stringResource(R.string.category_all)) },
                        shape = RoundedCornerShape(999.dp)
                    )
                    FilterChip(
                        selected = selectedCategoryIndex == 1,
                        onClick = { selectedCategoryIndex = 1 },
                        label = { Text(stringResource(R.string.category_useful)) },
                        shape = RoundedCornerShape(999.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = selectedCategoryIndex == 2,
                        onClick = { selectedCategoryIndex = 2 },
                        label = { Text(stringResource(R.string.category_spam)) },
                        shape = RoundedCornerShape(999.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        val filteredMessages = messages.filter { msg ->
            activeTypes.contains(msg.pp.activeUsabilityClass) &&
                    (searchQuery.isEmpty() ||
                            msg.pp.payamak.body.contains(searchQuery, ignoreCase = true) ||
                            msg.pp.payamak.origin.displayable().contains(searchQuery, ignoreCase = true))
        }

        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.padding(spacing.extraLarge)
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(spacing.medium))
                        Text(
                            text = stringResource(R.string.empty_messages),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            InfiniteList(
                filteredMessages,
                {
                    loadNextBatch()
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
}
