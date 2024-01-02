package no1.payamax.contracts

import no1.payamax.model.ProcessResult

data class Usability(
    val clazz: UsabilityClass,
    val rate: UsabilityRate,
    val processResults: List<ProcessResult>
) {
    fun dump(indent: String): String {
        val output = StringBuffer()
        output.append("${indent}class: $clazz\n")
        output.append("${indent}rate: ${rate.probability}\n")
        for (pr in processResults) {
            output.append(pr.dump("$indent\t"))
            output.append("\n")
        }
        return output.toString()
    }
}