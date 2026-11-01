package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate



class CancelableUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {

    override val name: String
        get() = context.getString(R.string.cancelable)

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        val lastLine = payamak.body.trim().split("\n").last()
        if (laqv.any { lastLine.contains(it) } && eleven.any { lastLine.contains(it) })
            return UsabilityRate(0.1)
        return null
    }

    @Suppress("SpellCheckingInspection")
    val laqv = listOf("لغو")
    val eleven = listOf("۱۱", "11", "١١")
}
