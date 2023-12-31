package no1.payamax

import no1.payamax.contracts.CellNumber
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CellNumberTest {
    @Test
    fun `test if parse detects iran prefix`() {
        assertEquals(98, CellNumber.parse(989123456789).prefix)
        assertEquals(9123456789, CellNumber.parse(989123456789).main)
    }

    @Test
    fun `test if parse skips no-prefixed number`() {
        assertEquals(null, CellNumber.parse(9123456789).prefix)
        assertEquals(9123456789, CellNumber.parse(9123456789).main)
    }
}