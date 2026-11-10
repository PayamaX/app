package no1.payamax.modules.rankedorigin

import no1.payamax.api.models.RankedOriginPublicPortable
import no1.payamax.db.RankedOriginEntity
import java.util.UUID

fun RankedOriginEntity.portable(): RankedOriginPublicPortable = RankedOriginPublicPortable(
    globalId = global_id?.let { runCatching { UUID.fromString(it) }.getOrNull() },
    origin = origin,
    rank = rank,
    position = position.toInt()
)