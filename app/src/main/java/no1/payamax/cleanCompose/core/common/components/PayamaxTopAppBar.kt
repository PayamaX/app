package no1.payamax.cleanCompose.core.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayamaxTopAppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    payamaxTopAppBarData: PayamaxTopAppBarData
) {
    PayamaxTheme {
        Surface(modifier = modifier, shadowElevation = 8.dp) {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        payamaxTopAppBarData.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)
                    )
                },
                navigationIcon = {
                    if (payamaxTopAppBarData.showBackIcon) {
                        IconButton(onClick = payamaxTopAppBarData.onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    }
                },
                actions = {
                },
                scrollBehavior = scrollBehavior,
            )
        }
    }
}

data class PayamaxTopAppBarData(
    val title: String,
    val showBackIcon: Boolean = true,
    val onBackClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PayamaxTopAppBarPreview() {
    PayamaxTopAppBar(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState()),
        payamaxTopAppBarData = PayamaxTopAppBarData(stringResource(R.string.title), true) {}
    )
}