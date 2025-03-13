package no1.payamax.cleanCompose.featureSplash.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no1.payamax.PayamaxApp
import no1.payamax.cleanCompose.core.presentation.base.BaseViewModel
import no1.payamax.cleanCompose.core.presentation.helper.MessagesRoute
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(app: PayamaxApp) : BaseViewModel<SplashUIStates>(app ,SplashUIStates()) {

    val isLoading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            delay(2000)
            isLoading.value = false
        }
    }

    fun onEvent(event: SplashEvents) {
        when (event) {
            is SplashEvents.OnSplashShown -> {
                _uiState.update {
                    it.copy(
                        isSplashShown = event.isSplashShown
                    )
                }

            }

            is SplashEvents.NavigateToMessages -> {
                viewModelScope.launch {
                _navigation.emit(MessagesRoute(event.types))}
            }
        }
    }
}