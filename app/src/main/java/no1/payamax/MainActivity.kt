package no1.payamax

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import no1.payamax.composables.MessageScreen
import no1.payamax.composables.MessagesScreen
import no1.payamax.constants.Screen
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.ui.theme.PayamaXTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContent {
            PayamaXTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        stringResource(R.string.app_title),
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.navigate(Screen.MessagesScreen.genUrl(PayamakUsabilityClass.Spam, PayamakUsabilityClass.Unknown)) }) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = stringResource(R.string.spams)
                                        )
                                    }
                                },
                            )
                        },
                        bottomBar = {
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController,
                            startDestination = Screen.MessagesScreen.genUrl(PayamakUsabilityClass.Important, PayamakUsabilityClass.Usable),
                            Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            composable(
                                Screen.MessagesScreen.urlTemplate,
                                arguments = listOf(navArgument("types") { type = NavType.StringType })
                            ) { bse ->
                                val types = bse.arguments?.getString("types")?.split(",")
                                MessagesScreen(
                                    contentResolver,
                                    navController,
                                    types?.map { PayamakUsabilityClass.valueOf(it) } ?: listOf()
                                )
                            }
                            composable(
                                Screen.MessageScreen.urlTemplate,
                                arguments = listOf(navArgument("payamakId") { type = NavType.LongType })
                            ) { bse ->
                                MessageScreen(
                                    contentResolver,
                                    navController,
                                    bse.arguments?.getLong("payamakId") ?: throw RuntimeException("")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
