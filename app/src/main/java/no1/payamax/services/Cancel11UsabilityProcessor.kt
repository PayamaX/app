package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

@Suppress("SpellCheckingInspection")
val Laqv11 = listOf("لغو۱۱", "لغو11")

class Cancel11UsabilityProcessor : PayamakUsabilityRuleContract {

    override val name: String
        get() = "Cancel11"

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        if (payamak.body.trim()
                .let { text -> Laqv11.any { text.endsWith(it) } }
        ) return UsabilityRate(0.1)
        return null
    }
}
