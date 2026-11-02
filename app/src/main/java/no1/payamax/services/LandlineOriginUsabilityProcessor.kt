package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class LandlineOriginUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.landline)

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.origin.number == null)
            return null
        val textual = payamak.origin.number.main.toString()
        if (textual.startsWith("21"))
            return UsabilityRate(0.0)
        if (textual.startsWith("26"))
            return UsabilityRate(0.0)
        return null
    }
}