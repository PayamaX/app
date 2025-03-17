package no1.payamax.cleanCompose.featureMessageDetails.presentation

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityRate
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak

@Composable
fun MessageDebugComposable(
    msgValue: ReviewableProcessedPayamak,
    onSelected: (Boolean) -> Unit,
    onEvent: (MessageDetailsEvents) -> Unit,
    onDesiredResultChanged: () -> Unit,
) {
    val expectedState = remember { mutableStateOf(msgValue.pp.expectedPayamakUsabilityClass) }
    val selectedState = remember { mutableStateOf(msgValue.selected) }
    val smsType = when (msgValue.pp.usability.clazz) {
        PayamakUsabilityClass.Important -> stringResource(R.string.important)
        PayamakUsabilityClass.Usable -> stringResource(R.string.important)
        PayamakUsabilityClass.Unknown -> stringResource(R.string.spam)
        PayamakUsabilityClass.Spam -> stringResource(R.string.spam)
        else -> stringResource(R.string.unknown)
    }

    PayamaxTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
//                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ) {

                Surface(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .border(1.5.dp, Color(0xFFE1E1E1), shape = RoundedCornerShape(8.dp)),
                    shadowElevation = 8.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = smsType,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .border(1.5.dp, Color(0xFFE1E1E1), shape = RoundedCornerShape(8.dp))
                        .fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column {
                        val ctx = LocalContext.current

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = msgValue.pp.payamak.origin.displayable(),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msgValue.pp.dump())
                                }
                                val shareIntent =
                                    Intent.createChooser(
                                        sendIntent,
                                        "email to px@no1.ir or t.me/DrHBN "
                                    )
                                ctx.startActivity(shareIntent)
                            }) {
                                Icon(Icons.Rounded.Share, contentDescription = "share")
                            }

                        }

                        Text(
                            text = msgValue.pp.payamak.body,
                            modifier = Modifier
                                .padding(5.dp)
                                .fillMaxWidth()
                                .padding(5.dp),
                            color = when (msgValue.pp.usability.clazz) {
                                PayamakUsabilityClass.Important -> Color.Black
                                PayamakUsabilityClass.Usable -> Color.Gray
                                PayamakUsabilityClass.Unknown -> Color.Magenta
                                PayamakUsabilityClass.Spam -> Color.Red
                            }
                        )
                    }
                }

                val ctx = LocalContext.current
                Button(
                    onClick = {
                        onEvent.invoke(
                            MessageDetailsEvents.OnMessageReportWrongDetected(
                                msgValue
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = ctx.getString(R.string.report_wrong_detection))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageComposablePreview() {
    MessageDebugComposable(
        ReviewableProcessedPayamak(
            ProcessedPayamakModel(
                0L,
                Payamak(0, 0L, Origin(null, "", null), ""),
                Usability(PayamakUsabilityClass.Important, UsabilityRate(0.9), listOf()),
                PayamakUsabilityClass.Important,
            ), true
        ),
        { _ -> },
        { _ -> },
        { -> },
    )
}