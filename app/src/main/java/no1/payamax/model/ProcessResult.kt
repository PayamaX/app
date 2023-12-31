package no1.payamax.model

data class ProcessResult(
    val name: String,
    val related: Boolean,
    val rate: Double?,
    val message: String
)