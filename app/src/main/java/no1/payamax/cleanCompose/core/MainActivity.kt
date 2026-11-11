package no1.payamax.cleanCompose.core

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import no1.payamax.BuildConfig
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.SplashRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.cleanCompose.featureSplash.presentation.SplashScreen
import no1.payamax.composables.MessageScreen
import no1.payamax.composables.MessagesScreen
import no1.payamax.utils.UserPreferences
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val userPreferences by inject<UserPreferences>(UserPreferences::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setupCompose()
    }

    private fun setupCompose() {
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState()
            val appLanguage by userPreferences.appLanguage.collectAsState()

            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val locale = remember(appLanguage) { Locale(appLanguage.code) }
            val localizedContext = remember(context, configuration, locale) {
                val config = Configuration(configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                val configContext = context.createConfigurationContext(config)
                object : android.content.ContextWrapper(context) {
                    override fun getResources(): android.content.res.Resources = configContext.resources
                }
            }
            val layoutDirection = remember(locale) {
                if (locale.language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalLayoutDirection provides layoutDirection
            ) {
                PayamaxTheme(themeMode = themeMode) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = SplashRoute,
                    ) {
                        composable<SplashRoute> {
                            SplashScreen(navController = navController)
                        }

                        composable<MessagesRoute> { entry ->
                            val route = entry.toRoute<MessagesRoute>()
                            MessagesScreen(
                                contentResolver,
                                navController,
                                route.messagesClass
                            )
                        }

                        composable<MessageRoute> { entry ->
                            val route = entry.toRoute<MessageRoute>()
                            MessageScreen(
                                contentResolver,
                                navController,
                                route.payamakId
                            )
                        }
                    }
                }
            }
        }
    }
}
