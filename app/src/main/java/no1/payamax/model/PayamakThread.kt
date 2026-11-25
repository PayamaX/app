package no1.payamax.model

import no1.payamax.contracts.Origin

data class PayamakThread(
    val key: String,
    val address: String,
    val origin: Origin,
    val latestMessage: ReviewableProcessedPayamak,
    val messages: List<ReviewableProcessedPayamak> = listOf(latestMessage),
    val messageCount: Int = messages.size
)
