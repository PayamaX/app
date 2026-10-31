package no1.payamax.composables

import android.content.ContentResolver
import android.database.Cursor
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                    modifier = Modifier.padding(innerPadding)
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
