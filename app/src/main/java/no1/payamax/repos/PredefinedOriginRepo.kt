package no1.payamax.repos

import no1.payamax.contracts.Origin
import no1.payamax.db.PredefinedOriginQueries
import no1.payamax.enums.OriginRankSource
import no1.payamax.prepared
import no1.payamax.services.PredefinedOriginProcessor
import java.util.UUID

class PredefinedOriginRepo(val queries: PredefinedOriginQueries) {
    fun all(): List<PredefinedOriginProcessor.PredefinedOrigin> {
        return queries.all().executeAsList().map { it.prepared() }
    }

    fun upsert(origin: Origin, rank: Double, source: OriginRankSource = OriginRankSource.User) {
        queries.upsert(origin.origin(), rank, source, 1, 0, UUID.randomUUID().toString())
    }
}
