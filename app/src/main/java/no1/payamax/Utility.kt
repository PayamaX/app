package no1.payamax

import no1.payamax.contracts.cellNumber
import no1.payamax.db.PredefinedOriginEntity
import no1.payamax.services.PredefinedOriginProcessor.PredefinedOrigin

val Any?.hasValue: Boolean
    get() = this != null



fun PredefinedOriginEntity.prepared(): PredefinedOrigin {
    return PredefinedOrigin(
        this.id,
        this.origin,
        this.rank,
        this.source,
        this.position
    )
}