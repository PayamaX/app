package no1.payamax.composables

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass
import no1.payamax.contracts.UsabilityRate
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak

@Composable
fun MessageComposable(
    msgValue: ReviewableProcessedPayamak,
    onSelected: (Boolean) -> Unit,
    onDesiredResultChanged: () -> Unit
) {
    val expectedState = remember { mutableStateOf(msgValue.pp.expectedUsabilityClass) }
    val selectedState = remember { mutableStateOf(msgValue.selected) }
    Column {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = msgValue.pp.payamak.origin.displayable(),
                    modifier = Modifier
                        .padding(1.dp, 1.dp)
                        .fillMaxWidth()
                        .padding(1.dp, 1.dp),
                    color = when (msgValue.pp.usability.clazz) {
                        UsabilityClass.Important -> Color.Black
                        UsabilityClass.Usable -> Color.Gray
                        UsabilityClass.Unknown -> Color.Magenta
                        UsabilityClass.Spam -> Color.Red
                    }
                )
                Text(
                    text = msgValue.pp.payamak.body,
                    modifier = Modifier
                        .padding(1.dp, 1.dp)
                        .fillMaxWidth()
                        .padding(1.dp, 1.dp),
                    color = when (msgValue.pp.usability.clazz) {
                        UsabilityClass.Important -> Color.Black
                        UsabilityClass.Usable -> Color.Gray
                        UsabilityClass.Unknown -> Color.Magenta
                        UsabilityClass.Spam -> Color.Red
                    }
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