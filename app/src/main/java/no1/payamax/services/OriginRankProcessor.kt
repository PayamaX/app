package no1.payamax.services

import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.hasValue

class OriginRankProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Origin Rank"

    private val predefinedOrigins = listOf(
        PredefinedOrigin(0, null, "IRANCELL", 0.1, OriginRankSource.PayamaX, 0),
        PredefinedOrigin(0, null, "SoratHesab", 1.0, OriginRankSource.PayamaX, 1),
    )

    override fun guess(payamak: Payamak): UsabilityRate? {
        val related: List<PredefinedOrigin> = if (payamak.origin.title.hasValue) {
            predefinedOrigins.filter { payamak.origin.title.contentEquals(it.title!!, false) }
        } else if (payamak.origin.number.hasValue) {
            predefinedOrigins.filter { payamak.origin.number!! == it.number }
        } else {
            listOf()
        }
        if (related.isEmpty())
            return null
        return UsabilityRate(related.sumOf { it.rank } / related.count())
    }
}

data class PredefinedOrigin(
    val id: Int,
    val number: CellNumber?,
    val title: String?,
    val rank: Double,
    val source: OriginRankSource,
    val position: Int
)

enum class OriginRankSource {
    PayamaX,
    User
}
