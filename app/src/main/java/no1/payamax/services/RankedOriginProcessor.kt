package no1.payamax.services

import android.content.Context
import kotlinx.coroutines.runBlocking
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.enums.OriginRankSource
import no1.payamax.hasValue
import no1.payamax.modules.rankedorigin.RankedOriginContract

class RankedOriginProcessor(private val rankedOriginProvider: RankedOriginContract, private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.predefinedorigin)

    private var initialized = false
    private val predefinedOrigins = mutableListOf<PredefinedOrigin>()

    private fun ensureOrigins(): List<PredefinedOrigin> {
        if (!initialized) {
            predefinedOrigins.clear()
            val cached = runBlocking { rankedOriginProvider.cachedEntities() }
            for ((id, origin, rank, source, position) in cached) {
                predefinedOrigins.add(
                    PredefinedOrigin(
                        id = id,
                        origin = origin,
                        rank = rank,
                        source = source,
                        position = position
                    )
                )
            }
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
        val title = payamak.origin.title
        val number = payamak.origin.number
        return if (title.hasValue && title != null) {
            origins.filter { title.equals(it.origin, ignoreCase = true) }
        } else if (number.hasValue && number != null) {
            origins.filter { it.origin.endsWith(number.main.toString()) }
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
