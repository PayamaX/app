package no1.payamax.utils.calendar

import java.util.*

class PersianCalendar {
    private var day: Int = 0
    private var gD: Int = 0
    private var gM: Int = 0
    private var gY: Int = 0
    private var jD: Int = 0
    private var jM: Int = 0
    private var jY: Int = 0
    private var leap: Int = 0
    private var march: Int = 0
    private var month: Int = 0
    private var year: Int = 0
    var isPersianDate = true

    private fun JG2JD(year: Int, month: Int, day: Int, J1G0: Int): Int {
        val jd = (year + 4800 + (month - 14) / 12) * 1461 / 4 + (month - 2 - (month - 14) / 12 * 12) * 367 / 12 - (year + 4900 + (month - 14) / 12) / 100 * 3 / 4 + day - 32075
        return if (J1G0 == 0) {
            jd - (100100 + year + (month - 8) / 6) / 100 * 3 / 4 + 752
        } else jd
    }


    private fun JD2JG(JD: Int, J1G0: Int) {
        var j = JD * 4 + 139361631
        if (J1G0 == 0) {
            j = (JD * 4 + 183187720) / 146097 * 3 / 4 * 4 + j - 3908
        }
        val i = j % 1461 / 4 * 5 + 308
        this.gD = i % 153 / 5 + 1
        this.gM = i / 153 % 12 + 1
        this.gY = j / 1461 - 100100 + (8 - this.gM) / 6
    }

    private fun JD2Jal(JDN: Int) {
        JD2JG(JDN, 0)
        this.jY = this.gY - 621
        JalCal(this.jY)
        var k = JDN - JG2JD(this.gY, 3, this.march, 0)
        if (k < 0) {
            this.jY--
            k += 179
            if (this.leap == 1) {
                k++
            }
        } else if (k <= 185) {
            this.jM = k / 31 + 1
            this.jD = k % 31 + 1
            return
        } else {
            k -= 186
        }
        this.jM = k / 30 + 7
        this.jD = k % 30 + 1
    }

    private fun Jal2JD(jY: Int, jM: Int, jD: Int): Int {
        JalCal(jY)
        return JG2JD(this.gY, 3, this.march, 1) + (jM - 1) * 31 - jM / 7 * (jM - 7) + jD - 1
    }

    private fun JalCal(jY: Int) {
        this.march = 0
        this.leap = 0
        val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)
        this.gY = jY + 621
        var leapJ = -14
        var jp = breaks[0]
        for (j in 1..19) {
            val jm = breaks[j]
            val jump = jm - jp
            if (jY < jm) {
                var N = jY - jp
                leapJ += N / 33 * 8 + (N % 33 + 3) / 4
                if (jump % 33 == 4 && jump - N == 4) {
                    leapJ++
                }
                this.march = leapJ + 20 - (this.gY / 4 - (this.gY / 100 + 1) * 3 / 4 - 150)
                if (jump - N < 6) {
                    N = N - jump + (jump + 4) / 33 * 33
                }
                this.leap = ((N + 1) % 33 - 1) % 4
                if (this.leap == -1) {
                    this.leap = 4
                    return
                }
                return
            }
            leapJ += jump / 33 * 8 + jump % 33 / 4
            jp = jm
        }
    }

    override fun toString(): String {
        return String.format(Locale.ENGLISH, "%04d/%02d/%02d", *arrayOf<Any>(Integer.valueOf(getYear()), Integer.valueOf(getMonth()), Integer.valueOf(getDay())))
    }

    //month is zero based
    fun gregorianToPersian(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        JD2Jal(JG2JD(year, month + 1, day, 0))
        this.year = this.jY
        this.month = this.jM
        this.day = this.jD

        isPersianDate = true

        return Triple(this.year, this.month - 1, this.day)
    }

    //month is zero based
    fun persianToGregorian(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        JD2JG(Jal2JD(year, month + 1, day), 0)
        this.year = this.gY
        this.month = this.gM
        this.day = this.gD

        isPersianDate = false

        return Triple(this.year, this.month - 1, this.day)
    }

    fun getDay(): Int {
        return this.day
    }

    //returns zero based month
    fun getMonth(): Int {
        return this.month - 1//make this zero based to compatibility with java date system
    }

    fun getYear(): Int {
        return this.year
    }
}