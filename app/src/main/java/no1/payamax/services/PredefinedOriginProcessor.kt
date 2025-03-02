package no1.payamax.services

import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.contracts.cellNumber
import no1.payamax.db.PredefinedOriginEntity
import no1.payamax.hasValue
import no1.payamax.repos.PredefinedOriginRepo

class PredefinedOriginProcessor(val repo: PredefinedOriginRepo) : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Origin Rank"

    private val predefinedOrigins = mutableListOf(
        PredefinedOrigin(0, null, "IRANCELL", 0.1, OriginRankSource.PayamaX, 0),
        PredefinedOrigin(0, null, "SoratHesab", 1.0, OriginRankSource.PayamaX, 1),
    )

    private fun ensureOrigins(): List<PredefinedOrigin> {
        if (predefinedOrigins.isEmpty()) {
            predefinedOrigins.addAll(repo.all())
        }
        return predefinedOrigins
    }

    override fun guess(payamak: Payamak): UsabilityRate? {
        val related: List<PredefinedOrigin> = if (payamak.origin.title.hasValue) {
            ensureOrigins().filter { payamak.origin.title.contentEquals(it.title!!, false) }
        } else if (payamak.origin.number.hasValue) {
            ensureOrigins().filter { payamak.origin.number!! == it.number }
        } else {
            listOf()
        }
        if (related.isEmpty())
            return null
        return UsabilityRate(related.sumOf { it.rank } / related.count())
    }
}

data class PredefinedOrigin(
    val id: Long,
    val number: CellNumber?,
    val title: String?,
    val rank: Double,
    val source: OriginRankSource,
    val position: Long
)

fun PredefinedOriginEntity.prepared(): PredefinedOrigin {
    return PredefinedOrigin(
        this.id,
        this.number?.cellNumber(),
        this.title,
        this.rank,
        this.source,
        this.position
    )
}

enum class OriginRankSource {
    PayamaX,
    User
}
