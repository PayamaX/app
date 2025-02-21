package no1.payamax.utils

import no1.payamax.utils.calendar.PersianCalendarConstants
import java.util.Calendar
import java.util.Locale

fun Long.dayOfWeek() : String{
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.timeInMillis = this
    return PersianCalendarConstants.persianWeekDays[calendar.get(Calendar.DAY_OF_WEEK) % 7]
}