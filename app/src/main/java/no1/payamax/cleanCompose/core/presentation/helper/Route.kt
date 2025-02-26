package no1.payamax.cleanCompose.core.presentation.helper

import kotlinx.serialization.Serializable
import no1.payamax.cleanCompose.core.presentation.base.BaseRoute
import no1.payamax.contracts.PayamakUsabilityClass

@Serializable
object SplashRoute : BaseRoute

@Serializable
data class MessagesRoute(val messages: PayamakUsabilityClass, val unknown: PayamakUsabilityClass) :
    BaseRoute

@Serializable
data class MessageRoute(val payamakId: Long) : BaseRoute