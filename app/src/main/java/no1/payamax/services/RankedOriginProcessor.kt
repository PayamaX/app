package no1.payamax.services

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.stateIn
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
    private val origins = rankedOriginProvider.itemsFlow().stateIn(scope, SharingStarted.Eagerly, emptyList())

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

        val userOrigin = related.find { it.source == OriginRankSource.User }
        if (userOrigin != null) {
            return UsabilityRate(userOrigin.rank)
        }

        return UsabilityRate(related.sumOf { it.rank } / related.count())
    }
}
