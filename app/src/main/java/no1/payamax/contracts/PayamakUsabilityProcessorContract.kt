package no1.payamax.contracts

interface PayamakUsabilityProcessorContract {
    fun detect(payamak: Payamak): UsabilityClass
}