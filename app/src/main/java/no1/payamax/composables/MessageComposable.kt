package no1.payamax.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension.infoStroke
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension.onBackgroundContainer
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension.onBackgroundText
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension.primary500
import no1.payamax.cleanCompose.core.presentation.ui.theme.ColorSchemeExtension.staticBlack
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.contracts.InstantProvider
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.utils.moment

@Composable
fun MessageComposable(
    msgValue: ReviewableProcessedPayamak,
    instantProvider: InstantProvider,
    onSelected: (Boolean) -> Unit,
    onClicked: (Long) -> Unit,
    onDesiredResultChanged: () -> Unit
) {
    //val expectedState = remember { mutableStateOf(msgValue.pp.expectedPayamakUsabilityClass) }
    //val selectedState = remember { mutableStateOf(msgValue.selected) }
    Column(modifier = Modifier.clickable { onClicked(msgValue.pp.id) }) {
        PayamaxTheme {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .border(1.5.dp, Color(0xFFE1E1E1), shape = RoundedCornerShape(8.dp))
                    .fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = msgValue.pp.payamak.origin.displayable(),
                        modifier = Modifier
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary500,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.staticBlack
                    )

                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        text = msgValue.pp.payamak.received.moment(instantProvider),
                        modifier = Modifier
                            .padding(1.dp, 1.dp),
//                            color = msgValue.pp.usability.clazz.color
                        color = MaterialTheme.colorScheme.onBackgroundText
                    )
                    Text(
                        text = msgValue.pp.payamak.body,
                        modifier = Modifier
                            .padding(1.dp, 1.dp)
                            .fillMaxWidth()
                            .padding(1.dp, 1.dp),
//                        color = msgValue.pp.usability.clazz.color
                        color = MaterialTheme.colorScheme.onBackgroundText
                    )
                }
            }
        }
    }
}