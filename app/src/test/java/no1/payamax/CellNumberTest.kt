package no1.payamax

import no1.payamax.contracts.cellNumber
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
        assertEquals(98, 989123456789.cellNumber().prefix)
        assertEquals(9123456789, 989123456789.cellNumber().main)
    }

    @Test
    fun `test if parse skips no-prefixed number`() {
        assertEquals(null, 9123456789.cellNumber().prefix)
        assertEquals(9123456789, 9123456789.cellNumber().main)
    }
}