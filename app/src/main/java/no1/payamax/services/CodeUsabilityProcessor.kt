package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

val codeRegex = "code\\s*\\:\\s*\\d{1,}".toRegex(option = RegexOption.IGNORE_CASE)

class CodeUsabilityProcessor : PayamakUsabilityRuleContract {

    override val name: String
        get() = "Code"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        if (payamak.body.split("\n").any { it.matches(codeRegex) })
            return UsabilityRate(1.0)
        return null
    }
}
