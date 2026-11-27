package no1.payamax.composables

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessagesRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.services.SmsNotificationManager
import no1.payamax.utils.DefaultSmsHelper
import no1.payamax.utils.NotificationPermissionHelper
import no1.payamax.utils.NotificationPermissionSession
import no1.payamax.utils.UserPreferences
import no1.payamax.utils.toPersianDigits
import org.koin.java.KoinJavaComponent
import kotlin.math.absoluteValue

private const val ONBOARDING_PAGE_COUNT = 4

@Composable
fun OnboardingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.spacing

    val userPreferences = remember { KoinJavaComponent.get<UserPreferences>(UserPreferences::class.java) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ONBOARDING_PAGE_COUNT }
    )

    fun checkContactsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkSmsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    var hasContactsPermission by remember { mutableStateOf(checkContactsGranted()) }
    var hasSmsPermission by remember { mutableStateOf(checkSmsGranted()) }
    var hasNotifPermission by remember {
        mutableStateOf(NotificationPermissionHelper.hasNotificationPermission(context))
    }
    var isDefaultSmsApp by remember { mutableStateOf(DefaultSmsHelper.isDefaultSmsApp(context)) }

    var contactsRequestCount by remember { mutableIntStateOf(0) }
    var smsNotifRequestCount by remember { mutableIntStateOf(0) }

    fun refreshAllStatuses() {
        hasContactsPermission = checkContactsGranted()
        hasSmsPermission = checkSmsGranted()
        hasNotifPermission = NotificationPermissionHelper.hasNotificationPermission(context)
        isDefaultSmsApp = DefaultSmsHelper.isDefaultSmsApp(context)
        if (isDefaultSmsApp) {
            SmsNotificationManager.ensureChannel(context)
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshAllStatuses()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    suspend fun animateToPageSmoothly(targetPage: Int) {
        val clamped = targetPage.coerceIn(0, ONBOARDING_PAGE_COUNT - 1)
        pagerState.animateScrollToPage(
            page = clamped,
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    fun completeOnboardingAndEnter() {
        if (!hasNotifPermission) {
            NotificationPermissionSession.skippedForSession = true
        }
        userPreferences.setDismissDefaultSmsPrompt(neverShowAgain = true)
        userPreferences.setOnboardingCompleted(true)
        navController.navigate(
            MessagesRoute(
                listOf(
                    PayamakUsabilityClass.Important,
                    PayamakUsabilityClass.Usable
                )
            )
        ) {
            popUpTo(0) { inclusive = true }
        }
    }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            animateToPageSmoothly(pagerState.currentPage - 1)
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        contactsRequestCount += 1
        refreshAllStatuses()
        if (granted || hasContactsPermission) {
            scope.launch {
                delay(320L)
                animateToPageSmoothly(2)
            }
        }
    }

    val smsAndNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        smsNotifRequestCount += 1
        refreshAllStatuses()
        if (hasSmsPermission && hasNotifPermission) {
            scope.launch {
                delay(320L)
                animateToPageSmoothly(3)
            }
        }
    }

    val defaultSmsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAllStatuses()
        if (DefaultSmsHelper.isDefaultSmsApp(context)) {
            scope.launch {
                delay(350L)
                completeOnboardingAndEnter()
            }
        }
    }

    // Gentle ambient breathing halo for hero emblems
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_halo")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Ambient Decorative Gradient Orbs shifting smoothly across pages (read in draw phase)
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-70).dp, y = (-60).dp)
                .graphicsLayer {
                    val progress = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    translationX = -progress * 42.dp.toPx()
                    translationY = (progress % 2f) * 24.dp.toPx()
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .graphicsLayer {
                    val progress = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    translationX = progress * 36.dp.toPx()
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Top Navigation & Step Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterStart) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn(tween(220)) + scaleIn(tween(220)),
                        exit = fadeOut(tween(180)) + scaleOut(tween(180))
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    animateToPageSmoothly(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.nav_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                val rawStepText = stringResource(
                    R.string.onboarding_step_indicator,
                    pagerState.currentPage + 1,
                    ONBOARDING_PAGE_COUNT
                )
                val stepText = if (layoutDirection == LayoutDirection.Rtl) {
                    rawStepText.toPersianDigits()
                } else {
                    rawStepText
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Box(contentAlignment = Alignment.CenterEnd) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = pagerState.currentPage < ONBOARDING_PAGE_COUNT - 1,
                        enter = fadeIn(tween(220)),
                        exit = fadeOut(tween(160))
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    animateToPageSmoothly(ONBOARDING_PAGE_COUNT - 1)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_skip),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Animated Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val rtlMultiplier = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                val parallaxProvider: () -> Float = {
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction) * rtlMultiplier
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val rawPageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            val absOffset = rawPageOffset.absoluteValue.coerceIn(0f, 1f)
                            alpha = lerp(1f, 0.25f, absOffset)
                            val scale = lerp(1f, 0.90f, absOffset)
                            scaleX = scale
                            scaleY = scale
                            translationY = absOffset * 28.dp.toPx()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = spacing.large, vertical = spacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (page) {
                            0 -> WelcomeOnboardingPage(
                                haloScale = haloScale,
                                parallaxProvider = parallaxProvider
                            )

                            1 -> ContactsOnboardingPage(
                                isGranted = hasContactsPermission,
                                haloScale = haloScale,
                                parallaxProvider = parallaxProvider
                            )

                            2 -> SmsAndNotifOnboardingPage(
                                isSmsGranted = hasSmsPermission,
                                isNotifGranted = hasNotifPermission,
                                haloScale = haloScale,
                                parallaxProvider = parallaxProvider
                            )

                            3 -> DefaultSmsOnboardingPage(
                                isDefaultSms = isDefaultSmsApp,
                                haloScale = haloScale,
                                parallaxProvider = parallaxProvider
                            )
                        }
                    }
                }
            }

            // Bottom Controls: Expanding Pill Indicators + Primary & Secondary Actions
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = spacing.large, vertical = spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Worm/Pill Page Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = spacing.medium)
                ) {
                    repeat(ONBOARDING_PAGE_COUNT) { index ->
                        val isSelected = pagerState.currentPage == index
                        val indicatorWidth by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "dot_width"
                        )
                        val indicatorColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            animationSpec = tween(250),
                            label = "dot_color"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(indicatorWidth)
                                .clip(CircleShape)
                                .background(indicatorColor)
                        )
                    }
                }

                val currentPage = pagerState.currentPage
                val contactsNeedsSettings = !hasContactsPermission &&
                    contactsRequestCount > 0 &&
                    !canShowRationale(context, Manifest.permission.READ_CONTACTS)

                val smsNeedsSettings = !hasSmsPermission &&
                    smsNotifRequestCount > 0 &&
                    !canShowRationale(context, Manifest.permission.READ_SMS)

                val primaryButtonLabel = when (currentPage) {
                    0 -> stringResource(R.string.onboarding_get_started)
                    1 -> when {
                        hasContactsPermission -> stringResource(R.string.onboarding_next)
                        contactsNeedsSettings -> stringResource(R.string.onboarding_open_app_settings)
                        else -> stringResource(R.string.onboarding_contacts_action)
                    }
                    2 -> when {
                        hasSmsPermission && hasNotifPermission -> stringResource(R.string.onboarding_next)
                        smsNeedsSettings -> stringResource(R.string.onboarding_open_app_settings)
                        !hasSmsPermission && !hasNotifPermission -> stringResource(R.string.onboarding_sms_notif_action)
                        !hasSmsPermission -> stringResource(R.string.onboarding_sms_only_action)
                        else -> stringResource(R.string.onboarding_notif_only_action)
                    }
                    else -> if (isDefaultSmsApp) {
                        stringResource(R.string.onboarding_enter_app)
                    } else {
                        stringResource(R.string.onboarding_default_action)
                    }
                }

                Button(
                    onClick = {
                        when (currentPage) {
                            0 -> {
                                scope.launch { animateToPageSmoothly(1) }
                            }

                            1 -> {
                                when {
                                    hasContactsPermission -> {
                                        scope.launch { animateToPageSmoothly(2) }
                                    }
                                    contactsNeedsSettings -> {
                                        openAppDetailsSettings(context)
                                    }
                                    else -> {
                                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                    }
                                }
                            }

                            2 -> {
                                when {
                                    hasSmsPermission && hasNotifPermission -> {
                                        scope.launch { animateToPageSmoothly(3) }
                                    }
                                    smsNeedsSettings -> {
                                        openAppDetailsSettings(context)
                                    }
                                    !hasSmsPermission -> {
                                        val perms = buildList {
                                            add(Manifest.permission.READ_SMS)
                                            add(Manifest.permission.RECEIVE_SMS)
                                            add(Manifest.permission.SEND_SMS)
                                            if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                add(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                        smsAndNotifLauncher.launch(perms.toTypedArray())
                                    }
                                    else -> {
                                        val canAskRuntime = NotificationPermissionHelper.canRequestRuntimePermission(
                                            context = context,
                                            alreadyRequestedOnce = smsNotifRequestCount > 0
                                        )
                                        if (canAskRuntime && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            smsAndNotifLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                                        } else {
                                            NotificationPermissionHelper.openNotificationSettings(context)
                                        }
                                    }
                                }
                            }

                            3 -> {
                                if (isDefaultSmsApp) {
                                    completeOnboardingAndEnter()
                                } else {
                                    val intent = DefaultSmsHelper.createRequestDefaultSmsIntent(context)
                                    if (intent != null) {
                                        try {
                                            defaultSmsLauncher.launch(intent)
                                        } catch (_: Exception) {
                                            completeOnboardingAndEnter()
                                        }
                                    } else {
                                        completeOnboardingAndEnter()
                                    }
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                ) {
                    AnimatedContent(
                        targetState = primaryButtonLabel,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        },
                        label = "primary_cta_text"
                    ) { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Contextual Secondary Skip / Later Button
                val showSecondaryButton = when (currentPage) {
                    1 -> !hasContactsPermission
                    2 -> !(hasSmsPermission && hasNotifPermission)
                    3 -> !isDefaultSmsApp
                    else -> false
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showSecondaryButton,
                        enter = fadeIn(tween(220)),
                        exit = fadeOut(tween(150))
                    ) {
                        TextButton(
                            onClick = {
                                when (currentPage) {
                                    1 -> scope.launch { animateToPageSmoothly(2) }
                                    2 -> {
                                        if (!hasNotifPermission) {
                                            NotificationPermissionSession.skippedForSession = true
                                        }
                                        scope.launch { animateToPageSmoothly(3) }
                                    }
                                    3 -> completeOnboardingAndEnter()
                                }
                            },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (currentPage == 3) {
                                    stringResource(R.string.onboarding_default_skip)
                                } else {
                                    stringResource(R.string.onboarding_skip_step)
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeOnboardingPage(
    haloScale: Float,
    parallaxProvider: () -> Float
) {
    val spacing = MaterialTheme.spacing

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(176.dp)
            .graphicsLayer {
                val parallax = parallaxProvider()
                translationX = parallax * 56.dp.toPx()
                rotationZ = -parallax * 7f
            }
    ) {
        Box(
            modifier = Modifier
                .size(168.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                }
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = CircleShape
                )
        )

        Surface(
            modifier = Modifier.size(132.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.email_spam_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-12).dp, y = (-12).dp)
                .size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing.medium))

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_badge),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }

    Spacer(modifier = Modifier.height(spacing.small + 4.dp))

    Text(
        text = stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.small))

    Text(
        text = stringResource(R.string.onboarding_welcome_desc),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.large))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small + 4.dp)
        ) {
            OnboardingFeatureRow(
                icon = Icons.Default.VerifiedUser,
                title = stringResource(R.string.onboarding_feature_smart_title),
                subtitle = stringResource(R.string.onboarding_feature_smart_desc)
            )
            OnboardingFeatureRow(
                icon = Icons.Default.NotificationsOff,
                title = stringResource(R.string.onboarding_feature_silent_title),
                subtitle = stringResource(R.string.onboarding_feature_silent_desc)
            )
            OnboardingFeatureRow(
                icon = Icons.Default.Forum,
                title = stringResource(R.string.onboarding_feature_privacy_title),
                subtitle = stringResource(R.string.onboarding_feature_privacy_desc)
            )
        }
    }
}

@Composable
private fun ContactsOnboardingPage(
    isGranted: Boolean,
    haloScale: Float,
    parallaxProvider: () -> Float
) {
    val spacing = MaterialTheme.spacing

    OnboardingHeroIcon(
        icon = Icons.Default.Contacts,
        isGranted = isGranted,
        haloScale = haloScale,
        parallaxProvider = parallaxProvider
    )

    Spacer(modifier = Modifier.height(spacing.large))

    Text(
        text = stringResource(R.string.onboarding_contacts_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.small + 2.dp))

    Text(
        text = stringResource(R.string.onboarding_contacts_desc),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.large))

    val pillColor by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "contacts_pill_bg"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = pillColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (isGranted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(spacing.small))
            Text(
                text = if (isGranted) {
                    stringResource(R.string.onboarding_contacts_granted)
                } else {
                    stringResource(R.string.onboarding_contacts_privacy_note)
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isGranted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun SmsAndNotifOnboardingPage(
    isSmsGranted: Boolean,
    isNotifGranted: Boolean,
    haloScale: Float,
    parallaxProvider: () -> Float
) {
    val spacing = MaterialTheme.spacing
    val bothGranted = isSmsGranted && isNotifGranted

    OnboardingHeroIcon(
        icon = Icons.Default.Email,
        secondaryBadgeIcon = Icons.Default.NotificationsActive,
        isGranted = bothGranted,
        haloScale = haloScale,
        parallaxProvider = parallaxProvider
    )

    Spacer(modifier = Modifier.height(spacing.medium + 4.dp))

    Text(
        text = stringResource(R.string.onboarding_sms_notif_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.small))

    Text(
        text = stringResource(R.string.onboarding_sms_notif_desc),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.large))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small + 2.dp)
    ) {
        PermissionStatusCard(
            icon = Icons.Default.Email,
            title = stringResource(R.string.onboarding_perm_sms_title),
            subtitle = stringResource(R.string.onboarding_perm_sms_desc),
            isGranted = isSmsGranted
        )

        PermissionStatusCard(
            icon = Icons.Default.NotificationsActive,
            title = stringResource(R.string.onboarding_perm_notif_title),
            subtitle = stringResource(R.string.onboarding_perm_notif_desc),
            isGranted = isNotifGranted
        )
    }
}

@Composable
private fun DefaultSmsOnboardingPage(
    isDefaultSms: Boolean,
    haloScale: Float,
    parallaxProvider: () -> Float
) {
    val spacing = MaterialTheme.spacing

    OnboardingHeroIcon(
        icon = Icons.Default.MarkChatRead,
        isGranted = isDefaultSms,
        haloScale = haloScale,
        parallaxProvider = parallaxProvider
    )

    Spacer(modifier = Modifier.height(spacing.medium + 4.dp))

    Text(
        text = stringResource(R.string.onboarding_default_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.small))

    Text(
        text = if (isDefaultSms) {
            stringResource(R.string.onboarding_default_granted)
        } else {
            stringResource(R.string.onboarding_default_desc)
        },
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
        color = if (isDefaultSms) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(spacing.large))

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small + 4.dp)
        ) {
            OnboardingSimpleBenefitRow(
                icon = Icons.Default.NotificationsActive,
                text = stringResource(R.string.onboarding_default_benefit_1)
            )
            OnboardingSimpleBenefitRow(
                icon = Icons.AutoMirrored.Filled.Send,
                text = stringResource(R.string.onboarding_default_benefit_2)
            )
            OnboardingSimpleBenefitRow(
                icon = Icons.Default.DeleteSweep,
                text = stringResource(R.string.onboarding_default_benefit_3)
            )
        }
    }
}

@Composable
private fun OnboardingHeroIcon(
    icon: ImageVector,
    isGranted: Boolean,
    haloScale: Float,
    parallaxProvider: () -> Float,
    secondaryBadgeIcon: ImageVector? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(320),
        label = "hero_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(320),
        label = "hero_tint"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(164.dp)
            .graphicsLayer {
                val parallax = parallaxProvider()
                translationX = parallax * 56.dp.toPx()
                rotationZ = -parallax * 7f
            }
    ) {
        Box(
            modifier = Modifier
                .size(156.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                }
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                    shape = CircleShape
                )
        )

        Surface(
            modifier = Modifier.size(122.dp),
            shape = CircleShape,
            color = containerColor,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = isGranted,
                    transitionSpec = {
                        (fadeIn(tween(240)) + scaleIn(initialScale = 0.7f)) togetherWith
                            (fadeOut(tween(180)) + scaleOut(targetScale = 0.7f))
                    },
                    label = "hero_icon_state"
                ) { granted ->
                    Icon(
                        imageVector = if (granted) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        if (secondaryBadgeIcon != null && !isGranted) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = secondaryBadgeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing.small + 4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OnboardingSimpleBenefitRow(
    icon: ImageVector,
    text: String
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing.small + 4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PermissionStatusCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean
) {
    val spacing = MaterialTheme.spacing
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        },
        label = "perm_card_border"
    )
    val cardBg by animateColorAsState(
        targetValue = if (isGranted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "perm_card_bg"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (isGranted) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.small + 4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(spacing.small))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (isGranted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = if (isGranted) {
                        stringResource(R.string.onboarding_perm_status_granted)
                    } else {
                        stringResource(R.string.onboarding_perm_status_required)
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isGranted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun canShowRationale(context: Context, permission: String): Boolean {
    val activity = context.findHostActivity() ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

private fun openAppDetailsSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

private fun Context.findHostActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
