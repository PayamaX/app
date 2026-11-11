package no1.payamax.utils

import no1.payamax.utils.calendar.PersianCalendar
import no1.payamax.utils.calendar.PersianCalendarConstants
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val persianTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Tehran")

fun Long.dayOfWeek(): String {
    val calendar = Calendar.getInstance(persianTimeZone, Locale.ENGLISH)
    calendar.timeInMillis = this
    return PersianCalendarConstants.persianWeekDays[calendar.get(Calendar.DAY_OF_WEEK) % 7]
}

fun Long.toFormattedDateString(calendarType: CalendarType = CalendarType.PERSIAN): String {
    return when (calendarType) {
        CalendarType.PERSIAN -> toPersianDateString()
        CalendarType.GREGORIAN -> toGregorianDateString()
    }
}

fun Long.toGregorianDateString(): String {
    val calendar = Calendar.getInstance(persianTimeZone)
    calendar.timeInMillis = this

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val gregorianPersianMonths = arrayOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "می", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )
    val monthName = gregorianPersianMonths.getOrElse(month) { "" }
    return "$day $monthName $year".toPersianDigits()
}

fun Long.toPersianDateString(): String {
    val calendar = Calendar.getInstance(persianTimeZone)
    calendar.timeInMillis = this

    val gYear = calendar.get(Calendar.YEAR)
    val gMonth = calendar.get(Calendar.MONTH)
    val gDay = calendar.get(Calendar.DAY_OF_MONTH)

    val pc = PersianCalendar()
    val (pYear, pMonth, pDay) = pc.gregorianToPersian(gYear, gMonth, gDay)

    val monthName = PersianCalendarConstants.persianMonthNames[pMonth]
    return "$pDay $monthName $pYear".toPersianDigits()
}

fun Long.toPersianTimeString(): String {
    val calendar = Calendar.getInstance(persianTimeZone)
    calendar.timeInMillis = this

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timeStr = String.format(Locale.ENGLISH, "%02d:%02d", hour, minute)
    return timeStr.toPersianDigits()
}

fun String.toPersianDigits(): String {
    val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    var result = this
    for (i in 0..9) {
        result = result.replace(englishDigits[i], persianDigits[i])
    }
    return result
}
