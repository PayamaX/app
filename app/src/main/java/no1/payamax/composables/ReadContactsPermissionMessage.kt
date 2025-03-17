package no1.payamax.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import no1.payamax.R
import no1.payamax.cleanCompose.core.common.components.PayamaxTopAppBar
import no1.payamax.cleanCompose.core.common.components.PayamaxTopAppBarData
import no1.payamax.cleanCompose.core.presentation.ui.theme.Spacing

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReadContactsPermissionMessage(ps: PermissionState) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(topBar = {
        PayamaxTopAppBar(
            scrollBehavior = scrollBehavior,
            payamaxTopAppBarData = PayamaxTopAppBarData(
                title = "دسترسی مجوز مخاطبین",
                showBackIcon = false,
                onBackClick = {})
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val textToShow = if (ps.status.shouldShowRationale) {
                stringResource(R.string.contact_permission_denied)
            } else {
                stringResource(R.string.contact_permission_comment)
            }
            Text(textToShow, modifier = Modifier.padding(Spacing.space8))
            Button(onClick = { ps.launchPermissionRequest() }) {
                Text(stringResource(R.string.contact_permission_command))
            }
        }
    }
}