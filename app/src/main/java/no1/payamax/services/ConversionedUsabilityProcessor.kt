package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class ConversionalUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.conversional)

    override fun guess(payamak: Payamak): UsabilityRate? {
        //todo
        return if (payamak.origin.title.hasValue) UsabilityRate(1.0) else null
    }
}