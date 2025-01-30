package no1.payamax.contracts

import no1.payamax.hasValue

data class Origin(val number: CellNumber?, val title: String?, val contact: Contact?) {
    fun dump(indent: String): String {
        val output = StringBuffer("\n")
        output.append("${indent}number: ${number?.dump("$indent\t") ?: "(null)"}\n")
        output.append("${indent}title: ${title ?: "(null)"}\n")
        output.append("${indent}contact: ${contact ?: "(null)"}\n")
        return output.toString()
    }

    fun displayable(): String {
        if (contact.hasValue) {
            return contact!!.name;
        }
        if (title.hasValue) {
            return title!!;
        }
        if (number.hasValue) {
            return number!!.displayable()
        }
        return "?"
    }
}