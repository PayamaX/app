package no1.payamax.model

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability

data class ProcessedPayamakModel(
    val id: Long,
    val payamak: Payamak,
    val usability: Usability,
) {
    fun dump(): String {
        val output = StringBuffer()
        output.append("payamak:\n")
        output.append(payamak.dump("\t"))
        output.append(usability.dump("\t"))
        return output.toString()
    }
}