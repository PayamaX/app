package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.PayamakUsabilityProcessorContract
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue
import no1.payamax.model.ProcessResult

open class UsabilityProcessorEngine(private val rules: List<PayamakUsabilityRuleContract>) :
    PayamakUsabilityProcessorContract {
    override fun detect(payamak: Payamak): Usability {
        if (rules.isEmpty()) return Usability(PayamakUsabilityClass.Unknown, UsabilityRate(0.0), listOf())
        var usabilitySum = 0.0
        var usabilityCount = 0
        var userOverrideRate: UsabilityRate? = null
        val list = mutableListOf<ProcessResult>()

        for (rule in rules) {
            val guess = rule.guess(payamak)
            if (guess.hasValue) {
                usabilitySum += guess!!.probability
                usabilityCount++
                if (rule is RankedOriginProcessor && rule.hasUserOverride(payamak)) {
                    userOverrideRate = guess
                }
            }
            list.add(ProcessResult(rule.name, guess.hasValue, guess?.probability, ""))
        }

        val finalRate = userOverrideRate?.probability
            ?: if (usabilityCount > 0) usabilitySum / usabilityCount else 0.0

        return Usability(
            when {
                finalRate >= 0.9 -> PayamakUsabilityClass.Important
                finalRate >= 0.7 -> PayamakUsabilityClass.Usable
                finalRate >= 0.5 -> PayamakUsabilityClass.Unknown
                else -> PayamakUsabilityClass.Spam
            },
            UsabilityRate(finalRate),
            list
        )
    }
}
