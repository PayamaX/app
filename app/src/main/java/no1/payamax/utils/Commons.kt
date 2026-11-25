package no1.payamax.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import androidx.core.database.getStringOrNull
import no1.payamax.contracts.Contact
import no1.payamax.contracts.InstantProvider
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.cellNumber
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.services.PayamakColumns
import no1.payamax.services.UsabilityProcessorEngine
import java.time.Instant

object ContactCache {
    private val cache = java.util.Collections.synchronizedMap(mutableMapOf<String, Contact?>())

    fun getContact(addressNumber: String, cr: ContentResolver): Contact? {
        if (addressNumber.isBlank()) return null
        val digits = addressNumber.filter { it.isDigit() }
        val key = if (digits.length >= 7) digits.takeLast(7) else addressNumber
        if (cache.containsKey(key)) {
            return cache[key]
        }
        val result = contact(addressNumber, cr)
        cache[key] = result
        return result
    }

    fun clear() {
        cache.clear()
    }
}

fun contact(addressNumber: String, cr: ContentResolver): Contact? {
    if (addressNumber.isBlank()) return null
    val digits = addressNumber.filter { it.isDigit() }
    if (digits.length < 3) return null

    return try {
        val contactsUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(addressNumber)
        )
        cr.query(
            contactsUri,
            arrayOf(
                BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
            ),
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                val photoUri = it.getStringOrNull(2)
                Contact(it.getLong(0), it.getString(1), photoUri)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

fun dump(it: Cursor): String {
    val output = StringBuffer()
    for (col in it.columnNames.withIndex()) {
        val index = col.index
        val name = col.value
        val type = it.getType(col.index)
        val typeName = typeName(type)
        val value = it.getStringOrNull(col.index)
        output.append("$index\t$name\t$typeName\t$value\n")
    }
    return output.toString()
}

fun typeName(type: Int): String {
    return when (type) {
        Cursor.FIELD_TYPE_BLOB -> "BLOB"
        Cursor.FIELD_TYPE_FLOAT -> "FLOAT"
        Cursor.FIELD_TYPE_INTEGER -> "INT"
        Cursor.FIELD_TYPE_NULL -> "NULL"
        Cursor.FIELD_TYPE_STRING -> "TEXT"
        else -> "ELSE"
    }
}

fun Long.iso8691(): String {
    return Instant.ofEpochMilli(this).toString()
}

fun Long.moment(instantProvider: InstantProvider): String {
    return Instant.ofEpochMilli(this).toString()
}

fun process(cursor: Cursor, contentResolver: ContentResolver, payamakColumns: PayamakColumns, engine: UsabilityProcessorEngine): ReviewableProcessedPayamak {
    val addressValue = cursor.getString(payamakColumns.addressIndex) ?: ""
    val address = addressValue.toLongOrNull()?.cellNumber()
    val addressTitle = if (address == null) addressValue else null
    val contactInfo = if (addressValue.isNotBlank()) ContactCache.getContact(addressValue, contentResolver) else null
    val origin = Origin(address, addressTitle, contactInfo)
    val payamak = Payamak(
        cursor.getLong(payamakColumns.idIndex),
        cursor.getLong(payamakColumns.dateIndex),
        origin,
        cursor.getString(payamakColumns.bodyIndex) ?: ""
    )

    return ReviewableProcessedPayamak(
        ProcessedPayamakModel(
            cursor.getLong(payamakColumns.idIndex),
            payamak,
            engine.detect(payamak)
        ),
        false
    )
}

fun processFast(
    cursor: Cursor,
    payamakColumns: PayamakColumns,
    engine: UsabilityProcessorEngine,
    cachedContact: Contact?
): ReviewableProcessedPayamak {
    val addressValue = cursor.getString(payamakColumns.addressIndex) ?: ""
    val address = addressValue.toLongOrNull()?.cellNumber()
    val addressTitle = if (address == null) addressValue else null
    val origin = Origin(address, addressTitle, cachedContact)
    val payamak = Payamak(
        cursor.getLong(payamakColumns.idIndex),
        cursor.getLong(payamakColumns.dateIndex),
        origin,
        cursor.getString(payamakColumns.bodyIndex) ?: ""
    )

    return ReviewableProcessedPayamak(
        ProcessedPayamakModel(
            cursor.getLong(payamakColumns.idIndex),
            payamak,
            engine.detect(payamak)
        ),
        false
    )
}
