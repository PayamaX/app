package no1.payamax.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.PayamakThread
import no1.payamax.utils.toPersianDigits

@Composable
fun ThreadComposable(
    thread: PayamakThread,
    onThreadClicked: (PayamakThread) -> Unit,
    isReportMode: Boolean = false,
    isSelected: Boolean = false,
    onThreadLongClicked: ((PayamakThread) -> Unit)? = null,
    onSelectedToggle: ((Boolean) -> Unit)? = null,
    userOpinion: PayamakUsabilityClass? = null,
    onUserOpinionChanged: ((PayamakUsabilityClass) -> Unit)? = null,
    userComment: String = "",
    onUserCommentChanged: ((String) -> Unit)? = null,
) {
    val usabilityClass = if (isReportMode && userOpinion != null) userOpinion else thread.latestMessage.pp.activeUsabilityClass
    val (bgColor, textColor, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)
    val spacing = MaterialTheme.spacing
    val photoUri = thread.origin.contact?.photoUri

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall + 2.dp)
            .combinedClickable(
                onClick = {
                    if (isReportMode) {
                        onSelectedToggle?.invoke(!isSelected)
                    } else {
                        onThreadClicked(thread)
                    }
                },
                onLongClick = {
                    onThreadLongClicked?.invoke(thread)
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReportMode && isSelected)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isReportMode && isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
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
                            onCheckedChange = { onSelectedToggle?.invoke(it) },
                            modifier = Modifier.padding(end = spacing.extraSmall)
                        )
                    }

                    // Contact Photo / Avatar
                    if (!photoUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = thread.origin.displayable(),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = strokeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = thread.origin.displayable().take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = strokeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing.small + 4.dp))

                    Column {
                        Text(
                            text = thread.origin.displayable(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val numberFormatted = thread.origin.number?.displayable()?.toPersianDigits()
                        if (numberFormatted != null && numberFormatted != thread.origin.displayable()) {
                            Text(
                                text = numberFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.extraSmall))
                        PersianDateTimeChips(timeInMillis = thread.latestMessage.pp.payamak.received)
                    }
                }

                Spacer(modifier = Modifier.width(spacing.small))

                // Usability Pill Badge
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

            Spacer(modifier = Modifier.height(spacing.mediumSmall))

            // Latest Message Preview
            Text(
                text = thread.latestMessage.pp.payamak.body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Report section when item is selected in report mode
            if (isReportMode && isSelected) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = spacing.small),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Row: User opinionated detection
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
                        val activeOpinion = userOpinion ?: thread.latestMessage.pp.activeUsabilityClass
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

                // Row: Comment box
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
