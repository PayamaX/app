package no1.payamax

import no1.payamax.utils.calendar.PersianCalendar
import no1.payamax.utils.toGregorianDateString
import no1.payamax.utils.toPersianDateString
import no1.payamax.utils.toPersianDigits
import no1.payamax.utils.toPersianTimeString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PersianCalendarTest {

    @Test
    fun `test Gregorian to Persian calendar conversion`() {
        val pc = PersianCalendar()
        // 2026-10-28 -> 1405-08-06 (6 Aban 1405)
        val (year, month, day) = pc.gregorianToPersian(2026, 9, 28) // Month 9 is October (0-based)
        assertEquals(1405, year)
        assertEquals(7, month) // 0-based month index for Aban (7)
        assertEquals(6, day)
    }

    @Test
    fun `test English to Persian digits conversion`() {
        assertEquals("۱۲34۵۶۷۸۹0".toPersianDigits(), "۱۲۳۴۵۶۷۸۹۰".toPersianDigits())
        assertEquals("1234567890".toPersianDigits(), "۱۲۳۴۵۶۷۸۹۰")
    }

    @Test
    fun `test Long toPersianDateString`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(2026, Calendar.OCTOBER, 28, 14, 30)
        val millis = cal.timeInMillis

        val dateStr = millis.toPersianDateString()
        // 28 October 2026 corresponds to 6 Aban 1405
        assertEquals("۶ آبان ۱۴۰۵", dateStr)
    }

    @Test
    fun `test Long toPersianTimeString`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(2026, Calendar.OCTOBER, 28, 14, 30)
        val millis = cal.timeInMillis

        val timeStr = millis.toPersianTimeString()
        assertEquals("۱۴:۳۰", timeStr)
    }

    @Test
    fun `test Long toGregorianDateString`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(2026, Calendar.OCTOBER, 31, 14, 30)
        val millis = cal.timeInMillis

        val dateStr = millis.toGregorianDateString()
        assertEquals("۳۱ اکتبر ۲۰۲۶", dateStr)
    }
}
