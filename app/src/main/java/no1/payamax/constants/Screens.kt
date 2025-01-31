package no1.payamax.constants

import no1.payamax.contracts.PayamakUsabilityClass

sealed interface Screen {
    object MessagesScreen : Screen {
        val urlTemplate = Screens.Messages.name + "/types={types}"
        fun genUrl(vararg types: PayamakUsabilityClass) =
            urlTemplate.replace("{types}", types.joinToString(",") { it.name })
    }

    object MessageScreen : Screen {
        val urlTemplate = "${Screens.Message.name}/{payamakId}"
        fun genUrl(payamakId: Long) = "${Screens.Message.name}/$payamakId"
    }
}


enum class Screens {
    Messages,
    Message,
    Conversations,
    Spams;
}
