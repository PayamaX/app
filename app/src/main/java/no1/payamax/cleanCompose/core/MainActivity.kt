package no1.payamax.cleanCompose.core

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import no1.payamax.BuildConfig
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.ComposeMessageRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.OnboardingRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.SplashRoute
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.ThreadConversationRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.PayamaxTheme
import no1.payamax.cleanCompose.featureSplash.presentation.SplashScreen
import no1.payamax.composables.ComposeMessageScreen
import no1.payamax.composables.MessageScreen
import no1.payamax.composables.MessagesScreen
import no1.payamax.composables.OnboardingScreen
import no1.payamax.composables.ThreadConversationScreen
import no1.payamax.services.SmsNotificationManager
import no1.payamax.utils.AppLanguage
import no1.payamax.utils.UserPreferences
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val userPreferences by inject<UserPreferences>(UserPreferences::class.java)
    private var pendingNotificationTarget by mutableStateOf<Pair<String, String>?>(null)

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("payamax_user_prefs", Context.MODE_PRIVATE)
        val langName = prefs.getString("key_app_language", AppLanguage.PERSIAN.name) ?: AppLanguage.PERSIAN.name
        val lang = try {
            AppLanguage.valueOf(langName)
        } catch (e: Exception) {
            AppLanguage.PERSIAN
        }
        val locale = Locale(lang.code)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        extractNotificationIntent(intent)
        setupCompose()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationIntent(intent)
    }

    private fun extractNotificationIntent(intent: Intent?) {
        val address = intent?.getStringExtra(SmsNotificationManager.EXTRA_NOTIFICATION_ADDRESS)
        if (!address.isNullOrBlank()) {
            val title = intent.getStringExtra(SmsNotificationManager.EXTRA_NOTIFICATION_TITLE) ?: address
            pendingNotificationTarget = address to title
            intent.removeExtra(SmsNotificationManager.EXTRA_NOTIFICATION_ADDRESS)
            intent.removeExtra(SmsNotificationManager.EXTRA_NOTIFICATION_TITLE)
        }
    }

    private fun setupCompose() {
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState()
            val appLanguage by userPreferences.appLanguage.collectAsState()

            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val locale = remember(appLanguage) { Locale(appLanguage.code) }
            val localizedConfiguration = remember(configuration, locale) {
                val config = Configuration(configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                config
            }
            val localizedContext = remember(context, localizedConfiguration, locale) {
                val configContext = context.createConfigurationContext(localizedConfiguration)
                object : android.content.ContextWrapper(context) {
                    override fun getResources(): android.content.res.Resources = configContext.resources
                }
            }
            val layoutDirection = remember(locale) {
                if (locale.language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
                LocalLayoutDirection provides layoutDirection
            ) {
                PayamaxTheme(themeMode = themeMode) {
                    val navController = rememberNavController()
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()

                    LaunchedEffect(pendingNotificationTarget, currentBackStackEntry) {
                        val target = pendingNotificationTarget ?: return@LaunchedEffect
                        val currentRoute = currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
                        if (!currentRoute.contains("SplashRoute") && !currentRoute.contains("OnboardingRoute")) {
                            pendingNotificationTarget = null
                            navController.navigate(
                                ThreadConversationRoute(
                                    address = target.first,
                                    title = target.second
                                )
                            )
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = SplashRoute,
                    ) {
                        composable<SplashRoute> {
                            SplashScreen(navController = navController)
                        }

                        composable<OnboardingRoute>(
                            enterTransition = { fadeIn(animationSpec = tween(420)) },
                            exitTransition = { fadeOut(animationSpec = tween(320)) }
                        ) {
                            OnboardingScreen(navController = navController)
                        }

                        composable<MessagesRoute>(
                            enterTransition = { fadeIn(animationSpec = tween(360)) }
                        ) { entry ->
                            val route = entry.toRoute<MessagesRoute>()
                            MessagesScreen(
                                contentResolver,
                                navController,
                                route.messagesClass
                            )
                        }

                        composable<ThreadConversationRoute> { entry ->
                            val route = entry.toRoute<ThreadConversationRoute>()
                            ThreadConversationScreen(
                                contentResolver,
                                navController,
                                route.address,
                                route.title,
                                route.autoFocusInput
                            )
                        }

                        composable<ComposeMessageRoute> {
                            ComposeMessageScreen(
                                contentResolver = contentResolver,
                                navController = navController
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
