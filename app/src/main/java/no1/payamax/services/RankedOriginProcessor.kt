package no1.payamax.services

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import no1.payamax.db.RankedOriginEntity
import no1.payamax.enums.OriginRankSource
import no1.payamax.hasValue
import no1.payamax.modules.rankedorigin.RankedOriginContract

class RankedOriginProcessor(private val rankedOriginProvider: RankedOriginContract, private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.rankedorigin)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val origins = MutableStateFlow<List<RankedOriginEntity>>(emptyList())

    init {
        scope.launch {
            rankedOriginProvider.itemsFlow().collect { list ->
                origins.value = list
            }
        }
    }

    /**
     * Synchronously reloads the in-memory ranked origins cache from SQLite.
     * Ensures background receivers (cold start) and immediate post-upsert UI re-detection
     * always evaluate against the latest user overrides.
     */
    suspend fun ensureLoaded() {
        origins.value = rankedOriginProvider.cachedEntries()
    }

    fun hasUserOverride(payamak: Payamak): Boolean {
        val related = findRelatedOrigins(payamak)
        return related.any { it.source == OriginRankSource.User }
    }

    private fun findRelatedOrigins(payamak: Payamak): List<RankedOriginEntity> {
        val title = payamak.origin.title
        val number = payamak.origin.number
        return if (title.hasValue && title != null) {
            origins.value.filter { title.equals(it.origin, ignoreCase = true) }
        } else if (number.hasValue && number != null) {
            origins.value.filter { it.origin.endsWith(number.main.toString()) }
        } else {
            listOf()
        }
    }

    override fun guess(payamak: Payamak): UsabilityRate? {
        val related = findRelatedOrigins(payamak)
        if (related.isEmpty())
            return null

        val userOrigin = related
            .filter { it.source == OriginRankSource.User }
            .maxByOrNull { it.last_synced }
        if (userOrigin != null) {
            return UsabilityRate(userOrigin.rank)
        }

        return UsabilityRate(related.sumOf { it.rank } / related.count())
    }
}
