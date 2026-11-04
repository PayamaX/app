package no1.payamax.services

import android.content.Context
import android.net.Uri
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate
import androidx.core.net.toUri

class ConversionalUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.conversional)

    override fun guess(payamak: Payamak): UsabilityRate? {
        val cellNumber = payamak.origin.number ?: return null
        val uri = "content://sms/sent".toUri()
        context.contentResolver.query(
            uri,
            arrayOf("body", "date"),
            "address LIKE ?",
            arrayOf("%${cellNumber.main}"),
            "date desc"
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val text = cursor.getString(cursor.getColumnIndexOrThrow("body"))
            val moment = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
            return UsabilityRate(1.0)
        }
        return null
    }
}
