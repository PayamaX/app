package no1.payamax.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.InstantProvider
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.utils.CalendarType
import no1.payamax.utils.UserPreferences
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import no1.payamax.utils.toFormattedDateString
import no1.payamax.utils.toPersianTimeString
import org.koin.java.KoinJavaComponent.inject

@Composable
fun MessageComposable(
    msgValue: ReviewableProcessedPayamak,
    instantProvider: InstantProvider,
    onSelected: (Boolean) -> Unit,
    onClicked: (Long) -> Unit,
    onDesiredResultChanged: () -> Unit,
    isReportMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClicked: (() -> Unit)? = null,
    userOpinion: PayamakUsabilityClass? = null,
    onUserOpinionChanged: ((PayamakUsabilityClass) -> Unit)? = null,
    userComment: String = "",
    onUserCommentChanged: ((String) -> Unit)? = null,
    calendarType: CalendarType = CalendarType.PERSIAN,
    isReported: Boolean = false,
) {
    val usabilityClass = if (isReportMode && userOpinion != null) userOpinion else msgValue.pp.activeUsabilityClass
    val (_, _, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)
    val spacing = MaterialTheme.spacing

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall + 2.dp)
            .combinedClickable(
                onClick = {
                    if (isReportMode) {
                        onSelected(!isSelected)
                    } else {
                        onClicked(msgValue.pp.id)
                    }
                },
                onLongClick = {
                    onLongClicked?.invoke()
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReportMode && isSelected)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = if (isReportMode && isSelected)
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else
            null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (isReportMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelected(it) },
                            modifier = Modifier.padding(end = spacing.small)
                        )
                    }

                    // Avatar icon
                    Surface(
                        shape = CircleShape,
                        color = strokeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = msgValue.pp.payamak.origin.displayable().take(1),
                                style = MaterialTheme.typography.titleMedium,
                                color = strokeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing.small + 2.dp))

                    Column {
                        Text(
                            text = msgValue.pp.payamak.origin.displayable(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(spacing.extraSmall))
                        PersianDateTimeChips(timeInMillis = msgValue.pp.payamak.received, calendarType = calendarType)
                    }
                }

                Spacer(modifier = Modifier.width(spacing.small))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    if (isReported) {
                        ReportedPayamakIcon()
                    }
                    UsabilityPill(usabilityClass = usabilityClass)
                }
            }

            Spacer(modifier = Modifier.height(spacing.mediumSmall))

            Text(
                text = msgValue.pp.payamak.body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isReportMode && isSelected) Int.MAX_VALUE else 3,
                overflow = if (isReportMode && isSelected) TextOverflow.Clip else TextOverflow.Ellipsis
            )

            if (isReportMode && isSelected) {
                Spacer(modifier = Modifier.height(spacing.mediumSmall))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(spacing.small))

                // Row 1: System detection result
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.report_system_detection),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val systemClass = msgValue.pp.usability.clazz
                    val (sysBg, sysText, sysStroke) = ColorSchemeExtension.getUsabilityColors(systemClass)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = sysBg,
                        border = BorderStroke(1.dp, sysStroke.copy(alpha = 0.5f))
                    ) {
                        val percent = (msgValue.pp.usability.rate.probability * 100).toInt()
                        Text(
                            text = "${getUsabilityPersianTitle(systemClass)} ($percent%)",
                            modifier = Modifier.padding(horizontal = spacing.small + 2.dp, vertical = spacing.extraSmall - 1.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = sysText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.small))

                // Row 2: User opinionated detection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.report_user_opinion),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(spacing.extraSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        val activeOpinion = userOpinion ?: msgValue.pp.activeUsabilityClass
                        val opinionOptions = listOf(
                            PayamakUsabilityClass.Important,
                            PayamakUsabilityClass.Usable,
                            PayamakUsabilityClass.Spam
                        )
                        for (clazz in opinionOptions) {
                            val isChipSelected = activeOpinion == clazz
                            val (chipBg, chipText, chipStroke) = ColorSchemeExtension.getUsabilityColors(clazz)
                            FilterChip(
                                selected = isChipSelected,
                                onClick = { onUserOpinionChanged?.invoke(clazz) },
                                label = { Text(getUsabilityPersianTitle(clazz)) },
                                shape = RoundedCornerShape(999.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipBg,
                                    selectedLabelColor = chipText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isChipSelected,
                                    selectedBorderColor = chipStroke
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.small))

                // Row 3: A comment box
                OutlinedTextField(
                    value = userComment,
                    onValueChange = { onUserCommentChanged?.invoke(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.report_comment_label)) },
                    placeholder = { Text(stringResource(R.string.report_comment_placeholder)) },
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

@Composable
fun PersianDateTimeChips(
    timeInMillis: Long,
    calendarType: CalendarType = CalendarType.PERSIAN,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.small - 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Item (تاریخ)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.small, vertical = spacing.extraSmall - 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Text(
                    text = if (calendarType == CalendarType.PERSIAN) "📅" else "📆",
                    fontSize = 11.sp
                )
                Text(
                    text = timeInMillis.toFormattedDateString(calendarType),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Time Item (ساعت)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = spacing.small, vertical = spacing.extraSmall - 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Text(
                    text = "⏰",
                    fontSize = 11.sp
                )
                Text(
                    text = timeInMillis.toPersianTimeString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReportedPayamakIcon() {
    Icon(
        imageVector = Icons.Outlined.Flag,
        contentDescription = stringResource(R.string.report_already_reported),
        modifier = Modifier.size(18.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun UsabilityPill(usabilityClass: PayamakUsabilityClass) {
    val (bgColor, textColor, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)
    val spacing = MaterialTheme.spacing
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(1.dp, strokeColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = getUsabilityPersianTitle(usabilityClass),
            modifier = Modifier.padding(horizontal = spacing.small + 2.dp, vertical = spacing.extraSmall),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}

@Composable
fun getUsabilityPersianTitle(clazz: PayamakUsabilityClass): String {
    return when (clazz) {
        PayamakUsabilityClass.Important -> stringResource(R.string.usability_important)
        PayamakUsabilityClass.Usable -> stringResource(R.string.usability_usable)
        PayamakUsabilityClass.Spam -> stringResource(R.string.usability_spam)
        PayamakUsabilityClass.Unknown -> stringResource(R.string.usability_unknown)
    }
}
