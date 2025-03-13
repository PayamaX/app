package no1.payamax.cleanCompose.core

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import no1.payamax.BuildConfig
import no1.payamax.cleanCompose.core.presentation.helper.MessageDetailsRoute
import no1.payamax.cleanCompose.core.presentation.helper.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.helper.SplashRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.cleanCompose.featureSplash.presentation.SplashScreen
import no1.payamax.cleanCompose.featureSplash.presentation.SplashViewModel
import no1.payamax.cleanCompose.feutureMessagesDetails.presentation.MessageDetailsScreen
import no1.payamax.cleanCompose.feutureMessagesDetails.presentation.MessageDetailsViewModel
import no1.payamax.composables.MessagesScreen
import no1.payamax.contracts.PayamakUsabilityClass
import kotlin.getValue

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val requestCodePermission = 1234

    override fun onCreate(savedInstanceState: Bundle?) {

        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val viewModel: SplashViewModel by viewModels()

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }


        setupCompose()

        checkRunTimePermission()
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
                        val viewModel: SplashViewModel by remember { viewModels() }
                        SplashScreen(
                            uiState = viewModel.uiState.collectAsState().value,
                            onEvent = viewModel::onEvent,
                            navController = navController,
                            navDestination = viewModel.navigation.collectAsState().value
                        )
                    }

                    composable<MessagesRoute> { bse ->
                        val types =
                            bse.arguments?.get("messagesClass") as List<PayamakUsabilityClass>?
                        MessagesScreen(
                            contentResolver,
                            navController,
                            types ?: throw RuntimeException("")

                        )
                    }

                    composable<MessageDetailsRoute> { bse ->
                        val viewModel: MessageDetailsViewModel by remember { viewModels() }
                        MessageDetailsScreen(
                            contentResolver,
                            navController,
                            bse.arguments?.getLong("payamakId") ?: throw RuntimeException(""),
                            uiState = viewModel.uiState.collectAsState().value,
                            onEvent = viewModel::onEvent,
                            navDestination = viewModel.navigation.collectAsState().value
                        )
                    }
                }
            }
        }
    }

    private fun checkRunTimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val requiredPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (requiredPermissions.isNotEmpty()) {
            requestPermission(requiredPermissions)
        }
    }

    private fun requestPermission(str: MutableList<String>) {
        ActivityCompat.requestPermissions(this, str.toTypedArray(), requestCodePermission)
    }
}
