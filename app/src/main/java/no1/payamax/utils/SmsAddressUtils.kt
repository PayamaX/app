package no1.payamax.utils

data class SmsSegmentInfo(
    val length: Int,
    val maxPerSegment: Int,
    val segmentCount: Int,
    val isUnicode: Boolean
)

object SmsAddressUtils {

    private val directionalAndFormatChars = Regex("[\\s\\-()\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069]")
    private val validNumericAddressRegex = Regex("^\\+?\\d{3,}$")
    private val iranMobileWithoutLeadingZero = Regex("^9\\d{9}$")
    private val iranMobileWithCountryCodeNoPlus = Regex("^989\\d{9}$")

    fun toAsciiDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '\u06F0'..'\u06F9' -> sb.append(('0'.code + (ch.code - '\u06F0'.code)).toChar())
                in '\u0660'..'\u0669' -> sb.append(('0'.code + (ch.code - '\u0660'.code)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun cleanAddress(address: String?): String {
        if (address.isNullOrBlank()) return ""
        return toAsciiDigits(address).replace(directionalAndFormatChars, "")
    }

    /**
     * Determines whether an SMS sender address can receive a reply.
     * Alphanumeric / titled senders (e.g., "IRANCELL", "Snapp", "Digikala", Persian titles)
     * and malformed or overly short addresses cannot receive SMS replies.
     */
    fun canReplyToAddress(address: String?): Boolean {
        val cleaned = cleanAddress(address)
        if (cleaned.isBlank()) return false
        if (cleaned.any { it.isLetter() }) return false
        return validNumericAddressRegex.matches(cleaned)
    }

    /**
     * Normalizes a dialable SMS address before sending.
     * Restores leading '0' for 10-digit Iranian mobile numbers (9xxxxxxxxx -> 09xxxxxxxxx)
     * and '+' for 12-digit country-prefixed numbers (989xxxxxxxxx -> +989xxxxxxxxx).
     */
    fun normalizeSmsDestination(address: String): String {
        val cleaned = cleanAddress(address)
        return when {
            iranMobileWithoutLeadingZero.matches(cleaned) -> "0$cleaned"
            iranMobileWithCountryCodeNoPlus.matches(cleaned) -> "+$cleaned"
            else -> cleaned
        }
    }

    fun calculateSegmentInfo(text: String): SmsSegmentInfo {
        val isUnicode = text.any { ch -> ch.code > 0x7F }
        val singleLimit = if (isUnicode) 70 else 160
        val multiLimit = if (isUnicode) 67 else 153
        val length = text.length
        val segments = when {
            length == 0 -> 1
            length <= singleLimit -> 1
            else -> (length + multiLimit - 1) / multiLimit
        }
        val currentLimit = if (segments == 1) singleLimit else segments * multiLimit
        return SmsSegmentInfo(
            length = length,
            maxPerSegment = currentLimit,
            segmentCount = segments,
            isUnicode = isUnicode
        )
    }
}
