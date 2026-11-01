package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class TitledOriginUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.titled)

    override fun guess(payamak: Payamak): UsabilityRate? {
        return if (payamak.origin.title.hasValue) UsabilityRate(0.5) else null
    }
}