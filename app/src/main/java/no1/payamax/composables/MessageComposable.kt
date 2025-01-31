package no1.payamax.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.contracts.InstantProvider
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.utils.moment

@Composable
fun MessageComposable(
    msgValue: ReviewableProcessedPayamak,
    instantProvider: InstantProvider,
    onSelected: (Boolean) -> Unit,
    onClicked: () -> Unit,
    onDesiredResultChanged: () -> Unit
) {
    val expectedState = remember { mutableStateOf(msgValue.pp.expectedPayamakUsabilityClass) }
    val selectedState = remember { mutableStateOf(msgValue.selected) }
    Column(modifier = Modifier.clickable { onClicked() }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = msgValue.pp.payamak.origin.displayable(),
                        modifier = Modifier
                            .padding(1.dp, 1.dp),
                        color = msgValue.pp.usability.clazz.color
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        text = msgValue.pp.payamak.received.moment(instantProvider),
                        modifier = Modifier
                            .padding(1.dp, 1.dp),
                        color = msgValue.pp.usability.clazz.color
                    )
                }
                Text(
                    text = msgValue.pp.payamak.body,
                    modifier = Modifier
                        .padding(1.dp, 1.dp)
                        .fillMaxWidth()
                        .padding(1.dp, 1.dp),
                    color = msgValue.pp.usability.clazz.color
                )
            }
        }
    }
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Color.Gray)
    )
}