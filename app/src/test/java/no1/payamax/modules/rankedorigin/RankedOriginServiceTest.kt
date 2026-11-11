package no1.payamax.modules.rankedorigin

import android.content.SharedPreferences
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.config.PaxKeys
import no1.payamax.db.PayamaxDatabase
import no1.payamax.db.RankedOriginEntity
import no1.payamax.enums.OriginRankSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.UUID

class RankedOriginServiceTest {
    @Test
    fun `sync runs until it reaches an empty page`() = runBlocking {
        val database = openDatabase()
        val preferences = MemoryPreferences()
        preferences.edit().putString(PaxKeys.RANKED_ORIGIN_LAST_SYNCED, "100.5").commit()
        val requests = mutableListOf<PageRequest>()
        val pages = listOf(
            listOf(
                item(identifier = 1, origin = "alpha", updated = "10.0"),
                item(identifier = 2, origin = "beta", updated = "11.0"),
            ),
            listOf(item(identifier = 3, origin = "gamma", updated = "12.0")),
            emptyList(),
        )
        val service = service(database, preferences, pages, requests)

        service.sync()

        assertEquals(
            listOf(
                PageRequest("100.5", null, null),
                PageRequest("100.5", "11.0", 2),
                PageRequest("100.5", "12.0", 3),
            ),
            requests,
        )
        assertEquals(listOf("alpha", "beta", "gamma"), database.rankedOriginQueries.list().awaitAsList().map { it.origin })
        assertNotEquals("100.5", preferences.getString(PaxKeys.RANKED_ORIGIN_LAST_SYNCED, null))
    }

    @Test
    fun `sync supports delete`() = runBlocking {
        val database = openDatabase()
        val queries = database.rankedOriginQueries
        val removedId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val keptId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        queries.upsert("removed", 0.4, OriginRankSource.PayamaX, 1, 1, removedId.toString())
        queries.upsert("kept", 0.6, OriginRankSource.User, 2, 1, keptId.toString())
        val service = service(
            database,
            MemoryPreferences(),
            listOf(
                listOf(
                    item(
                        identifier = 9,
                        origin = "removed",
                        updated = "40.1",
                        deleted = "40.1",
                        globalId = removedId,
                    ),
                ),
                emptyList(),
            ),
        )

        service.sync()

        val rows = queries.list().awaitAsList()
        assertEquals(listOf("kept"), rows.map { it.origin })
        assertEquals(keptId.toString(), rows.single().global_id)
    }

    @Test
    fun `sync supports update`() = runBlocking {
        val database = openDatabase()
        val queries = database.rankedOriginQueries
        val globalId = UUID.fromString("00000000-0000-0000-0000-000000000003")
        queries.upsert("bank", 0.2, OriginRankSource.PayamaX, 1, 1, globalId.toString())
        val service = service(
            database,
            MemoryPreferences(),
            listOf(
                listOf(
                    item(
                        identifier = 8,
                        origin = "bank",
                        rank = 0.9,
                        position = 4,
                        updated = "30.2",
                        globalId = globalId,
                    ),
                ),
                emptyList(),
            ),
        )

        service.sync()

        val row = queries.list().awaitAsList().single()
        assertEquals("bank", row.origin)
        assertEquals(0.9, row.rank, 0.0)
        assertEquals(4L, row.position)
        assertEquals(OriginRankSource.PayamaX, row.source)
        assertEquals(globalId.toString(), row.global_id)
    }

    private fun service(
        database: PayamaxDatabase,
        preferences: SharedPreferences,
        pages: List<List<RankedOriginPublicPortable>>,
        requests: MutableList<PageRequest> = mutableListOf(),
    ): RankedOriginService {
        return RankedOriginService(database.rankedOriginQueries, preferences) { lastSynced, cursorInstant, cursorId ->
            requests += PageRequest(lastSynced, cursorInstant, cursorId)
            pages[requests.lastIndex]
        }
    }

    private fun openDatabase(): PayamaxDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        runBlocking { PayamaxDatabase.Schema.create(driver).await() }
        return PayamaxDatabase(driver, RankedOriginEntity.Adapter(EnumColumnAdapter()))
    }

    private fun item(
        identifier: Long,
        origin: String,
        rank: Double = 0.5,
        position: Int = identifier.toInt(),
        updated: String,
        deleted: String? = null,
        globalId: UUID = UUID.nameUUIDFromBytes(origin.toByteArray()),
    ) = RankedOriginPublicPortable(
        identifier = identifier,
        globalId = globalId,
        updated = updated,
        deleted = deleted,
        origin = origin,
        rank = rank,
        position = position,
    )

    private data class PageRequest(
        val lastSynced: String,
        val cursorInstant: String?,
        val cursorId: Long?,
    )

    private class MemoryPreferences : SharedPreferences {
        private val strings = mutableMapOf<String, String>()

        override fun getAll(): MutableMap<String, *> = strings.toMutableMap()

        override fun getString(key: String?, defValue: String?): String? = strings[key] ?: defValue

        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues

        override fun getInt(key: String?, defValue: Int): Int = defValue

        override fun getLong(key: String?, defValue: Long): Long = defValue

        override fun getFloat(key: String?, defValue: Float): Float = defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue

        override fun contains(key: String?): Boolean = strings.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        private inner class Editor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, String?>()

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = this

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this

            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = null
                return this
            }

            override fun clear(): SharedPreferences.Editor = this

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                for ((key, value) in pending) {
                    if (value == null) strings.remove(key) else strings[key] = value
                }
            }
        }
    }
}
