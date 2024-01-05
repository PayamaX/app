package no1.payamax.services

import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityRuleContract
import no1.payamax.contracts.UsabilityRate

class EmojiUsabilityProcessor : PayamakUsabilityRuleContract {
    override val name: String
        get() = "Emoji"

    private val spamEmojis = listOf("⛔️", "🛑", "⭕️", "❌", "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟", "🔴", "🟠", "🟡", "🟢", "🔵", "🟣", "⚫️", "⚪️", "🟤", "🔺", "▫️", "▪️", "🔲", "🔳", "🔷", "🔶", "🔹", "🔸", "🔻", "◾️", "◽️", "◼️", "◻️", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "⬛️", "⬜️", "🟫")

    override fun guess(payamak: Payamak): UsabilityRate? {
        if (payamak.body.isBlank()) return null
        for (emoji in spamEmojis) {
            if (payamak.body.contains(emoji)) return UsabilityRate(0.0)
        }
        for (c in payamak.body) {
            if (c.isEmoji()) return UsabilityRate(0.3)
        }

        return null
    }

    private fun Char.isEmoji(): Boolean {
        val type = Character.getType(this).toByte()
        return type == Character.SURROGATE || type == Character.OTHER_SYMBOL
    }
}