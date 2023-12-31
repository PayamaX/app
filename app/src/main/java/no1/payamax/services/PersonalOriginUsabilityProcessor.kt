package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class PersonalOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Personal"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.number == null)
            return null
        val textual = payamak.origin.number.main.toString()
        val matches: Boolean = textual.matches(cellPattern)
        return if (matches) UsabilityRate(0.8) else null
    }
}

val cellPattern = Regex("9\\d{9}")