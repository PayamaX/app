package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityProcessorContract
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass
import no1.payamax.contracts.UsabilityRate

class UsabilityProcessorEngine(private val rules: List<PayamakUsabilityRuleContract>) :
    PayamakUsabilityProcessorContract {
    override fun detect(payamak: Payamak): Usability {
        if (rules.isEmpty()) return Usability(UsabilityClass.Unknown, UsabilityRate(0.0))
        var usabilitySum = 0.0
        var usabilityCount = 0
        for (rule in rules) {
            usabilitySum += rule.guess(payamak).probability
            usabilityCount++
        }
        val avg = usabilitySum / usabilityCount
        return Usability(
            when {
                avg >= 9 -> UsabilityClass.Important
                avg >= 7 -> UsabilityClass.Usable
                avg >= 5 -> UsabilityClass.Unknown
                else -> UsabilityClass.Spam
            }, UsabilityRate(avg)
        )
    }
}