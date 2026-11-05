package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.CellNumber
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.enums.OriginRankSource
import no1.payamax.hasValue
import no1.payamax.repos.PredefinedOriginRepo

class PredefinedOriginProcessor(val repo: PredefinedOriginRepo, private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.predefinedorigin)

    private var initialized = false
    private val predefinedOrigins = mutableListOf<PredefinedOrigin>()

    private fun ensureOrigins(): List<PredefinedOrigin> {
        if (!initialized) {
            predefinedOrigins.clear()
            var index = 0L
            predefinedOrigins.add(PredefinedOrigin(1000 + index, "IRANCELL", 0.1, OriginRankSource.PayamaX, index++))
            predefinedOrigins.add(PredefinedOrigin(1000 + index, "SoratHesab", 1.0, OriginRankSource.PayamaX, index++))
            predefinedOrigins.add(PredefinedOrigin(1000 + index, CellNumber(98, 9999987641L).displayable(), 1.0, OriginRankSource.PayamaX, index++))
            predefinedOrigins.add(PredefinedOrigin(1000 + index, CellNumber(98, 5000_4747).displayable(), 1.0, OriginRankSource.PayamaX, index++))
            predefinedOrigins.add(PredefinedOrigin(1000 + index, "CRA", 1.0, OriginRankSource.PayamaX, index++))
            predefinedOrigins.add(PredefinedOrigin(1000 + index, CellNumber(98, 2020).displayable(), 1.0, OriginRankSource.PayamaX, index++))
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
            origins.filter { payamak.origin.title.contentEquals(it.origin, ignoreCase = true) }
        } else if (payamak.origin.number.hasValue) {
            origins.filter { it.origin.endsWith(payamak.origin.number!!.main.toString()) }
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

    data class PredefinedOrigin(
        val id: Long,
        val origin: String,
        val rank: Double,
        val source: OriginRankSource,
        val position: Long
    )
}
