package no1.payamax.contracts

data class Payamak(val id: Long, val received: Long, val origin: Origin, val body: String) {
    fun dump(indent: String): String {
        val output = StringBuffer()
        output.append("${indent}received: $received\n")
        output.append("${indent}origin: ${origin.dump("$indent\t")}\n")
        output.append("${indent}body:\n------------\n$body\n------------\n")
        return output.toString()
    }
}
