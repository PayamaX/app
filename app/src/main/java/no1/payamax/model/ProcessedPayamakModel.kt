package no1.payamax.model

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.Usability

data class ProcessedPayamakModel(
    val id: Long,
    val payamak: Payamak,
    val usability: Usability,
    var expectedPayamakUsabilityClass: PayamakUsabilityClass? = null,
) {
    fun dump(): String {
        val output = StringBuffer()
        output.append("payamak:\n")
        output.append(payamak.dump("\t"))
        output.append("\n")
        output.append(usability.dump("\t"))
        output.append("\n")
        output.append("expectedUsabilityClass: $expectedPayamakUsabilityClass\n")
        return output.toString()
    }
}