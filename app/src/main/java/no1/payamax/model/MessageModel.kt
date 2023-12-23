package no1.payamax.model

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability

data class ProcessedPayamakModel(
    val id: Long,
    val payamak: Payamak,
    val usability: Usability,
)