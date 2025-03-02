package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

@Suppress("SpellCheckingInspection")
val laqv = listOf("لغو")
val eleven = listOf("۱۱", "11", "١١")

class Cancel11UsabilityProcessor : PayamakUsabilityRuleContract {

    override val name: String
        get() = "Cancel11"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        val lastLine = payamak.body.trim().split("\n").last()
        if (laqv.any { lastLine.contains(it) } && eleven.any { lastLine.contains(it) })
            return UsabilityRate(0.1)
        return null
    }
}
