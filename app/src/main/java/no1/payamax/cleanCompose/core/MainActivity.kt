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
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.SplashRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.cleanCompose.featureSplash.presentation.SplashScreen
import no1.payamax.composables.MessageScreen
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
                    composable<MessagesRoute> { bse ->
                        val types = bse.arguments?.get("messagesClass") as List<PayamakUsabilityClass>?
                        MessagesScreen(
                            contentResolver,
                            navController,
                            types ?: throw RuntimeException("")
                        )
                    }

                    composable<MessageRoute> { bse ->
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
