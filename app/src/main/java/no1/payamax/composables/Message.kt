package no1.payamax.composables

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun MessageComposable(message: ProcessedPayamakModel) {
    Column(
        modifier = Modifier.border(1.dp, color = Color.Cyan)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp), horizontalAlignment = Alignment.Start
            ) {
                val ctx = LocalContext.current
                Button(onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message.dump())
                    }
                    val shareIntent =
                        Intent.createChooser(sendIntent, "email to px@no1.ir or t.me/HBNTheGreat ")
                    ctx.startActivity(shareIntent)
                }) {
                    Icon(Icons.Rounded.Share, contentDescription = "share")
                }
                Row {
                    Text(text = message.usability.clazz.name)
                    for (clazz in UsabilityClass.values())
                        UsabilityLink(message, clazz)
                }

                Text(text = message.usability.rate.toString())
                Text(text = message.payamak.origin.number?.toString() ?: "(Unnumbered)")
                Text(text = message.payamak.origin.title ?: "(Untitled)")
                Text(text = message.payamak.origin.contact?.let { "${it.id} - ${it.name}" }
                    ?: "(Unsaved)")
                for (pr in message.usability.processResults) {
                    Text(text = "${pr.name}:${pr.rate?.toString() ?: "(Unrated)"}")
                }
            }
        }
        Text(
            text = message.payamak.body, modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .padding(5.dp), color = when (message.usability.clazz) {
                UsabilityClass.Important -> Color.Black
                UsabilityClass.Usable -> Color.Gray
                UsabilityClass.Unknown -> Color.Magenta
                UsabilityClass.Spam -> Color.Red
            }
        )
    }
}

@Composable
fun UsabilityLink(
    message: ProcessedPayamakModel,
    clazz: UsabilityClass
) {
    val background = expectedUsabilityBackground(message, clazz)
    return Text(
        text = clazz.name,
        fontWeight = expectedUsabilityWeight(message, clazz),
        fontStyle = expectedUsabilityStyle(message, clazz),
        modifier = Modifier
            .background(background)
            .padding(horizontal = 5.dp)
            .background(background)
            .clickable { message.expectedUsabilityClass = clazz }
    )
}

fun expectedUsabilityWeight(message: ProcessedPayamakModel, desired: UsabilityClass): FontWeight {
    return if (message.expectedUsabilityClass == desired) FontWeight.Bold else FontWeight.Normal
}

fun expectedUsabilityStyle(message: ProcessedPayamakModel, desired: UsabilityClass): FontStyle {
    return if (message.expectedUsabilityClass == desired) FontStyle.Normal else FontStyle.Italic
}

fun expectedUsabilityBackground(message: ProcessedPayamakModel, desired: UsabilityClass): Color {
    return if (message.expectedUsabilityClass == desired) Color.Magenta else Color.Cyan
}

@Preview(showBackground = true)
@Composable
fun MessageComposablePreview() {
    MessageComposable(
        ProcessedPayamakModel(
            0L,
            Payamak(0L, Origin(null, "", null), ""),
            Usability(UsabilityClass.Important, UsabilityRate(0.9), listOf()),
            UsabilityClass.Important
        ),
    )
}