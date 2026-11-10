package no1.payamax.modules.rankedorigin

import android.content.Context
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import no1.payamax.api.apis.RankedOriginsPublicV1ControllerApi
import no1.payamax.api.models.PageRankedOriginPublicPortable
import no1.payamax.api.models.Pageable
import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.config.PaxConfig
import no1.payamax.contracts.Origin
import no1.payamax.db.RankedOriginEntity
import no1.payamax.db.RankedOriginQueries
import no1.payamax.enums.OriginRankSource
import java.time.Instant
import androidx.core.content.edit
import app.cash.sqldelight.async.coroutines.awaitAsOne
import kotlin.coroutines.CoroutineContext

class RankedOriginService(private val queries: RankedOriginQueries, private val context: Context) : RankedOriginContract {
    override suspend fun itemsFlow(): Flow<List<RankedOriginPublicPortable>> = flow {
        val dbItems = queries.list().awaitAsList()
        val dbPortables = dbItems.map { it.portable() }
        emit(dbPortables)
        if (refreshNeeded()) {
            sync()
        }
    }

    override suspend fun cachedItems(): List<RankedOriginPublicPortable> {
        return queries.list().awaitAsList().map { it.portable() }
    }

    override suspend fun cachedEntities(): List<RankedOriginEntity> {
        return queries.list().awaitAsList()
    }

    override suspend fun upsert(origin: Origin, rank: Double, user: OriginRankSource) {
        mutex.withLock {
            val now = Instant.now().toEpochMilli()
            queries.upsert(
                origin.origin(),
                rank,
                user,
                0,
                now,
                null
            )
        }
    }

    override suspend fun cachedItemsCount(): Long {
        return queries.countGlobals().awaitAsOne()
    }

    override suspend fun sync() {
        mutex.withLock {
            val apiItems = withContext(Dispatchers.IO) {
                RankedOriginsPublicV1ControllerApi().get1(Pageable(0, 10_000))
            }
            write(apiItems)
            context.getSharedPreferences("general", 0)
                .edit {
                    putLong("RANKED_ORIGIN_LAST_SYNCED", Instant.now().toEpochMilli())
                }
        }
    }

    private suspend fun write(apiItems: PageRankedOriginPublicPortable) {
        val now = Instant.now().toEpochMilli()
        for ((globalId, origin, rank, position) in apiItems.content!!) {
            val apiGlobalId = globalId.toString()
            queries.upsert(
                origin!!,
                rank!!,
                OriginRankSource.PayamaX,
                position!!.toLong(),
                now,
                apiGlobalId
            )
        }
        queries.removeUnsyncedItems(now)
    }

    private fun refreshNeeded(): Boolean {
        val sharedPreferences = context.getSharedPreferences("general", 0)
        val lastSynced = sharedPreferences.getLong("RANKED_ORIGIN_LAST_SYNCED", 0)
        val refreshNeeded = PaxConfig.RANKED_ORIGINS_AGE_MILLIS < Instant.now().toEpochMilli() - lastSynced
        return refreshNeeded
    }
}


private val mutex = Mutex()