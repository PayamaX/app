package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class ThousandPrefixedOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "ThousandPrefixedOriginUsabilityProcessor"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.number == null)
            return null
        val originNumber = payamak.origin.number.main.toString()
        return when {
            originNumber.startsWith("1000") -> UsabilityRate(0.0)
            originNumber.startsWith("2000") -> UsabilityRate(0.0)
            originNumber.startsWith("3000") -> UsabilityRate(0.0)
            originNumber.startsWith("4000") -> UsabilityRate(0.0)
            originNumber.startsWith("5000") -> UsabilityRate(0.0)
            originNumber.startsWith("6000") -> UsabilityRate(0.0)
            originNumber.startsWith("9000") -> UsabilityRate(0.0)
            else -> null
        }
    }
}