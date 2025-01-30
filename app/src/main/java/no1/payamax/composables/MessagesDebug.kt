package no1.payamax.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import no1.payamax.hasValue
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.vm.MessagesViewModel

@Composable
fun MessagesDebugComposable(viewModel: MessagesViewModel) {
    val messages = remember {
        viewModel.msgs
    }
    val statusState = remember {
        mutableStateOf(Stats(messages))
    }
    val selectionState = remember {
        mutableIntStateOf(0)
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column {
            Text(text = "Applicability: ${statusState.value.applicability?.let { (it * 100).toString() + "%" } ?: "Unknown"}")
            Text(text = "Number of correct detections: ${statusState.value.trueDetectedCount}")
            Text(text = "Number of incorrect detections: ${statusState.value.falseDetections}")
            Text(text = "Number of determined: ${statusState.value.undeterminedCount}")
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f)
            ) {
                items(messages) { message ->
                    MessageDebugComposable(msgValue = message,
                        { selected -> selectionState.intValue += (if (selected) 1 else -1) },
                        { statusState.value = Stats(messages) })
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
            val ctx = LocalContext.current
            Button(enabled = selectionState.intValue > 0, onClick = {
                viewModel.share(ctx)
            }) {
                Icon(Icons.Rounded.Share, contentDescription = "share")
            }
        }
    }
}

data class Stats(val trueDetectedCount: Int, val falseDetections: Int, val undeterminedCount: Int) {
    val applicability: Double?
        get() {
            if (trueDetectedCount + falseDetections == 0) return null
            return trueDetectedCount.toDouble() / (trueDetectedCount + falseDetections)
        }

    constructor(msgs: List<ReviewableProcessedPayamak>) : this(
        msgs.count { it.pp.expectedUsabilityClass.hasValue && it.pp.expectedUsabilityClass == it.pp.usability.clazz },
        msgs.count { it.pp.expectedUsabilityClass.hasValue && it.pp.expectedUsabilityClass != it.pp.usability.clazz },
        msgs.count { it.pp.expectedUsabilityClass == null },
    )
}


@Preview(showBackground = true)
@Composable
fun MessagesDebugComposablePreview() {
    MessagesDebugComposable(
        viewModel = MessagesViewModel(
            listOf()
        )
    )
}