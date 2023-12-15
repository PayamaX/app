package no1.payamax.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.contracts.UsabilityClass
import no1.payamax.model.MessageModel

@Composable
fun MessageComposable(message: MessageModel) {
    Column {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row {
                Text(text = message.usabilityClass.name)
                Spacer(modifier = Modifier.padding(horizontal = 10.dp))
                Text(text = message.origin)
            }
        }
        Text(
            text = message.text,
            modifier = Modifier
                .padding(5.dp)
                .border(1.dp, color = Color.Cyan)
                .fillMaxWidth()
                .padding(5.dp),
            color = when (message.usabilityClass) {
                UsabilityClass.Important -> Color.Black
                UsabilityClass.Usable -> Color.Gray
                UsabilityClass.Unknown -> Color.Magenta
                UsabilityClass.Spam -> Color.Red
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageComposablePreview() {
    MessageComposable(
        MessageModel(
            0L,
            "9123456789",
            "Important Message",
            "",
            "",
            UsabilityClass.Important
        ),
    )
}