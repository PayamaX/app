package no1.payamax.contracts

@JvmInline
value class UsabilityRate(val probability: Double) {
    init {
        if (probability < 0)
            throw IllegalArgumentException("probability must be between 0.0 and 1.0")

        if (1.0 < probability)
            throw IllegalArgumentException("probability must be between 0.0 and 1.0")
    }
}