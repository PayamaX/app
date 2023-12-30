package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class TitledOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "TitledOriginUsabilityProcessor"

    override fun guess(payamak: Payamak): UsabilityRate? {
        return if (payamak.origin.title.hasValue) UsabilityRate(0.5) else null
    }
}