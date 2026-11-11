package no1.payamax.utils

import java.time.Instant

object InstantUtility {
    fun fromCompact(textual: String): Instant {
        val parts = textual.split(".")
        if (parts.size != 2) {
            throw IllegalArgumentException("")
        }
        val millis = parts[0].toLongOrNull()
        val nanos = parts[1].toLongOrNull()
        if (millis == null || nanos == null) {
            throw IllegalArgumentException("")
        }
        if (1_000_000_000 < nanos) {
            throw IllegalArgumentException("")
        }
        return Instant.ofEpochSecond(millis, nanos)
    }
    fun toCompact(instant: Instant): String {
        return "${instant.epochSecond}.${instant.nano}"
    }
}


fun Instant.toCompact(): String {
    return "${epochSecond}.${nano}"
}