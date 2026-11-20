package no1.payamax.composables

import android.content.ContentResolver
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.modules.report.PayamakReportContract
import no1.payamax.modules.report.PayamakReportSubmissionItem
import no1.payamax.modules.report.ReportSubmissionResult
import no1.payamax.modules.report.UsableInfoSharingOption
import no1.payamax.services.InstantProviderService
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.UserPreferences
import no1.payamax.vm.ThreadConversationViewModel
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadConversationScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    address: String,
    title: String
) {
    val engine = remember { KoinJavaComponent.get<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java) }
    val viewModel = remember(address) {
        KoinJavaComponent.get<ThreadConversationViewModel>(ThreadConversationViewModel::class.java)
    }
    val reportService = remember { KoinJavaComponent.get<PayamakReportContract>(PayamakReportContract::class.java) }
    val userPreferences = remember { KoinJavaComponent.get<UserPreferences>(UserPreferences::class.java) }
    val calendarType by userPreferences.calendarType.collectAsState()
    val instantProvider = remember { InstantProviderService() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.spacing
    val listState = rememberLazyListState()

    val reportSuccessMessage = stringResource(R.string.report_success_toast)
    val reportSavedOfflineMessage = stringResource(R.string.report_saved_offline_toast)
    val reportNoSelectionMessage = stringResource(R.string.report_no_selection_toast)

    // Report mode state
    var isReportMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateMapOf<Long, Boolean>() }
    val userOpinions = remember { mutableStateMapOf<Long, PayamakUsabilityClass>() }
    val userComments = remember { mutableStateMapOf<Long, String>() }

    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var allowAllSpamInfo by remember { mutableStateOf(true) }
    var usableSharingOption by remember { mutableStateOf(UsableInfoSharingOption.PARTIAL) }
    var isSubmittingReport by remember { mutableStateOf(false) }

    BackHandler(enabled = isReportMode) {
        isReportMode = false
        selectedItemIds.clear()
    }

    LaunchedEffect(address, title) {
        viewModel.loadThread(contentResolver, address, title, engine)
    }

    val cachedContact = viewModel.cachedContact
    val firstOrigin = viewModel.messages.firstOrNull()?.pp?.payamak?.origin
    val titleText = cachedContact?.name ?: (if (title.isNotBlank()) title else firstOrigin?.displayable() ?: address)
    val photoUri = cachedContact?.photoUri ?: firstOrigin?.contact?.photoUri

    if (showReportConfirmDialog) {
        val currentLayoutDirection = LocalLayoutDirection.current
        val currentContext = LocalContext.current
        val currentConfiguration = LocalConfiguration.current

        AlertDialog(
            onDismissRequest = {
                if (!isSubmittingReport) showReportConfirmDialog = false
            },
            title = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentLayoutDirection,
                    LocalContext provides currentContext,
                    LocalConfiguration provides currentConfiguration
                ) {
                    Text(
                        text = stringResource(R.string.report_dialog_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentLayoutDirection,
                    LocalContext provides currentContext,
                    LocalConfiguration provides currentConfiguration
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        Text(
                            text = stringResource(R.string.report_dialog_desc, selectedItemIds.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Spam consent
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = allowAllSpamInfo,
                                    onCheckedChange = { allowAllSpamInfo = it },
                                    enabled = !isSubmittingReport
                                )
                                Spacer(modifier = Modifier.width(spacing.extraSmall))
                                Column {
                                    Text(
                                        text = stringResource(R.string.report_dialog_spam_consent),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.report_dialog_spam_consent_desc),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Usable consent
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(spacing.small)) {
                                Text(
                                    text = stringResource(R.string.report_dialog_usable_consent),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = spacing.extraSmall, vertical = spacing.extraSmall)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isSubmittingReport) {
                                            usableSharingOption = UsableInfoSharingOption.PARTIAL
                                        }
                                ) {
                                    RadioButton(
                                        selected = usableSharingOption == UsableInfoSharingOption.PARTIAL,
                                        onClick = { usableSharingOption = UsableInfoSharingOption.PARTIAL },
                                        enabled = !isSubmittingReport
                                    )
                                    Text(
                                        text = stringResource(R.string.report_dialog_usable_partial),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !isSubmittingReport) {
                                            usableSharingOption = UsableInfoSharingOption.FULL
                                        }
                                ) {
                                    RadioButton(
                                        selected = usableSharingOption == UsableInfoSharingOption.FULL,
                                        onClick = { usableSharingOption = UsableInfoSharingOption.FULL },
                                        enabled = !isSubmittingReport
                                    )
                                    Text(
                                        text = stringResource(R.string.report_dialog_usable_full),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (isSubmittingReport) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = spacing.small),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(spacing.medium))
                                Text(
                                    text = stringResource(R.string.report_dialog_submitting),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentLayoutDirection,
                    LocalContext provides currentContext,
                    LocalConfiguration provides currentConfiguration
                ) {
                    Button(
                        onClick = {
                            isSubmittingReport = true
                            scope.launch {
                                val selectedMessages = viewModel.messages.filter { selectedItemIds.containsKey(it.pp.id) }
                                val itemsToSubmit = selectedMessages.map { msg ->
                                    PayamakReportSubmissionItem(
                                        message = msg,
                                        userOpinion = userOpinions[msg.pp.id] ?: msg.pp.activeUsabilityClass,
                                        userComment = userComments[msg.pp.id] ?: ""
                                    )
                                }
                                val result = reportService.submitBatchReport(
                                    items = itemsToSubmit,
                                    allowAllSpamInfo = allowAllSpamInfo,
                                    usableInfoSharingOption = usableSharingOption
                                )
                                isSubmittingReport = false
                                showReportConfirmDialog = false
                                when (result) {
                                    is ReportSubmissionResult.Success -> {
                                        Toast.makeText(context, reportSuccessMessage, Toast.LENGTH_LONG).show()
                                        isReportMode = false
                                        selectedItemIds.clear()
                                    }
                                    is ReportSubmissionResult.SavedLocallyOnly -> {
                                        Toast.makeText(context, reportSavedOfflineMessage, Toast.LENGTH_LONG).show()
                                        isReportMode = false
                                        selectedItemIds.clear()
                                    }
                                    is ReportSubmissionResult.Failure -> {
                                        Toast.makeText(context, reportSavedOfflineMessage, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = !isSubmittingReport
                    ) {
                        Text(stringResource(R.string.report_dialog_confirm))
                    }
                }
            },
            dismissButton = {
                CompositionLocalProvider(
                    LocalLayoutDirection provides currentLayoutDirection,
                    LocalContext provides currentContext,
                    LocalConfiguration provides currentConfiguration
                ) {
                    TextButton(
                        onClick = { showReportConfirmDialog = false },
                        enabled = !isSubmittingReport
                    ) {
                        Text(stringResource(R.string.export_cancel))
                    }
                }
            }
        )
    }

    PayamaxScaffold(
        title = if (isReportMode) stringResource(R.string.report_mode_title) else titleText,
        navigationIcon = if (isReportMode) null else Icons.AutoMirrored.Filled.ArrowBack,
        navigationIconContentDescription = if (isReportMode) null else stringResource(R.string.nav_back),
        onNavigationIconClick = if (isReportMode) null else { { navController.navigateUp() } },
        topBarHeader = if (isReportMode) {
            {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            isReportMode = false
                            selectedItemIds.clear()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.report_mode_cancel)
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.report_mode_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.report_mode_selected_count, selectedItemIds.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedItemIds.size == viewModel.messages.size && viewModel.messages.isNotEmpty()) {
                                selectedItemIds.clear()
                            } else {
                                selectedItemIds.clear()
                                viewModel.messages.forEach { selectedItemIds[it.pp.id] = true }
                            }
                        }) {
                            Icon(
                                imageVector = if (selectedItemIds.size == viewModel.messages.size && viewModel.messages.isNotEmpty())
                                    Icons.Default.Deselect else Icons.Default.SelectAll,
                                contentDescription = stringResource(
                                    if (selectedItemIds.size == viewModel.messages.size && viewModel.messages.isNotEmpty())
                                        R.string.report_mode_deselect_all else R.string.report_mode_select_all
                                )
                            )
                        }
                        IconButton(onClick = {
                            if (selectedItemIds.isEmpty()) {
                                Toast.makeText(context, reportNoSelectionMessage, Toast.LENGTH_SHORT).show()
                            } else {
                                showReportConfirmDialog = true
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.report_mode_submit),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        } else null,
        actions = {
            if (!isReportMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = spacing.medium)
                ) {
                    if (!photoUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = titleText,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading && viewModel.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (viewModel.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_messages),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = spacing.small)
            ) {
                items(
                    items = viewModel.messages,
                    key = { it.pp.id }
                ) { msg ->
                    val msgId = msg.pp.id
                    val isSelected = selectedItemIds.containsKey(msgId)
                    MessageComposable(
                        msgValue = msg,
                        instantProvider = instantProvider,
                        onSelected = { isChecked ->
                            if (isChecked) {
                                selectedItemIds[msgId] = true
                            } else {
                                selectedItemIds.remove(msgId)
                            }
                        },
                        onClicked = { id ->
                            if (isReportMode) {
                                if (selectedItemIds.containsKey(id)) {
                                    selectedItemIds.remove(id)
                                } else {
                                    selectedItemIds[id] = true
                                }
                            } else {
                                navController.navigate(MessageRoute(id))
                            }
                        },
                        onDesiredResultChanged = {},
                        isReportMode = isReportMode,
                        isSelected = isSelected,
                        onLongClicked = {
                            isReportMode = true
                            selectedItemIds.clear()
                            selectedItemIds[msgId] = true
                        },
                        userOpinion = userOpinions[msgId],
                        onUserOpinionChanged = { opinion ->
                            userOpinions[msgId] = opinion
                        },
                        userComment = userComments[msgId] ?: "",
                        onUserCommentChanged = { comment ->
                            userComments[msgId] = comment
                        },
                        calendarType = calendarType
                    )
                }

                if (viewModel.isPageLoading) {
                    item(key = "conversation_page_loading") {
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
                buffer = 2,
                isLoading = viewModel.isPageLoading
            ) {
                if (viewModel.hasMore) {
                    viewModel.loadNextPage(contentResolver, address, title, engine)
                }
            }
        }
    }
}
