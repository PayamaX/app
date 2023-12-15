package no1.payamax.contracts

interface PayamakUsabilityRuleContract {
    val name: String
    fun guess(payamak: Payamak): UsabilityRate
}