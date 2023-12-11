package no1.payamax.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no1.payamax.model.MessageModel

@Composable
fun MessageComposable(message: MessageModel) {
    Text(
        text = message.text, modifier = Modifier
            .padding(5.dp)
            .border(1.dp, color = Color.Cyan)
    )
}