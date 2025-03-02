package no1.payamax.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.util.Log
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

fun contact(addressNumber: String, cr: ContentResolver): Contact? {
    val contactsUri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(addressNumber)
    )
    cr.query(
        contactsUri,
        arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME),
        null,
        null,
        null
    )?.use {
        //return Contact(it.count.toLong(), it.count.toString())
        if (it.moveToFirst()) {
            val dump = dump(it)
            Log.i("dump", dump)
            return Contact(it.getLong(0), it.getString(1))
        }
    }
    return null
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
    val addressValue = cursor.getString(payamakColumns.addressIndex)
    val address = addressValue.toLongOrNull()?.cellNumber()
    val addressTitle = if (address == null) addressValue else null
    val origin =
        Origin(address, addressTitle, address?.let { contact(addressValue, contentResolver) })
    val payamak = Payamak(
        cursor.getLong(payamakColumns.idIndex),
        cursor.getLong(payamakColumns.dateIndex),
        origin,
        cursor.getString(payamakColumns.bodyIndex)
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