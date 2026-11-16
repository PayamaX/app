package no1.payamax.model

import no1.payamax.contracts.Origin

data class PayamakThread(
    val key: String,
    val address: String,
    val origin: Origin,
    val messages: List<ReviewableProcessedPayamak>,
    val latestMessage: ReviewableProcessedPayamak,
    val messageCount: Int
)
