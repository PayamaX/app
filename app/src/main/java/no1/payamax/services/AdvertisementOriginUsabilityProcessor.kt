package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class AdvertisementOriginUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Ad"

    override fun guess(payamak: Payamak): UsabilityRate {
        if (payamak.origin.contact.hasValue) return UsabilityRate(10.0)
        val originNumber = payamak.origin.number?.run {
            val textualNumber = this.toString()
            return@run if (textualNumber.startsWith("98")) textualNumber.substring(2)
            else textualNumber
        } ?: return UsabilityRate(5.0)

        return when {
            originNumber.startsWith("1000") -> UsabilityRate(0.0)
            originNumber.startsWith("2000") -> UsabilityRate(0.0)
            originNumber.startsWith("3000") -> UsabilityRate(0.0)
            originNumber.startsWith("4000") -> UsabilityRate(0.0)
            originNumber.startsWith("5000") -> UsabilityRate(0.0)
            originNumber.startsWith("6000") -> UsabilityRate(0.0)
            originNumber.startsWith("9000") -> UsabilityRate(0.0)
            originNumber.startsWith("21") -> UsabilityRate(0.0)
            originNumber.startsWith("26") -> UsabilityRate(0.0)
            originNumber.startsWith("999") -> UsabilityRate(0.0)
            else -> UsabilityRate(7.0)
        }
    }
}