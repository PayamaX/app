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
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.ThreadConversationRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.hasValue
import no1.payamax.model.PayamakThread
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

fun getThreadKey(msg: ReviewableProcessedPayamak): String {
    val origin = msg.pp.payamak.origin
    if (origin.contact != null && origin.contact.hasValue) {
        return "contact_${origin.contact.id}"
    }
    val numberStr = origin.number?.displayable() ?: ""
    val digits = (if (numberStr.isNotEmpty()) numberStr else (origin.title ?: "")).filter { it.isDigit() }
    if (digits.length >= 7) {
        return "num_${digits.takeLast(10)}"
    }
    val title = origin.title ?: origin.displayable()
    if (title.isNotBlank()) {
        return "title_${title.trim().lowercase()}"
    }
    return "addr_${msg.pp.id}"
}

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

    val threads = remember { mutableStateListOf<PayamakThread>() }
    val seenThreadKeys = remember { mutableSetOf<String>() }
    val seenRawAddresses = remember { mutableSetOf<String>() }
    var cursorOffset by remember { mutableIntStateOf(0) }
    var hasMoreToLoad by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }

    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
    val reportService by inject<PayamakReportContract>(PayamakReportContract::class.java)
    val payamakColumns = PayamakColumns(cursor)

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Batch report mode state
    var isReportMode by remember { mutableStateOf(false) }
    val selectedThreadKeys = remember { mutableStateMapOf<String, Boolean>() }
    val userOpinions = remember { mutableStateMapOf<Long, PayamakUsabilityClass>() }
    val userComments = remember { mutableStateMapOf<Long, String>() }

    var showReportConfirmDialog by remember { mutableStateOf(false) }
    var allowAllSpamInfo by remember { mutableStateOf(true) }
    var usableSharingOption by remember { mutableStateOf(UsableInfoSharingOption.PARTIAL) }
    var isSubmittingReport by remember { mutableStateOf(false) }

    // Intercept back button when in report mode
    BackHandler(enabled = isReportMode) {
        isReportMode = false
        selectedThreadKeys.clear()
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

    val filteredThreads by remember(threads.size, selectedCategoryIndex, searchQuery) {
        derivedStateOf {
            threads.filter { thread ->
                activeTypes.contains(thread.latestMessage.pp.activeUsabilityClass) &&
                        (searchQuery.isEmpty() ||
                                thread.origin.displayable().contains(searchQuery, ignoreCase = true) ||
                                thread.address.contains(searchQuery, ignoreCase = true) ||
                                thread.latestMessage.pp.payamak.body.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    fun performExport() {
        val categoryName = if (selectedCategoryIndex == 1) "Useful" else "Spam"
        val categoryLabel = if (selectedCategoryIndex == 1) "پیام‌های مفید" else "اسپم و تبلیغاتی"

        val exportList = threads
            .filter { activeTypes.contains(it.latestMessage.pp.activeUsabilityClass) }
            .filter {
                if (selectedCategoryIndex == 1) {
                    it.origin.contact == null || !it.origin.contact.hasValue
                } else true
            }
            .distinctBy {
                it.origin.number?.displayable() ?: it.origin.displayable()
            }
            .map {
                val origin = it.origin
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

    fun loadNextBatch() {
        if (isLoadingMore || !hasMoreToLoad) return
        if (cursorOffset >= cursor.count) {
            hasMoreToLoad = false
            return
        }

        isLoadingMore = true
        scope.launch(Dispatchers.Default) {
            val newThreads = mutableListOf<PayamakThread>()
            var currentOffset = cursorOffset
            var reachedEnd = false

            synchronized(cursor) {
                if (cursor.moveToPosition(currentOffset)) {
                    val targetNewThreads = 15
                    val maxScanLimit = 500
                    var scanned = 0

                    do {
                        val addressValue = cursor.getString(payamakColumns.addressIndex) ?: ""
                        if (!seenRawAddresses.contains(addressValue)) {
                            seenRawAddresses.add(addressValue)
                            val message = process(
                                cursor,
                                contentResolver,
                                payamakColumns,
                                engine
                            )
                            val key = getThreadKey(message)
                            if (!seenThreadKeys.contains(key)) {
                                seenThreadKeys.add(key)
                                val origin = message.pp.payamak.origin
                                val address = origin.number?.let { if (it.prefix != null) "+${it.prefix}${it.main}" else it.main.toString() }
                                    ?: (origin.title ?: origin.displayable())
                                newThreads.add(
                                    PayamakThread(
                                        key = key,
                                        address = address,
                                        origin = origin,
                                        latestMessage = message
                                    )
                                )
                            }
                        }
                        currentOffset++
                        scanned++
                    } while (cursor.moveToNext() && newThreads.size < targetNewThreads && scanned < maxScanLimit)

                    if (currentOffset >= cursor.count) {
                        reachedEnd = true
                    }
                } else {
                    reachedEnd = true
                }
            }

            withContext(Dispatchers.Main) {
                cursorOffset = currentOffset
                if (reachedEnd) {
                    hasMoreToLoad = false
                }
                if (newThreads.isNotEmpty()) {
                    threads.addAll(newThreads)
                }
                isLoadingMore = false
            }
        }
    }

    val spacing = MaterialTheme.spacing

    LaunchedEffect(selectedCategoryIndex, threads.size) {
        if (filteredThreads.size < 10 && hasMoreToLoad && !isLoadingMore) {
            loadNextBatch()
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
                            text = stringResource(R.string.report_dialog_desc, selectedThreadKeys.size),
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
                                val selectedThreads = threads.filter { selectedThreadKeys.containsKey(it.key) }
                                val itemsToSubmit = selectedThreads.map { thread ->
                                    val msg = thread.latestMessage
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
                                        selectedThreadKeys.clear()
                                    }
                                    is ReportSubmissionResult.SavedLocallyOnly -> {
                                        Toast.makeText(context, reportSavedOfflineMessage, Toast.LENGTH_LONG).show()
                                        isReportMode = false
                                        selectedThreadKeys.clear()
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
                                selectedThreadKeys.clear()
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
                                    text = stringResource(R.string.report_mode_selected_count, selectedThreadKeys.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                if (selectedThreadKeys.size == filteredThreads.size && filteredThreads.isNotEmpty()) {
                                    selectedThreadKeys.clear()
                                } else {
                                    selectedThreadKeys.clear()
                                    filteredThreads.forEach { selectedThreadKeys[it.key] = true }
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedThreadKeys.size == filteredThreads.size && filteredThreads.isNotEmpty())
                                        Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = stringResource(
                                        if (selectedThreadKeys.size == filteredThreads.size && filteredThreads.isNotEmpty())
                                            R.string.report_mode_deselect_all else R.string.report_mode_select_all
                                    )
                                )
                            }
                            IconButton(onClick = {
                                if (selectedThreadKeys.isEmpty()) {
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
        if (filteredThreads.isEmpty()) {
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
            ThreadInfiniteList(
                threads = filteredThreads,
                onLoadMore = {
                    loadNextBatch()
                },
                onThreadClicked = { thread ->
                    navController.navigate(
                        ThreadConversationRoute(
                            address = thread.address,
                            title = thread.origin.displayable()
                        )
                    )
                },
                paddings = padding,
                isLoading = isLoadingMore,
                isReportMode = isReportMode,
                selectedKeys = selectedThreadKeys.keys,
                onThreadLongClicked = { thread ->
                    isReportMode = true
                    selectedThreadKeys.clear()
                    selectedThreadKeys[thread.key] = true
                },
                onThreadSelectToggle = { key, isChecked ->
                    if (isChecked) {
                        selectedThreadKeys[key] = true
                    } else {
                        selectedThreadKeys.remove(key)
                    }
                },
                userOpinions = userOpinions,
                onUserOpinionChanged = { id, opinion ->
                    userOpinions[id] = opinion
                },
                userComments = userComments,
                onUserCommentChanged = { id, comment ->
                    userComments[id] = comment
                }
            )
        }
    }
}
