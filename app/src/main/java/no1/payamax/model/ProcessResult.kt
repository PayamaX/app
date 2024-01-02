package no1.payamax.model

data class ProcessResult(
    val name: String,
    val related: Boolean,
    val rate: Double?,
    val message: String
) {
    fun dump(indent: String): String {
        val output = StringBuffer()
        output.append("${indent}name: $name\n")
        output.append("${indent}related: $related\n")
        output.append("${indent}rate: $rate\n")
        output.append("${indent}message: $message\n")
        return output.toString()
    }
}