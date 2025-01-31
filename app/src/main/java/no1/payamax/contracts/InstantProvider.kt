package no1.payamax.contracts

interface InstantProvider {
    fun epoch(key: String = ""): Long = System.currentTimeMillis()
}