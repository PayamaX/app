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
        val list = mutableListOf<ProcessResult>()
        for (rule in rules) {
            val guess = rule.guess(payamak)
            if (guess.hasValue) {
                usabilitySum += guess!!.probability
                usabilityCount++
            }
            list.add(ProcessResult(rule.name, guess.hasValue, guess?.probability, ""))
        }
        if (usabilityCount == 0)
            return Usability(PayamakUsabilityClass.Unknown, UsabilityRate(0.0), list)
        val avg = usabilitySum / usabilityCount
        return Usability(
            when {
                avg >= 0.9 -> PayamakUsabilityClass.Important
                avg >= 0.7 -> PayamakUsabilityClass.Usable
                avg >= 0.5 -> PayamakUsabilityClass.Unknown
                else -> PayamakUsabilityClass.Spam
            },
            UsabilityRate(avg),
            list
        )
    }
}