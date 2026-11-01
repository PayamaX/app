package no1.payamax.services

import android.content.Context
import no1.payamax.R
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class EmojiUsabilityProcessor(private val context: Context) : PayamakUsabilityRuleContract {
    override val name: String
        get() = context.getString(R.string.emoji)

    private val spamEmojis = listOf("⛔️", "🛑", "⭕️", "❌", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟", "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "⚫️", "⚪️", "🟤", "🔺", "▫️", "▪️", "🔲", "🔳", "🔷", "🔶", "🔹", "🔸", "🔻", "◾️", "◽️", "◼️", "◻️", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "⬛️", "⬜️", "🟫")

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        for (emoji in spamEmojis) {
            if (payamak.body.contains(emoji)) return UsabilityRate(0.0)
        }

        return null
    }

    private fun Char.isEmoji(): Boolean {
        val type = Character.getType(this).toByte()
        return type == Character.SURROGATE || type == Character.OTHER_SYMBOL
    }
}