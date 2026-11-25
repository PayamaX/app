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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.model.SmsSendStatus
import no1.payamax.utils.CalendarType
import no1.payamax.utils.toFormattedDateString
import no1.payamax.utils.toPersianTimeString

@Composable
fun ConversationBubbleComposable(
    message: ReviewableProcessedPayamak,
    isReportMode: Boolean,
    isSelected: Boolean,
    calendarType: CalendarType,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.pp.payamak.isOutgoing
    val spacing = MaterialTheme.spacing
    val usabilityClass = message.pp.activeUsabilityClass
    val (_, _, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val containerColor = when {
        isReportMode && isSelected -> MaterialTheme.colorScheme.surfaceVariant
        isOutgoing -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val textColor = if (isOutgoing) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val bubbleAlignment = if (isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = 3.dp),
        horizontalAlignment = bubbleAlignment
    ) {
        Surface(
            shape = bubbleShape,
            color = containerColor,
            tonalElevation = if (isOutgoing) 1.dp else 2.dp,
            border = if (isReportMode && isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else if (!isOutgoing && usabilityClass == PayamakUsabilityClass.Spam) {
                BorderStroke(1.dp, strokeColor.copy(alpha = 0.5f))
            } else {
                null
            },
            modifier = Modifier
                .widthIn(min = 60.dp, max = 320.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (!isOutgoing && usabilityClass == PayamakUsabilityClass.Spam) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = strokeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = getUsabilityPersianTitle(usabilityClass),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = strokeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = message.pp.payamak.body,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${message.pp.payamak.received.toFormattedDateString(calendarType)}  ${message.pp.payamak.received.toPersianTimeString()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.7f)
                    )

                    if (isOutgoing) {
                        when (message.sendStatus) {
                            SmsSendStatus.SENDING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                            SmsSendStatus.FAILED -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = stringResource(R.string.sms_failed),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .combinedClickable(onClick = onRetryClick)
                                )
                            }
                            SmsSendStatus.SENT -> {
                                Text(
                                    text = "✓✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            SmsSendStatus.NONE -> {
                                Text(
                                    text = "✓",
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isOutgoing && message.sendStatus == SmsSendStatus.FAILED) {
            Surface(
                onClick = onRetryClick,
                color = Color.Transparent,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.sms_failed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
