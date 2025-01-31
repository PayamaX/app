package no1.payamax.composables

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
    onDesiredResultChanged: () -> Unit
) {
    val expectedState = remember { mutableStateOf(msgValue.pp.expectedPayamakUsabilityClass) }
    val selectedState = remember { mutableStateOf(msgValue.selected) }
    Column(
        modifier = Modifier
            .border(1.dp, color = Color.Cyan)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val ctx = LocalContext.current
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, msgValue.pp.dump())
                        }
                        val shareIntent =
                            Intent.createChooser(sendIntent, "email to px@no1.ir or t.me/DrHBN ")
                        ctx.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = "share")
                    }
                    Checkbox(checked = selectedState.value, onCheckedChange = {
                        selectedState.value = it
                        msgValue.selected = it
                        onSelected(it)
                    })
                }
                Text(text = msgValue.pp.usability.clazz.name)
                for (clazz in PayamakUsabilityClass.values()) UsabilityLink(
                    expectedState.value, clazz
                ) { y ->
                    expectedState.value = y
                    msgValue.pp.expectedPayamakUsabilityClass = expectedState.value
                    onDesiredResultChanged()
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

                Text(text = msgValue.pp.usability.rate.toString())
                Text(text = msgValue.pp.payamak.origin.number?.toString() ?: "(Unnumbered)")
                Text(text = msgValue.pp.payamak.origin.title ?: "(Untitled)")
                Text(text = msgValue.pp.payamak.origin.contact?.let { "${it.id} - ${it.name}" }
                    ?: "(Unsaved)")
                for (pr in msgValue.pp.usability.processResults) {
                    Text(text = "${pr.name}:${pr.rate?.toString() ?: "(Unrated)"}")
                }
            }
        }
    }
}

fun bgColor(p: ReviewableProcessedPayamak): Color {
    val selected = Color(Color.Magenta.red, Color.Magenta.green, Color.Magenta.blue, 0.5F)
    val unselected = Color(Color.Cyan.red, Color.Cyan.green, Color.Cyan.blue, 0.5F)
    return if (p.selected) selected else unselected
}

@Composable
fun UsabilityLink(
    current: PayamakUsabilityClass?, clazz: PayamakUsabilityClass, onClick: (clazz: PayamakUsabilityClass) -> Unit
) {
    val background = expectedUsabilityBackground(current, clazz)
    return Text(text = clazz.name,
        fontWeight = expectedUsabilityWeight(current, clazz),
        fontStyle = expectedUsabilityStyle(current, clazz),
        color = Color.Black,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(Color.Transparent)
            .padding(vertical = 2.dp)
            .background(background)
            .clickable { onClick(clazz) })
}

fun expectedUsabilityWeight(current: PayamakUsabilityClass?, desired: PayamakUsabilityClass): FontWeight {
    return if (current == desired) FontWeight.Bold else FontWeight.Normal
}

fun expectedUsabilityStyle(current: PayamakUsabilityClass?, desired: PayamakUsabilityClass): FontStyle {
    return if (current == desired) FontStyle.Normal else FontStyle.Italic
}

fun expectedUsabilityBackground(current: PayamakUsabilityClass?, desired: PayamakUsabilityClass): Color {
    return if (current == desired) Color.Magenta else Color.Cyan
}

@Preview(showBackground = true)
@Composable
fun MessageComposablePreview() {
    MessageDebugComposable(
        ReviewableProcessedPayamak(
            ProcessedPayamakModel(
                0L,
                Payamak(0,0L, Origin(null, "", null), ""),
                Usability(PayamakUsabilityClass.Important, UsabilityRate(0.9), listOf()),
                PayamakUsabilityClass.Important,
            ), true
        ),
        { _ -> },
        { -> }
    )
}