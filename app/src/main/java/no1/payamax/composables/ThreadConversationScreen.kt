package no1.payamax.composables

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.Origin
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.cellNumber
import no1.payamax.enums.OriginRankSource
import no1.payamax.modules.rankedorigin.RankedOriginContract
import no1.payamax.modules.report.applySavedPayamakReports
import no1.payamax.modules.report.PayamakReportContract
import no1.payamax.modules.report.PayamakReportSubmissionItem
import no1.payamax.modules.report.ReportSubmissionResult
import no1.payamax.modules.report.describeReportFailure
import no1.payamax.modules.report.UsableInfoSharingOption
import no1.payamax.services.InstantProviderService
import no1.payamax.services.RankedOriginProcessor
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.SmsAddressUtils
import no1.payamax.utils.UserPreferences
import no1.payamax.vm.ThreadConversationViewModel
import org.koin.java.KoinJavaComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadConversationScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    address: String,
    title: String,
    autoFocusInput: Boolean = false
) {
    val engine = remember { KoinJavaComponent.get<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java) }
    val rankedOriginRepo = remember { KoinJavaComponent.get<RankedOriginContract>(RankedOriginContract::class.java) }
    val rankedOriginProcessor = remember { KoinJavaComponent.get<RankedOriginProcessor>(RankedOriginProcessor::class.java) }
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
    val smsSendFailedMessage = stringResource(R.string.sms_send_failed_toast)
    val categoryUpdatedFormat = stringResource(R.string.conversation_category_updated_toast)

    // Overflow menu state for sender category reclassification
    var showMoreMenu by remember { mutableStateOf(false) }
    var overriddenSenderCategory by remember(address) { mutableStateOf<PayamakUsabilityClass?>(null) }

    // Batch report mode state
    var isReportMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateMapOf<Long, Boolean>() }
    val userOpinions = remember { mutableStateMapOf<Long, PayamakUsabilityClass>() }
    val userComments = remember { mutableStateMapOf<Long, String>() }
    val reportedPayamakIds = remember { mutableStateMapOf<Long, Boolean>() }

    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var allowAllSpamInfo by remember { mutableStateOf(true) }
    var usableSharingOption by remember { mutableStateOf(UsableInfoSharingOption.PARTIAL) }
    var isSubmittingReport by remember { mutableStateOf(false) }

    val bannerKey = remember(address) {
        val cleaned = SmsAddressUtils.cleanAddress(address).trim().lowercase()
        val digits = cleaned.filter { it.isDigit() }
        if (digits.length >= 10) digits.takeLast(10) else cleaned
    }
    var isBannerDismissed by remember(bannerKey) {
        mutableStateOf(userPreferences.isFeedbackBannerDismissed(bannerKey))
    }
    var isOverrideChecked by remember(address) { mutableStateOf(false) }
    var hasExistingUserOverride by remember(address) { mutableStateOf(false) }

    // Compose input state
    var inputText by remember { mutableStateOf("") }
    val inputFocusRequester = remember { FocusRequester() }

    fun doSendSms() {
        val textToSend = inputText.trim()
        if (textToSend.isEmpty()) return
        inputText = ""
        viewModel.sendMessage(
            context = context,
            address = address,
            body = textToSend,
            onSendFailed = { err ->
                val errorMsg = if (!err.isNullOrBlank()) "$smsSendFailedMessage: $err" else smsSendFailedMessage
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        )
        scope.launch {
            try {
                listState.animateScrollToItem(0)
            } catch (_: Exception) {
            }
        }
    }

    val smsSendPermissionRequiredMessage = stringResource(R.string.sms_send_permission_required)

    val sendSmsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            doSendSms()
        } else {
            Toast.makeText(context, smsSendPermissionRequiredMessage, Toast.LENGTH_LONG).show()
        }
    }

    fun onSendClicked() {
        if (inputText.trim().isEmpty()) return
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            doSendSms()
        } else {
            sendSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    BackHandler(enabled = isReportMode) {
        isReportMode = false
        selectedItemIds.clear()
    }

    DisposableEffect(navController.currentBackStackEntry) {
        for (i in viewModel.messages.indices) {
            val oldMsg = viewModel.messages[i]
            val updatedUsability = engine.detect(oldMsg.pp.payamak)
            if (updatedUsability != oldMsg.pp.usability || oldMsg.pp.expectedPayamakUsabilityClass != null) {
                val newMsg = oldMsg.copy(
                    pp = oldMsg.pp.copy(
                        usability = updatedUsability,
                        expectedPayamakUsabilityClass = null
                    )
                )
                viewModel.refreshMessage(newMsg)
            }
        }
        onDispose { }
    }

    EnsurePermissions {
        LaunchedEffect(address, title) {
            viewModel.loadThread(contentResolver, address, title, engine)
        }
    }

    val reportMessageIds = viewModel.messages.map { it.pp.id }
    LaunchedEffect(reportMessageIds) {
        if (reportMessageIds.isEmpty()) return@LaunchedEffect
        val saved = reportService.savedReportsByPayamakId()
        applySavedPayamakReports(
            saved = saved,
            payamakIds = reportMessageIds,
            userComments = userComments,
            userOpinions = userOpinions,
        )
        for (id in reportMessageIds) {
            if (id in saved) reportedPayamakIds[id] = true
        }
    }

    LaunchedEffect(address) {
        val targetCleaned = SmsAddressUtils.cleanAddress(address)
        val targetDigits = targetCleaned.filter { it.isDigit() }.takeLast(10)
        no1.payamax.services.IncomingSmsEventBus.events.collect { event ->
            val eventCleaned = SmsAddressUtils.cleanAddress(event.address)
            val eventDigits = eventCleaned.filter { it.isDigit() }.takeLast(10)
            val matches = event.address.equals(address, ignoreCase = true) ||
                eventCleaned.equals(targetCleaned, ignoreCase = true) ||
                (targetDigits.length >= 10 && targetDigits == eventDigits)
            if (matches) {
                viewModel.reloadThread(contentResolver, address, title, engine)
            }
        }
    }

    LaunchedEffect(autoFocusInput, viewModel.canReply) {
        if (autoFocusInput && viewModel.canReply) {
            try {
                inputFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    val cachedContact = viewModel.cachedContact
    val firstOrigin = viewModel.messages.firstOrNull()?.pp?.payamak?.origin
    val titleText = cachedContact?.name ?: (if (title.isNotBlank()) title else firstOrigin?.displayable() ?: address)
    val photoUri = cachedContact?.photoUri ?: firstOrigin?.contact?.photoUri

    val singleMessage = if (!viewModel.isLoading && !viewModel.hasMore && viewModel.messages.size == 1) {
        viewModel.messages.firstOrNull()
    } else {
        null
    }

    LaunchedEffect(address, singleMessage?.pp?.id) {
        val msg = singleMessage
        if (msg != null) {
            rankedOriginProcessor.ensureLoaded()
            hasExistingUserOverride = rankedOriginProcessor.hasUserOverride(msg.pp.payamak)
            isOverrideChecked = true
        }
    }

    fun dismissBannerPermanently() {
        isBannerDismissed = true
        userPreferences.dismissFeedbackBanner(bannerKey)
    }

    fun reclassifySender(clazz: PayamakUsabilityClass, categoryLabel: String) {
        val rank = when (clazz) {
            PayamakUsabilityClass.Important -> 1.0
            PayamakUsabilityClass.Usable -> 0.8
            PayamakUsabilityClass.Unknown -> 0.5
            PayamakUsabilityClass.Spam -> 0.0
        }
        val targetOrigin = firstOrigin ?: run {
            val cleaned = SmsAddressUtils.cleanAddress(address)
            val cellNum = cleaned.toLongOrNull()?.cellNumber()
            Origin(
                number = cellNum,
                title = if (cellNum == null) address else null,
                contact = cachedContact
            )
        }

        overriddenSenderCategory = clazz
        hasExistingUserOverride = true
        dismissBannerPermanently()

        scope.launch {
            rankedOriginRepo.upsert(targetOrigin, rank, OriginRankSource.User)
            rankedOriginProcessor.ensureLoaded()
            for (i in viewModel.messages.indices) {
                val oldMsg = viewModel.messages[i]
                if (!oldMsg.pp.payamak.isOutgoing) {
                    val updatedUsability = engine.detect(oldMsg.pp.payamak)
                    val newMsg = oldMsg.copy(
                        pp = oldMsg.pp.copy(
                            usability = updatedUsability,
                            expectedPayamakUsabilityClass = null
                        )
                    )
                    viewModel.refreshMessage(newMsg)
                }
            }
            Toast.makeText(
                context,
                String.format(categoryUpdatedFormat, categoryLabel),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Confirmation dialog for batch report mode
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
                                if (result is ReportSubmissionResult.Success || result is ReportSubmissionResult.SavedLocallyOnly) {
                                    for (item in itemsToSubmit) {
                                        reportedPayamakIds[item.message.pp.id] = true
                                    }
                                }
                                when (result) {
                                    is ReportSubmissionResult.Success -> {
                                        Toast.makeText(context, reportSuccessMessage, Toast.LENGTH_LONG).show()
                                        isReportMode = false
                                        selectedItemIds.clear()
                                    }
                                    is ReportSubmissionResult.SavedLocallyOnly -> {
                                        Toast.makeText(
                                            context,
                                            "$reportSavedOfflineMessage\n${result.throwable.describeReportFailure()}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        isReportMode = false
                                        selectedItemIds.clear()
                                    }
                                    is ReportSubmissionResult.Failure -> {
                                        Toast.makeText(
                                            context,
                                            result.throwable.describeReportFailure(),
                                            Toast.LENGTH_LONG
                                        ).show()
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
        topBarHeader = {
            if (isReportMode) {
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
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back)
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!photoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = titleText,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
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

                            Spacer(modifier = Modifier.width(spacing.small + 2.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (cachedContact != null && address.isNotBlank() && address != titleText) {
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Three-dot overflow menu: Report to backend + Reclassify sender
                        Box {
                            val activeSenderCategory = overriddenSenderCategory
                                ?: viewModel.messages.firstOrNull { !it.pp.payamak.isOutgoing }?.pp?.activeUsabilityClass
                                ?: viewModel.messages.firstOrNull()?.pp?.activeUsabilityClass

                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.conversation_menu_more)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                // Report sender/messages to backend
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.report_sender_action),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        isReportMode = true
                                        selectedItemIds.clear()
                                        val firstMsg = viewModel.messages.firstOrNull()
                                        if (firstMsg != null) {
                                            selectedItemIds[firstMsg.pp.id] = true
                                        }
                                    }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Text(
                                    text = stringResource(R.string.conversation_change_category_title),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                for (clazz in PayamakUsabilityClass.entries) {
                                    val (_, _, strokeColor) = ColorSchemeExtension.getUsabilityColors(clazz)
                                    val isCurrent = activeSenderCategory == clazz
                                    val categoryLabel = getUsabilityPersianTitle(clazz)

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = categoryLabel,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Surface(
                                                shape = CircleShape,
                                                color = strokeColor,
                                                modifier = Modifier.size(12.dp)
                                            ) {}
                                        },
                                        trailingIcon = if (isCurrent) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            showMoreMenu = false
                                            reclassifySender(clazz, categoryLabel)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isReportMode) {
                Surface(
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.medium, vertical = spacing.small),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.report_mode_selected_count, selectedItemIds.size),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = {
                                if (selectedItemIds.isEmpty()) {
                                    Toast.makeText(context, reportNoSelectionMessage, Toast.LENGTH_SHORT).show()
                                } else {
                                    showReportConfirmDialog = true
                                }
                            },
                            enabled = selectedItemIds.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.extraSmall))
                            Text(text = stringResource(R.string.report_mode_submit))
                        }
                    }
                }
            } else if (viewModel.canReply) {
                // Interactive Compose Input Bar
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    val segmentInfo = remember(inputText) { SmsAddressUtils.calculateSegmentInfo(inputText) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.medium, vertical = spacing.small)
                    ) {
                        if (inputText.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "${segmentInfo.length}/${segmentInfo.maxPerSegment} (${segmentInfo.segmentCount})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(inputFocusRequester),
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.compose_message_placeholder),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                maxLines = 5,
                                minLines = 1,
                                shape = RoundedCornerShape(22.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                                ),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Default
                                )
                            )

                            FilledIconButton(
                                onClick = { onSendClicked() },
                                enabled = inputText.trim().isNotEmpty(),
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.compose_send),
                                    modifier = Modifier.size(20.dp)
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
                    text = if (viewModel.canReply) stringResource(R.string.compose_empty_conversation) else stringResource(R.string.empty_messages),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(spacing.large)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = spacing.small)
            ) {
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

                items(
                    items = viewModel.messages,
                    key = { it.pp.id }
                ) { msg ->
                    val msgId = msg.pp.id
                    val isSelected = selectedItemIds.containsKey(msgId)

                    if (isReportMode) {
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
                                if (selectedItemIds.containsKey(id)) {
                                    selectedItemIds.remove(id)
                                } else {
                                    selectedItemIds[id] = true
                                }
                            },
                            onDesiredResultChanged = {},
                            isReportMode = true,
                            isSelected = isSelected,
                            onLongClicked = {
                                selectedItemIds[msgId] = !isSelected
                            },
                            userOpinion = userOpinions[msgId],
                            onUserOpinionChanged = { opinion ->
                                userOpinions[msgId] = opinion
                            },
                            userComment = userComments[msgId] ?: "",
                            onUserCommentChanged = { comment ->
                                userComments[msgId] = comment
                            },
                            calendarType = calendarType,
                            isReported = reportedPayamakIds.containsKey(msgId),
                            maxBodyLines = Int.MAX_VALUE
                        )
                    } else if (viewModel.canReply) {
                        ConversationBubbleComposable(
                            message = msg,
                            isReportMode = false,
                            isSelected = false,
                            calendarType = calendarType,
                            onClick = {
                                navController.navigate(MessageRoute(msgId))
                            },
                            onLongClick = {
                                isReportMode = true
                                selectedItemIds.clear()
                                selectedItemIds[msgId] = true
                            },
                            onRetryClick = {
                                viewModel.retryMessage(
                                    context = context,
                                    address = address,
                                    failedMessage = msg,
                                    onSendFailed = { err ->
                                        val errorMsg = if (!err.isNullOrBlank()) "$smsSendFailedMessage: $err" else smsSendFailedMessage
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    } else {
                        MessageComposable(
                            msgValue = msg,
                            instantProvider = instantProvider,
                            onSelected = {},
                            onClicked = { id ->
                                navController.navigate(MessageRoute(id))
                            },
                            onDesiredResultChanged = {},
                            isReportMode = false,
                            isSelected = false,
                            onLongClicked = {
                                isReportMode = true
                                selectedItemIds.clear()
                                selectedItemIds[msgId] = true
                            },
                            calendarType = calendarType,
                            isReported = reportedPayamakIds.containsKey(msgId),
                            maxBodyLines = Int.MAX_VALUE
                        )
                    }
                }

                // Interactive feedback banner: shown only once on the very first incoming SMS
                // from an unknown/unclassified sender (never shown if > 1 message or already decided)
                val shouldShowFeedbackBanner =
                    cachedContact == null &&
                    !isBannerDismissed &&
                    !isReportMode &&
                    overriddenSenderCategory == null &&
                    singleMessage != null &&
                    !singleMessage.pp.payamak.isOutgoing &&
                    isOverrideChecked &&
                    !hasExistingUserOverride

                if (shouldShowFeedbackBanner) {
                    item(key = "sender_feedback_banner") {
                        val spamCategoryLabel = getUsabilityPersianTitle(PayamakUsabilityClass.Spam)
                        val usableCategoryLabel = getUsabilityPersianTitle(PayamakUsabilityClass.Usable)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.medium, vertical = spacing.extraSmall)
                        ) {
                            Column(modifier = Modifier.padding(spacing.medium)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(spacing.small))
                                        Text(
                                            text = stringResource(R.string.banner_is_spam_question),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { dismissBannerPermanently() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.banner_dismiss),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(spacing.small))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                                ) {
                                    Button(
                                        onClick = {
                                            reclassifySender(PayamakUsabilityClass.Spam, spamCategoryLabel)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.banner_action_spam),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            reclassifySender(PayamakUsabilityClass.Usable, usableCategoryLabel)
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.banner_action_useful),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
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
