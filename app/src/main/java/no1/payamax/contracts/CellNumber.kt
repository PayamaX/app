package no1.payamax.contracts

import kotlin.math.log10
import kotlin.math.pow

data class CellNumber(val prefix: Int?, val main: Long) {
    fun dump(indent: String): String {
        val output = StringBuffer("\n")
        output.append("${indent}prefix: $prefix\n")
        output.append("${indent}main: $main\n")
        return output.toString()
    }

    companion object {
        private val prefixes = listOf(98)

        fun parse(number: Long): CellNumber {
            val fullLength = log10(number.toDouble()).toInt() + 1
            for (prefix in prefixes) {
                val prefixLength = log10(prefix.toDouble()).toInt() + 1
                val c = 10.0.pow((fullLength - prefixLength).toDouble())
                val divided = (number / c).toInt()
                if (divided == prefix) {
                    val plainNumber = number % c
                    return CellNumber(prefix, plainNumber.toLong())
                }
            }
            return CellNumber(null, number)
        }
    }
}