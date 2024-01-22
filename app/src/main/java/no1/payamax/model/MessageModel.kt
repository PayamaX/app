package no1.payamax.model

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass

data class ProcessedPayamakModel(
    val id: Long,
    val payamak: Payamak,
    val usability: Usability,
    var expectedUsabilityClass: UsabilityClass? = null,
) {
    fun dump(): String {
        val output = StringBuffer()
        output.append("payamak:\n")
        output.append(payamak.dump("\t"))
        output.append("\n")
        output.append(usability.dump("\t"))
        output.append("\n")
        output.append("expectedUsabilityClass: $expectedUsabilityClass\n")
        return output.toString()
    }
}