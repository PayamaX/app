package no1.payamax.utils

import no1.payamax.utils.calendar.PersianCalendar
import java.util.Calendar
import java.util.Locale


fun String.isValidPhone(): Boolean {
    return this.startsWith("0")
}

fun String.isValidMobile(): Boolean {
    return this.startsWith("09") && this.length == 11
}

private fun String.dateFormatFromShamsi(): String {//input is persian DateTime i.e: "1397-08-20T09:30:01Z"

    val indexOfT = indexOf("T")
    val date = substring(0, indexOfT)

    val weekDayName = date.dayOfWeek()

    //val tmp = PersianCalendarUtils.getTodayOrTomorrowOrAfterTomorrowString(date)

    return "$weekDayName، ${date.replace("-", "/")}"
}

private fun String.dateTimeFormatFromShamsi(): String {//input is persian DateTime i.e: "1397-08-20T09:30:01Z"
    val indexOfT = indexOf("T")
    val date = substring(0, indexOfT)
    val weekDayName = date.dayOfWeek()
    val time = substring(indexOfT + 1)
    val lastIndexOfColon = time.lastIndexOf(":")

    //val tmp = PersianCalendarUtils.getTodayOrTomorrowOrAfterTomorrowString(date)

    return "$weekDayName، ${date.replace("-", "/")} ساعت: ${time.substring(0, lastIndexOfColon)}"

}

fun String.toPersianDateTimeString(): String {//input format: 2018-11-03T14:19:10Z
    if (startsWith("2")) {//miladi
        val indexOfT = indexOf("T")
        var date = substring(0, indexOfT).replace("/", "-")

        val (gYear, gMonth, gDay) = getDateTripleFromDateString(date)

        val hPc = PersianCalendar()
        hPc.gregorianToPersian(gYear, gMonth, gDay)


        val pYear = hPc.getYear()
        val pMonth = hPc.getMonth()
        val pDay = hPc.getDay()

        date = String.format(Locale.ENGLISH, "%04d", pYear) + "/" +
                String.format(Locale.ENGLISH, "%02d", pMonth + 1) + "/" +
                String.format(Locale.ENGLISH, "%02d", pDay)

        val weekDayName = date.dayOfWeek()
        //val tmp = PersianCalendarUtils.getTodayOrTomorrowOrAfterTomorrowString(date)

        val time = substring(indexOfT + 1)
        val lastIndexOfColon = time.lastIndexOf(":")

        return "$weekDayName، ${date.replace("-", "/")} ساعت: ${
            time.substring(
                0,
                lastIndexOfColon
            )
        }"

    } else {//shamsi
        return dateTimeFormatFromShamsi()
    }
}

fun String.dayOfWeek(): String {
    val millis = toMillis()
    return millis.dayOfWeek()
}

fun String.toMillis(): Long {
    if (startsWith("2")) {//miladi: 2018-11-03T14:19:10Z
        val indexOfT = indexOf("T")
        if (indexOfT != -1) {
            val date = substring(0, indexOfT).replace("/", "-")

            val (gYear, gMonth, gDay) = getDateTripleFromDateString(date)

            val time = substring(indexOfT + 1)

            val (hour, minute) = getTimePairFromString(time)

            val instance = getGregorianCalendar(gYear, gMonth, gDay, hour, minute)

            return instance.timeInMillis
        } else {////miladi: 2018-11-03
            val date = this.replace("/", "-")
            val (gYear, gMonth, gDay) = getDateTripleFromDateString(date)

            val instance = getGregorianCalendar(gYear, gMonth, gDay, 0, 0)

            return instance.timeInMillis
        }

    } else {//shamsi: 1397-08-09T09:30:01Z
        val indexOfT = indexOf("T")
        if (indexOfT != -1) {
            val date = substring(0, indexOfT).replace("/", "-")
            val (pYear, pMonth, pDay) = getDateTripleFromDateString(date)

            val hPc = PersianCalendar()
            hPc.persianToGregorian(pYear, pMonth, pDay)

            val gYear = hPc.getYear()
            val gMonth = hPc.getMonth()
            val gDay = hPc.getDay()


            val time = substring(indexOfT + 1)

            val (hour, minute) = getTimePairFromString(time)

            val instance = getGregorianCalendar(gYear, gMonth, gDay, hour, minute)

            return instance.timeInMillis
        } else {//shamsi: 1397-08-09
            val date = this.replace("/", "-")
            val (pYear, pMonth, pDay) = getDateTripleFromDateString(date)

            val hPc = PersianCalendar()
            hPc.persianToGregorian(pYear, pMonth, pDay)

            val gYear = hPc.getYear()
            val gMonth = hPc.getMonth()
            val gDay = hPc.getDay()

            val instance = getGregorianCalendar(gYear, gMonth, gDay, 0, 0)

            return instance.timeInMillis
        }

    }

}

private fun getTimePairFromString(time: String): Pair<Int, Int> {
    val hour = Integer.parseInt(time.split(":")[0])
    val minute = Integer.parseInt(time.split(":")[1])
    return Pair(hour, minute)
}

//returns zero based month
private fun getDateTripleFromDateString(date: String): Triple<Int, Int, Int> {
    val year = Integer.parseInt(date.split("-")[0])
    val month = Integer.parseInt(date.split("-")[1]) - 1
    val day = Integer.parseInt(date.split("-")[2])
    return Triple(year, month, day)
}

private fun getGregorianCalendar(
    gYear: Int,
    gMonth: Int,
    gDay: Int,
    hour: Int,
    minute: Int
): Calendar {
    val instance = Calendar.getInstance()
    instance.set(Calendar.YEAR, gYear)
    instance.set(Calendar.MONTH, gMonth)
    instance.set(Calendar.DAY_OF_MONTH, gDay)
    instance.set(Calendar.HOUR_OF_DAY, hour)
    instance.set(Calendar.MINUTE, minute)
    instance.set(Calendar.SECOND, 0)
    instance.set(Calendar.MILLISECOND, 0)
    return instance
}


