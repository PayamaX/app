package no1.payamax.modules.rankedorigin

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.contracts.Origin
import no1.payamax.db.RankedOriginEntity
import no1.payamax.enums.OriginRankSource

interface RankedOriginContract {
    fun itemsFlow(): Flow<List<RankedOriginEntity>>
    suspend fun cachedItems(): List<RankedOriginPublicPortable>
    suspend fun cachedEntries(): List<RankedOriginEntity>
    suspend fun sync()
    suspend fun upsert(origin: Origin, rank: Double, source: OriginRankSource)
}