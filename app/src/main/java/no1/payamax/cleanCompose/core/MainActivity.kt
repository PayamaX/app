package no1.payamax.cleanCompose.core

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no1.payamax.BuildConfig
import no1.payamax.cleanCompose.core.presentation.helper.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.helper.SplashRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.cleanCompose.featureSplash.presentation.SplashScreen
import no1.payamax.composables.MessagesScreen
import no1.payamax.contracts.PayamakUsabilityClass

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupCompose()

//        setContent {
//            PayamaxTheme {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(5.dp),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    val navController = rememberNavController()
//                    Scaffold(
//                        topBar = {
//                            CenterAlignedTopAppBar(
//                                title = {
//                                    Text(
//                                        stringResource(R.string.app_title),
//                                    )
//                                },
//                                navigationIcon = {
//                                    IconButton(onClick = {
//                                        navController.navigate(
//                                            Screen.MessagesScreen.genUrl(
//                                                PayamakUsabilityClass.Spam,
//                                                PayamakUsabilityClass.Unknown
//                                            )
//                                        )
//                                    }) {
//                                        Icon(
//                                            imageVector = Icons.Default.Warning,
//                                            contentDescription = stringResource(R.string.spams)
//                                        )
//                                    }
//                                },
//                            )
//                        },
//                        bottomBar = {
//                        }
//                    ) { innerPadding ->
//                        NavHost(
//                            navController,
//                            startDestination = Screen.MessagesScreen.genUrl(
//                                PayamakUsabilityClass.Important,
//                                PayamakUsabilityClass.Usable
//                            ),
//                            Modifier
//                                .padding(innerPadding)
//                                .fillMaxSize()
//                        ) {
//                            composable(
//                                Screen.MessagesScreen.urlTemplate,
//                                arguments = listOf(navArgument("types") {
//                                    type = NavType.StringType
//                                })
//                            ) { bse ->
//                                val types = bse.arguments?.getString("types")?.split(",")
//                                MessagesScreen(
//                                    contentResolver,
//                                    navController,
//                                    types?.map { PayamakUsabilityClass.valueOf(it) } ?: listOf()
//                                )
//                            }
//                            composable(
//                                Screen.MessageScreen.urlTemplate,
//                                arguments = listOf(navArgument("payamakId") {
//                                    type = NavType.LongType
//                                })
//                            ) { bse ->
//                                MessageScreen(
//                                    contentResolver,
//                                    navController,
//                                    bse.arguments?.getLong("payamakId")
//                                        ?: throw RuntimeException("")
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    private fun setupCompose() {
        enableEdgeToEdge()
        setContent {
            PayamaxTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = SplashRoute,
                ) {
                    composable<SplashRoute> {
                        SplashScreen(navController = navController)
                    }
                    composable<MessagesRoute> {
                        MessagesScreen(
                            contentResolver,
                            navController,
                            listOf(PayamakUsabilityClass.Important, PayamakUsabilityClass.Usable)
                        )
                    }
//                    composable<MessageRoute> { bse ->
//                        MessageScreen(
//                            contentResolver,
//                            navController,
//                            bse.arguments?.getLong("payamakId") ?: throw RuntimeException("")
//                        )
//                    }
                }
            }
        }
    }
}
