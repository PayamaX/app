package no1.payamax.modules.rankedorigin

import kotlinx.coroutines.flow.Flow
import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.contracts.Origin
import no1.payamax.db.RankedOriginEntity
import no1.payamax.enums.OriginRankSource
import kotlin.coroutines.CoroutineContext

interface RankedOriginContract {
    suspend fun itemsFlow(): Flow<List<RankedOriginPublicPortable>>
    suspend fun cachedItems(): List<RankedOriginPublicPortable>
    suspend fun cachedEntities(): List<RankedOriginEntity>
    suspend fun sync()
    suspend fun upsert(origin: Origin, rank: Double, user: OriginRankSource)
    suspend fun cachedItemsCount(): Long
}