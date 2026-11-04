package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate


class CodeUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {

    override val name: String
        get() = context.getString(R.string.code)

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        if (payamak.body.split("\n").any { it.matches(codeRegex) })
            return UsabilityRate(1.0)
        return null
    }

    val codeRegex = "\\w+\\s*:\\s*\\d+".toRegex(option = RegexOption.IGNORE_CASE)
}
