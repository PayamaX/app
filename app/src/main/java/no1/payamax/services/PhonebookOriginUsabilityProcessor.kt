package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class PhonebookOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Phonebook"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.contact.hasValue)
            return UsabilityRate(1.0)
        return null
    }
}