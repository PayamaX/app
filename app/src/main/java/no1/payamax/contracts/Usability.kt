package no1.payamax.contracts

import no1.payamax.model.ProcessResult

data class Usability(
    val clazz: UsabilityClass,
    val rate: UsabilityRate,
    val processResults: List<ProcessResult>
)