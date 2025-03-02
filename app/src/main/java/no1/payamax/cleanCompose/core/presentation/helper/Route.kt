package no1.payamax.cleanCompose.core.presentation.helper

import kotlinx.serialization.Serializable
import no1.payamax.contracts.PayamakUsabilityClass

@Serializable
sealed class AppRoute {
    @Serializable
    object SplashRoute : AppRoute()

    @Serializable
    data class MessagesRoute(val messagesClass:List<PayamakUsabilityClass>) : AppRoute()

//    @Serializable
//    data class MessagesRoute(val types: List<PayamakUsabilityClass>) : AppRoute()

    @Serializable
    data class MessageRoute(val payamakId: Long) : AppRoute()
}
