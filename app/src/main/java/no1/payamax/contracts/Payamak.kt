package no1.payamax.contracts

data class Payamak(
    val id: Long,
    val received: Long,
    val origin: Origin,
    val body: String,
    val type: Int = MESSAGE_TYPE_INBOX
) {
    val isOutgoing: Boolean
        get() = type != MESSAGE_TYPE_INBOX

    fun dump(indent: String): String {
        val output = StringBuffer()
        output.append("${indent}received: $received\n")
        output.append("${indent}origin: ${origin.dump("$indent\t")}\n")
        output.append("${indent}type: $type\n")
        output.append("${indent}body:\n------------\n$body\n------------\n")
        return output.toString()
    }

    companion object {
        const val MESSAGE_TYPE_INBOX = 1
        const val MESSAGE_TYPE_SENT = 2
        const val MESSAGE_TYPE_DRAFT = 3
        const val MESSAGE_TYPE_OUTBOX = 4
        const val MESSAGE_TYPE_FAILED = 5
        const val MESSAGE_TYPE_QUEUED = 6
    }
}
