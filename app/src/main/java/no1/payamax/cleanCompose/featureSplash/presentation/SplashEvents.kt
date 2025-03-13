package no1.payamax.cleanCompose.featureSplash.presentation

import no1.payamax.cleanCompose.core.presentation.base.BaseEvent
import no1.payamax.contracts.PayamakUsabilityClass

sealed class SplashEvents : BaseEvent() {
    data class OnSplashShown(val isSplashShown: Boolean) : SplashEvents()

    data class NavigateToMessages(val types: List<PayamakUsabilityClass>) : SplashEvents()

}