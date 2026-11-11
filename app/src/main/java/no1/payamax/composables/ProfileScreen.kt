package no1.payamax.composables

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.utils.AppLanguage
import no1.payamax.utils.CalendarType
import no1.payamax.utils.ThemeMode
import no1.payamax.utils.UserPreferences
import org.koin.java.KoinJavaComponent.inject

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val userPreferences by inject<UserPreferences>(UserPreferences::class.java)
    val currentThemeMode by userPreferences.themeMode.collectAsState()
    val currentCalendarType by userPreferences.calendarType.collectAsState()
    val currentAppLanguage by userPreferences.appLanguage.collectAsState()
    val currentHttpLogging by userPreferences.httpLoggingEnabled.collectAsState()
    val spacing = MaterialTheme.spacing

    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showCalendarSheet by remember { mutableStateOf(false) }

    PayamaxScaffold(
        title = stringResource(R.string.profile_settings_title),
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacing.medium, vertical = spacing.small)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Header Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(spacing.medium + 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(spacing.medium))
                    Column {
                        Text(
                            text = stringResource(R.string.user_profile_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(spacing.extraSmall))
                        Text(
                            text = stringResource(R.string.user_profile_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Personalization Settings
            SectionHeader(title = stringResource(R.string.section_personalization))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Language ProfileItem
                    ProfileItem(
                        title = stringResource(R.string.language_settings_title),
                        icon = Icons.Outlined.Language,
                        valueText = when (currentAppLanguage) {
                            AppLanguage.PERSIAN -> stringResource(R.string.language_persian)
                            AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                        },
                        onClick = { showLanguageSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Theme ProfileItem
                    ProfileItem(
                        title = stringResource(R.string.theme_settings_title),
                        icon = Icons.Outlined.Palette,
                        valueText = when (currentThemeMode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            ThemeMode.DARK -> stringResource(R.string.theme_dark)
                        },
                        onClick = { showThemeSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Calendar ProfileItem
                    ProfileItem(
                        title = stringResource(R.string.calendar_settings_title),
                        icon = Icons.Outlined.CalendarMonth,
                        valueText = when (currentCalendarType) {
                            CalendarType.PERSIAN -> stringResource(R.string.calendar_persian)
                            CalendarType.GREGORIAN -> stringResource(R.string.calendar_gregorian)
                        },
                        onClick = { showCalendarSheet = true }
                    )
                }
            }

            // Section 2: Developer / Debug Settings
            SectionHeader(title = stringResource(R.string.section_developer))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileItem(
                    title = stringResource(R.string.http_logging_title),
                    icon = Icons.Outlined.BugReport,
                    showChevron = false,
                    trailingContent = {
                        Switch(
                            checked = currentHttpLogging,
                            onCheckedChange = { userPreferences.setHttpLoggingEnabled(it) }
                        )
                    }
                )
            }

            // Section 2: About App Info
            SectionHeader(title = stringResource(R.string.section_about))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(spacing.medium + 2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.about_app_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.small))
                    Text(
                        text = stringResource(R.string.about_app_description),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(spacing.mediumSmall))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(spacing.mediumSmall))

                    Text(text = stringResource(R.string.about_feature_1), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(spacing.extraSmall))
                    Text(text = stringResource(R.string.about_feature_2), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(spacing.extraSmall))
                    Text(text = stringResource(R.string.about_feature_3), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(spacing.extraSmall))
                    Text(text = stringResource(R.string.about_feature_4), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Bottom Sheets
        if (showLanguageSheet) {
            SelectionBottomSheet(
                title = stringResource(R.string.language_settings_title),
                options = listOf(
                    AppLanguage.PERSIAN to stringResource(R.string.language_persian),
                    AppLanguage.ENGLISH to stringResource(R.string.language_english)
                ),
                selectedOption = currentAppLanguage,
                onOptionSelected = { userPreferences.setAppLanguage(it) },
                onDismissRequest = { showLanguageSheet = false }
            )
        }

        if (showThemeSheet) {
            SelectionBottomSheet(
                title = stringResource(R.string.theme_settings_title),
                options = listOf(
                    ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                    ThemeMode.LIGHT to stringResource(R.string.theme_light),
                    ThemeMode.DARK to stringResource(R.string.theme_dark)
                ),
                selectedOption = currentThemeMode,
                onOptionSelected = { userPreferences.setThemeMode(it) },
                onDismissRequest = { showThemeSheet = false }
            )
        }

        if (showCalendarSheet) {
            SelectionBottomSheet(
                title = stringResource(R.string.calendar_settings_title),
                options = listOf(
                    CalendarType.PERSIAN to stringResource(R.string.calendar_persian),
                    CalendarType.GREGORIAN to stringResource(R.string.calendar_gregorian)
                ),
                selectedOption = currentCalendarType,
                onOptionSelected = { userPreferences.setCalendarType(it) },
                onDismissRequest = { showCalendarSheet = false }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}
