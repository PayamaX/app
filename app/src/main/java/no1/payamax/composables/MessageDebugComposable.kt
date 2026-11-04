package no1.payamax.composables

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.repos.PredefinedOriginRepo
import no1.payamax.services.OriginRankSource
import no1.payamax.services.PredefinedOriginProcessor
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.toPersianDigits
import org.koin.java.KoinJavaComponent.inject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageDebugComposable(
    msgValue: ReviewableProcessedPayamak,
    onSelected: (Boolean) -> Unit,
    onDesiredResultChanged: () -> Unit
) {
    val ctx = LocalContext.current
    val repo by inject<PredefinedOriginRepo>(PredefinedOriginRepo::class.java)
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
    val predefinedOriginProcessor by inject<PredefinedOriginProcessor>(PredefinedOriginProcessor::class.java)

    val expectedState = remember { mutableStateOf(msgValue.pp.expectedPayamakUsabilityClass) }
    val usabilityClass = expectedState.value ?: msgValue.pp.activeUsabilityClass
    val (bgColor, textColor, strokeColor) = ColorSchemeExtension.getUsabilityColors(usabilityClass)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sender & Usability Header Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = strokeColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = msgValue.pp.payamak.origin.displayable().take(1),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = strokeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = msgValue.pp.payamak.origin.displayable(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val formattedNumber = msgValue.pp.payamak.origin.number?.displayable()?.toPersianDigits()
                            if (formattedNumber != null && formattedNumber != msgValue.pp.payamak.origin.displayable()) {
                                Text(
                                    text = formattedNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            PersianDateTimeChips(timeInMillis = msgValue.pp.payamak.received)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, strokeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = getUsabilityPersianTitle(usabilityClass),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                }

                msgValue.pp.payamak.origin.contact?.let { contact ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.contact_label, contact.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Message Content Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.sms_body_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msgValue.pp.payamak.body,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SMS Body", msgValue.pp.payamak.body)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(ctx, ctx.getString(R.string.text_copied_toast), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.copy_text))
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msgValue.pp.dump())
                            }
                            val shareIntent = Intent.createChooser(sendIntent, ctx.getString(R.string.share_dialog_title))
                            ctx.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.share_text))
                    }
                }
            }
        }

        // Re-classification Action Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.reclassify_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (clazz in PayamakUsabilityClass.entries) {
                        val isSelected = expectedState.value == clazz
                        val (chipBg, chipText, chipStroke) = ColorSchemeExtension.getUsabilityColors(clazz)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                expectedState.value = clazz
                                msgValue.pp.expectedPayamakUsabilityClass = clazz

                                val rank = when (clazz) {
                                    PayamakUsabilityClass.Important -> 1.0
                                    PayamakUsabilityClass.Usable -> 0.8
                                    PayamakUsabilityClass.Unknown -> 0.5
                                    PayamakUsabilityClass.Spam -> 0.0
                                }

                                repo.upsert(msgValue.pp.payamak.origin, rank, OriginRankSource.User)
                                predefinedOriginProcessor.refresh()
                                msgValue.pp.usability = engine.detect(msgValue.pp.payamak)

                                onDesiredResultChanged()
                            },
                            label = { Text(getUsabilityPersianTitle(clazz)) },
                            shape = RoundedCornerShape(999.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipBg,
                                selectedLabelColor = chipText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = chipStroke
                            )
                        )
                    }
                }
            }
        }

        // Processor Rules Breakdown Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.processing_details_title, msgValue.pp.usability.rate.probability),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                for (pr in msgValue.pp.usability.processResults) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pr.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val rateText = pr.rate?.toString() ?: stringResource(R.string.no_rate)
                        Text(
                            text = rateText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
