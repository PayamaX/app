package no1.payamax.repos

import no1.payamax.contracts.Origin
import no1.payamax.db.PredefinedOriginQueries
import no1.payamax.hasValue
import no1.payamax.services.OriginRankSource
import no1.payamax.services.PredefinedOrigin
import no1.payamax.services.prepared

class PredefinedOriginRepo(val queries: PredefinedOriginQueries) {
    fun all(): List<PredefinedOrigin> {
        return queries.all().executeAsList().map { it.prepared() }
    }

    fun upsert(origin: Origin, rank: Double, source: OriginRankSource = OriginRankSource.User) {
        val title = if (origin.title.hasValue) origin.title else null
        val numberMain = origin.number?.main
        val now = System.currentTimeMillis()

        if (title != null) {
            val existing = queries.findByTitle(title).executeAsOneOrNull()
            if (existing != null) {
                queries.updateRankAndSourceByTitle(rank = rank, source = source, title = title)
            } else {
                queries.insertEntity(
                    number = numberMain,
                    title = title,
                    rank = rank,
                    source = source,
                    position = 0,
                    last_synced = now,
                    global_id = null
                )
            }
        } else if (numberMain != null) {
            val existing = queries.findByNumber(numberMain).executeAsOneOrNull()
            if (existing != null) {
                queries.updateRankAndSourceByNumber(rank = rank, source = source, number = numberMain)
            } else {
                queries.insertEntity(
                    number = numberMain,
                    title = null,
                    rank = rank,
                    source = source,
                    position = 0,
                    last_synced = now,
                    global_id = null
                )
            }
        }
    }
}
