package no1.payamax.composables

import android.content.ContentResolver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import no1.payamax.R
import no1.payamax.cleanCompose.core.presentation.helper.AppRoute.MessageRoute
import no1.payamax.cleanCompose.core.presentation.ui.theme.spacing
import no1.payamax.services.InstantProviderService
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.vm.ThreadConversationViewModel
import org.koin.java.KoinJavaComponent.inject

@Composable
fun ThreadConversationScreen(
    contentResolver: ContentResolver,
    navController: NavHostController,
    address: String,
    title: String
) {
    val engine by inject<UsabilityProcessorEngine>(UsabilityProcessorEngine::class.java)
    val viewModel by inject<ThreadConversationViewModel>(ThreadConversationViewModel::class.java)

    DisposableEffect(navController.currentBackStackEntry) {
        for (i in viewModel.messages.indices) {
            val oldMsg = viewModel.messages[i]
            val updatedUsability = engine.detect(oldMsg.pp.payamak)
            if (updatedUsability != oldMsg.pp.usability || oldMsg.pp.expectedPayamakUsabilityClass != null) {
                val newMsg = oldMsg.copy(
                    pp = oldMsg.pp.copy(
                        usability = updatedUsability,
                        expectedPayamakUsabilityClass = null
                    )
                )
                viewModel.refreshMessage(newMsg)
            }
        }
        onDispose { }
    }

    EnsurePermissions {
        LaunchedEffect(address, title) {
            viewModel.loadThread(contentResolver, address, title, engine)
        }
    }

    val cachedContact = viewModel.cachedContact
    val firstOrigin = viewModel.messages.firstOrNull()?.pp?.payamak?.origin
    val titleText = cachedContact?.name ?: (if (title.isNotBlank()) title else firstOrigin?.displayable() ?: address)
    val photoUri = cachedContact?.photoUri ?: firstOrigin?.contact?.photoUri
    val spacing = MaterialTheme.spacing
    val listState = rememberLazyListState()

    PayamaxScaffold(
        title = titleText,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.nav_back),
        onNavigationIconClick = { navController.navigateUp() },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = spacing.medium)
            ) {
                if (!photoUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = titleText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = spacing.small)
            ) {
                items(viewModel.messages) { msg ->
                    MessageComposable(
                        msgValue = msg,
                        instantProvider = InstantProviderService(),
                        onSelected = {},
                        onClicked = { id -> navController.navigate(MessageRoute(id)) },
                        onDesiredResultChanged = {}
                    )
                }
            }

            InfiniteListHandler(listState = listState) {
                if (viewModel.hasMore) {
                    viewModel.loadNextPage(contentResolver, address, title, engine)
                }
            }
        }
    }
}
