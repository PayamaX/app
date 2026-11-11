package no1.payamax.modules.rankedorigin

import android.content.SharedPreferences
import androidx.core.content.edit
import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import no1.payamax.api.apis.RankedOriginsPublicV1ControllerApi
import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.config.PaxConfig
import no1.payamax.config.PaxKeys
import no1.payamax.config.PaxKeys.RANKED_ORIGIN_LAST_SYNCED
import no1.payamax.contracts.Origin
import no1.payamax.db.RankedOriginEntity
import no1.payamax.db.RankedOriginQueries
import no1.payamax.enums.OriginRankSource
import no1.payamax.hasValue
import no1.payamax.utils.InstantUtility.fromCompact
import no1.payamax.utils.toCompact
import java.time.Instant

class RankedOriginService(
    private val queries: RankedOriginQueries,
    private val preferences: SharedPreferences,
) : RankedOriginContract {
    override suspend fun itemsFlow(): Flow<List<RankedOriginPublicPortable>> = flow {
        val dbItems = queries.list().awaitAsList()
        val dbPortables = dbItems.map { it.portable() }
        emit(dbPortables)
        if (refreshNeeded()) {
            sync()
            emit(queries.list().awaitAsList().map { it.portable() })
        }
    }

    override suspend fun cachedItems(): List<RankedOriginPublicPortable> {
        return queries.list().awaitAsList().map { it.portable() }
    }

    override suspend fun cachedEntries(): List<RankedOriginEntity> {
        return queries.list().awaitAsList()
    }

    override suspend fun upsert(origin: Origin, rank: Double, source: OriginRankSource) {
        mutex.withLock {
            val now = Instant.now().toEpochMilli()
            queries.upsert(
                origin.origin(),
                rank,
                source,
                0,
                now,
                null
            )
        }
    }

    override suspend fun sync() {
        val lastSynced = loadLastSynced()
        val lastSyncedText = lastSynced.toCompact()
        mutex.withLock {
            var cursorInstant: String? = null
            var cursorId: Long? = null
            do {
                val apiItems = withContext(Dispatchers.IO) {
                    RankedOriginsPublicV1ControllerApi().get1(lastSyncedText, cursorInstant, cursorId)
                }
                if (apiItems.isEmpty()) {
                    break
                }
                sync(apiItems)

                cursorInstant = apiItems.last().updated
                cursorId = apiItems.last().identifier
            } while (true)

            persistLastSynced(Instant.now())
        }
    }

    private suspend fun sync(apiItems: List<RankedOriginPublicPortable>) {
        val now = Instant.now().toEpochMilli()
        for (item in apiItems) {
            val deleted = item.deleted?.let { fromCompact(it) }
            val apiGlobalId = item.globalId.toString()
            if (deleted.hasValue) {
                queries.delete(apiGlobalId)
            } else {
                queries.upsert(
                    item.origin!!,
                    item.rank!!,
                    OriginRankSource.PayamaX,
                    item.position!!.toLong(),
                    now,
                    apiGlobalId
                )
            }
        }
    }

    private fun refreshNeeded(): Boolean {
        val refreshNeeded = PaxConfig.RANKED_ORIGINS_AGE_MILLIS < Instant.now().toEpochMilli() - loadLastSynced().toEpochMilli()
        return refreshNeeded
    }

    private fun loadLastSynced(): Instant {
        val lastSyncedRaw = preferences.getString(RANKED_ORIGIN_LAST_SYNCED, "0.0")
        val lastSynced = fromCompact(lastSyncedRaw!!)
        return lastSynced
    }

    private fun persistLastSynced(value: Instant) {
        preferences.edit {
            putString(RANKED_ORIGIN_LAST_SYNCED, value.toCompact())
        }
    }
}


private val mutex = Mutex()