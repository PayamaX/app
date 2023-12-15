package no1.payamax.model

import no1.payamax.contracts.UsabilityClass

data class MessageModel(
    val id: Long,
    val origin: String,
    val text: String,
    val dest: String,
    val type: String,
    val usabilityClass: UsabilityClass
)