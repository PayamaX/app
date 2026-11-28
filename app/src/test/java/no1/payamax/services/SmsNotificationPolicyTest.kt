package no1.payamax.services

import no1.payamax.contracts.PayamakUsabilityClass
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsNotificationPolicyTest {

    @Test
    fun shouldNotify_returnsTrue_forImportantUsableAndUnknown() {
        assertTrue(SmsNotificationManager.shouldNotify(PayamakUsabilityClass.Important))
        assertTrue(SmsNotificationManager.shouldNotify(PayamakUsabilityClass.Usable))
        assertTrue(SmsNotificationManager.shouldNotify(PayamakUsabilityClass.Unknown))
    }

    @Test
    fun shouldNotify_returnsFalse_forSpam() {
        assertFalse(SmsNotificationManager.shouldNotify(PayamakUsabilityClass.Spam))
    }
}
