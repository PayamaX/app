package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityProcessorContract
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityClass

class UsabilityProcessorEngine(private val rules: List<PayamakUsabilityRuleContract>) :
    PayamakUsabilityProcessorContract {
    override fun detect(payamak: Payamak): UsabilityClass {
        if (rules.isEmpty())
            return UsabilityClass.Unknown
        var usabilitySum = 0.0
        var usabilityCount = 0
        for (rule in rules) {
            usabilitySum += rule.guess(payamak).probability
            usabilityCount++
        }
        val avg = usabilitySum / usabilityCount
        return when {
            avg > 9 -> UsabilityClass.Important
            avg > 7 -> UsabilityClass.Usable
            avg > 5 -> UsabilityClass.Unknown
            else -> UsabilityClass.Spam
        }
    }
}