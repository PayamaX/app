package no1.payamax.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import no1.payamax.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayamaxScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBar: @Composable () -> Unit = {},
    topBarHeader: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.statusBarsPadding(),
        topBar = {
            if (topBarHeader != null) {
                topBarHeader()
            } else {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        if (navigationIcon != null && onNavigationIconClick != null) {
                            IconButton(onClick = onNavigationIconClick) {
                                Icon(
                                    imageVector = navigationIcon,
                                    contentDescription = navigationIconContentDescription ?: stringResource(R.string.nav_back)
                                )
                            }
                        }
                    },
                    actions = actions
                )
            }
        },
        bottomBar = bottomBar,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        content(paddingValues)
    }
}
