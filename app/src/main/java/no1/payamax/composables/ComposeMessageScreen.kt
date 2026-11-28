package no1.payamax.composables

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.ThreadConversationRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.Contact
import no1.payamax.utils.SmsAddressUtils

data class ContactEntry(
    val contact: Contact,
    val phoneNumber: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMessageScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    onBackClick: () -> Unit = { navController.navigateUp() }
) {
    var searchQuery by remember { mutableStateOf("") }
    val contactsList = remember { mutableStateListOf<ContactEntry>() }
    var isLoadingContacts by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.spacing

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val loaded = mutableListOf<ContactEntry>()
            try {
                val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                )
                contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                    while (cursor.moveToNext()) {
                        val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                        val rawNumber = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                        val photoUri = if (photoIdx >= 0) cursor.getString(photoIdx) else null

                        val cleanedNum = SmsAddressUtils.cleanAddress(rawNumber)
                        if (cleanedNum.isNotBlank() && name.isNotBlank()) {
                            loaded.add(
                                ContactEntry(
                                    contact = Contact(id, name, photoUri),
                                    phoneNumber = cleanedNum
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore query exceptions
            }

            withContext(Dispatchers.Main) {
                contactsList.clear()
                contactsList.addAll(loaded.distinctBy { it.phoneNumber })
                isLoadingContacts = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    val filteredContacts = remember(searchQuery, contactsList.size) {
        if (searchQuery.isBlank()) {
            contactsList
        } else {
            val q = searchQuery.trim()
            contactsList.filter { entry ->
                entry.contact.name.contains(q, ignoreCase = true) ||
                        entry.phoneNumber.contains(q)
            }
        }
    }

    val isSearchDirectNumber = remember(searchQuery) {
        val cleaned = SmsAddressUtils.cleanAddress(searchQuery)
        SmsAddressUtils.canReplyToAddress(cleaned)
    }

    PayamaxScaffold(
        title = stringResource(R.string.compose_new_message),
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.nav_back),
        onNavigationIconClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Search / Recipient Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium, vertical = spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.compose_to_label) + ":",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = spacing.small)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.compose_to_placeholder),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.nav_clear)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Direct Number Card if input is dialable
            if (isSearchDirectNumber) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium, vertical = spacing.small)
                        .clickable {
                            val directNum = SmsAddressUtils.normalizeSmsDestination(searchQuery.trim())
                            navController.navigate(
                                ThreadConversationRoute(
                                    address = directNum,
                                    title = directNum,
                                    autoFocusInput = true
                                )
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.medium, vertical = spacing.small + 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(spacing.medium))

                        Column {
                            Text(
                                text = stringResource(R.string.compose_start_chat_with, searchQuery.trim()),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = SmsAddressUtils.normalizeSmsDestination(searchQuery.trim()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Contacts List
            if (isLoadingContacts) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                }
            } else if (filteredContacts.isEmpty() && !isSearchDirectNumber) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.compose_no_contacts),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(key = "contacts_header") {
                        Text(
                            text = stringResource(R.string.compose_contacts_header),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = spacing.medium,
                                vertical = spacing.small
                            )
                        )
                    }

                    items(
                        items = filteredContacts,
                        key = { "${it.contact.id}_${it.phoneNumber}" }
                    ) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val destination = SmsAddressUtils.normalizeSmsDestination(entry.phoneNumber)
                                    navController.navigate(
                                        ThreadConversationRoute(
                                            address = destination,
                                            title = entry.contact.name,
                                            autoFocusInput = true
                                        )
                                    )
                                }
                                .padding(horizontal = spacing.medium, vertical = spacing.small + 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!entry.contact.photoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = entry.contact.photoUri,
                                    contentDescription = entry.contact.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(spacing.medium))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.contact.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = entry.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
