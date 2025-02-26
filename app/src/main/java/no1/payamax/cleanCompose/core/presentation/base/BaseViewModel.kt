package no1.payamax.cleanCompose.core.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class BaseViewModel<E : BaseState>(
//    val app: App,
    state: E,
) : ViewModel() {
    
    @Suppress("PropertyName")
    protected val _uiState = MutableStateFlow(state)
    val uiState: StateFlow<E> = _uiState.asStateFlow()
    
    @Suppress("PropertyName")
    protected val _navigation = MutableStateFlow<BaseRoute?>(null)
    val navigation: StateFlow<BaseRoute?> get() = _navigation.asStateFlow()
    
}