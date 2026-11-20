package no1.payamax.composables

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.hasValue
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.modules.report.PayamakReportContract
import no1.payamax.modules.report.PayamakReportSubmissionItem
import no1.payamax.modules.report.ReportSubmissionResult
import no1.payamax.modules.report.UsableInfoSharingOption
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.ExcelExporter
import no1.payamax.utils.OriginExportData
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportEmptyMessage = stringResource(R.string.export_empty)
    val exportSuccessMessage = stringResource(R.string.export_success)
    val exportFailedMessage = stringResource(R.string.export_failed)
    val reportSuccessMessage = stringResource(R.string.report_success_toast)
    val reportSavedOfflineMessage = stringResource(R.string.report_saved_offline_toast)
    val reportNoSelectionMessage = stringResource(R.string.report_no_selection_toast)

    val messages = remember { mutableStateListOf<ReviewableProcessedPayamak>() }
    var lastIndex = 0
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
    val reportService by inject<PayamakReportContract>(PayamakReportContract::class.java)
    val payamakColumns = PayamakColumns(cursor)

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Batch report mode state
    var isReportMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateMapOf<Long, Boolean>() }
    val userOpinions = remember { mutableStateMapOf<Long, PayamakUsabilityClass>() }
    val userComments = remember { mutableStateMapOf<Long, String>() }

    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var allowAllSpamInfo by remember { mutableStateOf(true) }
    var usableSharingOption by remember { mutableStateOf(UsableInfoSharingOption.PARTIAL) }
    var isSubmittingReport by remember { mutableStateOf(false) }

    // Intercept back button when in report mode
    BackHandler(enabled = isReportMode) {
        isReportMode = false
        selectedItemIds.clear()
    }

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

    fun performExport() {
        val categoryName = if (selectedCategoryIndex == 1) "Useful" else "Spam"
        val categoryLabel = if (selectedCategoryIndex == 1) "پیام‌های مفید" else "اسپم و تبلیغاتی"

        val exportList = messages
            .filter { activeTypes.contains(it.pp.activeUsabilityClass) }
            .filter {
                if (selectedCategoryIndex == 1) {
                    it.pp.payamak.origin.contact == null || !it.pp.payamak.origin.contact.hasValue
                } else true
            }
            .distinctBy {
                it.pp.payamak.origin.number?.displayable() ?: it.pp.payamak.origin.displayable()
            }
            .map {
                val origin = it.pp.payamak.origin
                OriginExportData(
                    title = origin.title ?: origin.displayable(),
                    number = origin.number?.displayable() ?: "",
                    category = categoryLabel
                )
            }

        if (exportList.isEmpty()) {
            Toast.makeText(context, exportEmptyMessage, Toast.LENGTH_SHORT).show()
            return
        }

        val result = ExcelExporter.exportOriginsToCsv(context, categoryName, exportList)
        result.onSuccess {
            Toast.makeText(context, exportSuccessMessage, Toast.LENGTH_LONG).show()
        }.onFailure {
            Toast.makeText(context, exportFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            performExport()
        } else {
            Toast.makeText(context, exportFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun onExportClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performExport()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                performExport()
            } else {
                showPermissionDialog = true
            }
        }
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

    var isLoadingMore by remember { mutableStateOf(false) }
    var hasMoreToLoad by remember { mutableStateOf(true) }

    fun loadNextBatch() {
        if (isLoadingMore || !hasMoreToLoad) return
        if (lastIndex >= cursor.count) {
            hasMoreToLoad = false
            return
        }

        isLoadingMore = true
        scope.launch(Dispatchers.Default) {
            val batch = mutableListOf<ReviewableProcessedPayamak>()
            var currentBatchIndex = 0
            var addedSize = 0
            var scannedCount = 0
            val targetBatchSize = 25
            val maxScanPerRun = 400

            synchronized(cursor) {
                if (cursor.moveToFirst()) {
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
                        batch.add(message)
                        scannedCount++
                        if (activeTypes.contains(message.pp.activeUsabilityClass)) {
                            addedSize++
                        }
                    } while (cursor.moveToNext() && addedSize < targetBatchSize && scannedCount < maxScanPerRun)
                    lastIndex = currentBatchIndex
                    if (currentBatchIndex >= cursor.count) {
                        hasMoreToLoad = false
                    }
                } else {
                    hasMoreToLoad = false
                }
            }

            withContext(Dispatchers.Main) {
                if (batch.isNotEmpty()) {
                    messages.addAll(batch)
                }
                isLoadingMore = false
            }
        }
    }

    val spacing = MaterialTheme.spacing

    LaunchedEffect(selectedCategoryIndex, messages.size, hasMoreToLoad) {
        if (messages.none { activeTypes.contains(it.pp.activeUsabilityClass) } && hasMoreToLoad && !isLoadingMore) {
            loadNextBatch()
        }
    }

    val filteredMessages by remember(messages, selectedCategoryIndex, searchQuery) {
        derivedStateOf {
            messages.filter { msg ->
                activeTypes.contains(msg.pp.activeUsabilityClass) &&
                        (searchQuery.isEmpty() ||
                                msg.pp.payamak.body.contains(searchQuery, ignoreCase = true) ||
                                msg.pp.payamak.origin.displayable().contains(searchQuery, ignoreCase = true))
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.export_permission_dialog_title)) },
            text = { Text(stringResource(R.string.export_permission_dialog_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                ) {
                    Text(stringResource(R.string.export_permission_grant))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.export_cancel))
                }
            }
        )
    }

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

                        // Consent 1: Spam messages consent
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

                        // Consent 2: Usable SMS consent
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
                                val itemsToSubmit = messages
                                    .filter { selectedItemIds.containsKey(it.pp.id) }
                                    .map { msg ->
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
        title = if (isReportMode) stringResource(R.string.report_mode_title) else stringResource(R.string.app_title_full),
        modifier = modifier,
        topBarHeader = {
            Column {
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
                                if (selectedItemIds.size == filteredMessages.size && filteredMessages.isNotEmpty()) {
                                    selectedItemIds.clear()
                                } else {
                                    selectedItemIds.clear()
                                    filteredMessages.forEach { selectedItemIds[it.pp.id] = true }
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedItemIds.size == filteredMessages.size && filteredMessages.isNotEmpty())
                                        Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = stringResource(
                                        if (selectedItemIds.size == filteredMessages.size && filteredMessages.isNotEmpty())
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
                            if (selectedCategoryIndex == 1 || selectedCategoryIndex == 2) {
                                IconButton(onClick = { onExportClicked() }) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = stringResource(R.string.export_excel),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
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
        }
    ) { padding ->
        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingMore) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
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
            }
        } else {
            InfiniteList(
                listItems = filteredMessages,
                onLoadMore = {
                    loadNextBatch()
                },
                onSelected = {},
                onClicked = { id ->
                    navController.navigate(
                        MessageRoute(id)
                    )
                },
                onDesiredResultChanged = {},
                paddings = padding,
                isReportMode = isReportMode,
                selectedIds = selectedItemIds.keys,
                onItemLongClicked = { id ->
                    isReportMode = true
                    selectedItemIds.clear()
                    selectedItemIds[id] = true
                },
                onItemSelectToggle = { id, isChecked ->
                    if (isChecked) {
                        selectedItemIds[id] = true
                    } else {
                        selectedItemIds.remove(id)
                    }
                },
                userOpinions = userOpinions,
                onUserOpinionChanged = { id, opinion ->
                    userOpinions[id] = opinion
                    selectedItemIds[id] = true
                },
                userComments = userComments,
                onUserCommentChanged = { id, comment ->
                    userComments[id] = comment
                    selectedItemIds[id] = true
                },
                isLoading = isLoadingMore
            )
        }
    }
}
