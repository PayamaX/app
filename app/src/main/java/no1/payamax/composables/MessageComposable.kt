package no1.payamax.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.contracts.InstantProvider
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.utils.CalendarType
import no1.payamax.utils.UserPreferences
import no1.payamax.utils.toFormattedDateString
import no1.payamax.utils.toPersianTimeString
import org.koin.java.KoinJavaComponent.inject

@Composable
fun MessageComposable(
    msgValue: ReviewableProcessedPayamak,
    instantProvider: InstantProvider,
    onSelected: (Boolean) -> Unit,
    onClicked: (Long) -> Unit,
    onDesiredResultChanged: () -> Unit
) {
    val usabilityClass = msgValue.pp.activeUsabilityClass
    val (bgColor, textColor, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClicked(msgValue.pp.id) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = msgValue.pp.payamak.origin.displayable(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        PersianDateTimeChips(timeInMillis = msgValue.pp.payamak.received)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Usability Pill Badge
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, strokeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = getUsabilityPersianTitle(usabilityClass),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = msgValue.pp.payamak.body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PersianDateTimeChips(
    timeInMillis: Long,
    modifier: Modifier = Modifier
) {
    val userPreferences by inject<UserPreferences>(UserPreferences::class.java)
    val calendarType by userPreferences.calendarType.collectAsState()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Item (تاریخ)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (calendarType == no1.payamax.utils.CalendarType.PERSIAN) "📅" else "📆",
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
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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

fun getUsabilityPersianTitle(clazz: PayamakUsabilityClass): String {
    return when (clazz) {
        PayamakUsabilityClass.Important -> "مهم"
        PayamakUsabilityClass.Usable -> "کاربردی"
        PayamakUsabilityClass.Spam -> "اسپم"
        PayamakUsabilityClass.Unknown -> "نامشخص"
    }
}
