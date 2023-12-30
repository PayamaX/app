package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityProcessorContract
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityClass
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class UsabilityProcessorEngine(private val rules: List<PayamakUsabilityRuleContract>) :
    PayamakUsabilityProcessorContract {
    override fun detect(payamak: Payamak): Usability {
        if (rules.isEmpty()) return Usability(UsabilityClass.Unknown, UsabilityRate(0.0))
        var usabilitySum = 0.0
        var usabilityCount = 0
        for (rule in rules) {
            val guess = rule.guess(payamak)
            if (guess.hasValue) {
                usabilitySum += guess!!.probability
                usabilityCount++
            }
        }
        if (usabilityCount == 0)
            return Usability(UsabilityClass.Unknown, UsabilityRate(0.0))
        val avg = usabilitySum / usabilityCount
        return Usability(
            when {
                avg >= 0.9 -> UsabilityClass.Important
                avg >= 0.7 -> UsabilityClass.Usable
                avg >= 0.5 -> UsabilityClass.Unknown
                else -> UsabilityClass.Spam
            }, UsabilityRate(avg)
        )
    }
}