package no1.payamax.composables

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import no1.payamax.R
import no1.payamax.contracts.PayamakUsabilityClass

@Composable
fun MainTabScreen(
    cursor: Cursor,
    contentResolver: ContentResolver,
    navController: NavHostController,
    types: List<PayamakUsabilityClass>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val exitToastMessage = stringResource(R.string.press_back_again_to_exit)

    BackHandler {
        if (selectedTab != 0) {
            selectedTab = 0
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                context.findActivity()?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, exitToastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = stringResource(R.string.nav_home)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_home),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = stringResource(R.string.nav_profile)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.nav_profile),
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> {
                FilteredPayamaksComposable(
                    cursor = cursor,
                    contentResolver = contentResolver,
                    navController = navController,
                    types = types,
                )
            }

            1 -> {
                ProfileScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
