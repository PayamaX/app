package no1.payamax.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no1.payamax.contracts.InstantProvider
import no1.payamax.vm.MessagesViewModel

@Composable
fun MessagesComposable(viewModel: MessagesViewModel, instantProvider: InstantProvider, navController: NavHostController) {
    val messages = remember {
        viewModel.msgs
    }
    val statusState = remember {
        mutableStateOf(Stats(messages))
    }
    val selectionState = remember {
        mutableIntStateOf(0)
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column {
            LazyColumn(
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f)
            ) {
                items(messages) { message ->
                    /* MessageComposable(
                         msgValue = message,
                         instantProvider,
                         { selected -> selectionState.intValue += (if (selected) 1 else -1) },
                         { navController.navigate(Screen.MessageScreen.genUrl(message.pp.payamak.id)) },
                         { statusState.value = Stats(messages) }
                     )*/
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}