package no1.payamax.repos

import no1.payamax.db.PredefinedOriginQueries
import no1.payamax.services.PredefinedOrigin
import no1.payamax.services.prepared

class PredefinedOriginRepo(val queries: PredefinedOriginQueries) {
    fun all(): List<PredefinedOrigin>{
        return queries.all().executeAsList().map { it.prepared() }
    }
}