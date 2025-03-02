package no1.payamax.contracts

import kotlinx.serialization.Serializable

@Serializable
enum class PayamakUsabilityClass {
    Important, Usable, Spam, Unknown
}