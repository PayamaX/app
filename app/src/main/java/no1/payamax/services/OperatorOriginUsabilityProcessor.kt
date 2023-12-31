package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class OperatorOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Operator"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.number == null)
            return null
        val textual = payamak.origin.number.main.toString()
        val matches: Boolean = textual.matches(operatorNumberPattern)
        return if (matches) UsabilityRate(0.0) else null
    }
}

val operatorNumberPattern = Regex("\\d{3,8}")