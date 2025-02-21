package no1.payamax.cleanCompose.core.presentation.helper

import kotlinx.serialization.Serializable
import no1.payamax.cleanCompose.core.presentation.base.BaseRoute
import no1.payamax.constants.Screens

@Serializable
object SplashRoute : BaseRoute

@Serializable
object MessagesRoute : BaseRoute

@Serializable
data class MessageRoute(val payamakId: Long)  : BaseRoute