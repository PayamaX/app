package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class PhonebookOriginUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.phonebook)

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.contact.hasValue)
            return UsabilityRate(1.0)
        return null
    }
}