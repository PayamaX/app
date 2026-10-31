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

    private var initialized = false
    private val predefinedOrigins = mutableListOf<PredefinedOrigin>()

    private fun ensureOrigins(): List<PredefinedOrigin> {
        if (!initialized) {
            predefinedOrigins.clear()
            predefinedOrigins.add(PredefinedOrigin(0, null, "IRANCELL", 0.1, OriginRankSource.PayamaX, 0))
            predefinedOrigins.add(PredefinedOrigin(0, null, "SoratHesab", 1.0, OriginRankSource.PayamaX, 1))
            predefinedOrigins.addAll(repo.all())
            initialized = true
        }
        return predefinedOrigins
    }

    fun refresh() {
        initialized = false
    }

    fun hasUserOverride(payamak: Payamak): Boolean {
        val related = findRelatedOrigins(payamak)
        return related.any { it.source == OriginRankSource.User }
    }

    private fun findRelatedOrigins(payamak: Payamak): List<PredefinedOrigin> {
        val origins = ensureOrigins()
        return if (payamak.origin.title.hasValue) {
            origins.filter { it.title != null && payamak.origin.title.contentEquals(it.title, ignoreCase = true) }
        } else if (payamak.origin.number.hasValue) {
            origins.filter { it.number != null && payamak.origin.number!!.main == it.number.main }
        } else {
            listOf()
        }
    }

    override fun guess(payamak: Payamak): UsabilityRate? {
        val related = findRelatedOrigins(payamak)
        if (related.isEmpty())
            return null

        val userOrigin = related.find { it.source == OriginRankSource.User }
        if (userOrigin != null) {
            return UsabilityRate(userOrigin.rank)
        }

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
