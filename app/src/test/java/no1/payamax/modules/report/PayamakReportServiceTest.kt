package no1.payamax.modules.report

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import no1.payamax.contracts.Origin
import no1.payamax.contracts.Payamak
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.contracts.Usability
import no1.payamax.contracts.UsabilityRate
import no1.payamax.db.PayamaxDatabase
import no1.payamax.db.RankedOriginEntity
import no1.payamax.enums.OriginRankSource
import no1.payamax.model.ProcessResult
import no1.payamax.model.ProcessedPayamakModel
import no1.payamax.model.ReviewableProcessedPayamak
import no1.payamax.modules.rankedorigin.RankedOriginContract
import no1.payamax.services.UsabilityProcessorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PayamakReportServiceTest {

    private lateinit var database: PayamaxDatabase
    private val rankedOriginContract = object : RankedOriginContract {
        val upserted = mutableListOf<Triple<Origin, Double, OriginRankSource>>()
        override fun itemsFlow() = flowOf(emptyList<RankedOriginEntity>())
        override suspend fun cachedItems() = emptyList<no1.payamax.api.models.RankedOriginPublicPortable>()
        override suspend fun cachedEntries() = emptyList<RankedOriginEntity>()
        override suspend fun upsert(origin: Origin, rank: Double, source: OriginRankSource) {
            upserted.add(Triple(origin, rank, source))
        }
        override suspend fun sync() {}
    }
    private val engine = UsabilityProcessorEngine(emptyList())

    @Before
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PayamaxDatabase.Schema.synchronous().create(driver)
        database = PayamaxDatabase(driver, RankedOriginEntity.Adapter(EnumColumnAdapter()))
    }

    @Test
    fun `privacy settings enforce null body on partial usable reporting`() = runBlocking {
        val service = PayamakReportService(
            reportQueries = database.payamakReportQueries,
            rankedOriginContract = rankedOriginContract,
            engine = engine
        )

        val payamak = Payamak(
            id = 1L,
            received = 1732111222000L,
            origin = Origin(null, "TestSender", null),
            body = "Secret verification code: 12345"
        )
        val usability = Usability(
            clazz = PayamakUsabilityClass.Usable,
            rate = UsabilityRate(0.8),
            processResults = listOf(ProcessResult("TestRule", true, 0.8, "Matched"))
        )
        val message = ReviewableProcessedPayamak(
            pp = ProcessedPayamakModel(1L, payamak, usability),
            selected = true
        )

        val item = PayamakReportSubmissionItem(
            message = message,
            userOpinion = PayamakUsabilityClass.Usable,
            userComment = "A useful message"
        )

        // Submit with PARTIAL info
        val result = service.submitBatchReport(
            items = listOf(item),
            allowAllSpamInfo = true,
            usableInfoSharingOption = UsableInfoSharingOption.PARTIAL
        )

        // Local DB check
        val records = database.payamakReportQueries.list().executeAsList()
        assertEquals(1, records.size)
        val record = records.first()
        assertEquals("TestSender", record.origin)
        assertNull("Body must be null when partial usable sharing is selected", record.body)
        assertNotNull("SHA-512 must be present", record.sha512)
        assertTrue(record.sha512!!.length == 128)
        assertEquals("Useful", record.expected_diagnostics)
        assertEquals("A useful message", record.reporter_comment)
        assertEquals(1L, record.payamak_id)

        val saved = service.savedReportsByPayamakId()
        assertEquals("A useful message", saved[1L]?.userComment)
        assertEquals(PayamakUsabilityClass.Usable, saved[1L]?.userOpinion)
    }

    @Test
    fun `privacy settings allow full body on spam when allowAllSpamInfo is true`() = runBlocking {
        val service = PayamakReportService(
            reportQueries = database.payamakReportQueries,
            rankedOriginContract = rankedOriginContract,
            engine = engine
        )

        val payamak = Payamak(
            id = 2L,
            received = 1732111222000L,
            origin = Origin(null, "Spammer", null),
            body = "Buy lottery tickets now!"
        )
        val usability = Usability(
            clazz = PayamakUsabilityClass.Spam,
            rate = UsabilityRate(0.1),
            processResults = listOf()
        )
        val message = ReviewableProcessedPayamak(
            pp = ProcessedPayamakModel(2L, payamak, usability),
            selected = true
        )

        val item = PayamakReportSubmissionItem(
            message = message,
            userOpinion = PayamakUsabilityClass.Spam,
            userComment = "Spam report"
        )

        // Submit with allowAllSpamInfo = true
        service.submitBatchReport(
            items = listOf(item),
            allowAllSpamInfo = true,
            usableInfoSharingOption = UsableInfoSharingOption.PARTIAL
        )

        val records = database.payamakReportQueries.list().executeAsList()
        assertEquals(1, records.size)
        val record = records.first()
        assertEquals("Buy lottery tickets now!", record.body)
        assertNotNull(record.sha512)
        assertEquals("Spam", record.expected_diagnostics)
        assertEquals(2L, record.payamak_id)
    }

    @Test
    fun `latest report for a payamak is the one reloaded`() = runBlocking {
        val service = PayamakReportService(
            reportQueries = database.payamakReportQueries,
            rankedOriginContract = rankedOriginContract,
            engine = engine
        )
        val payamak = Payamak(
            id = 5L,
            received = 1732111222000L,
            origin = Origin(null, "Sender", null),
            body = "Hello"
        )
        val usability = Usability(
            clazz = PayamakUsabilityClass.Spam,
            rate = UsabilityRate(0.2),
            processResults = emptyList()
        )
        val message = ReviewableProcessedPayamak(
            pp = ProcessedPayamakModel(5L, payamak, usability),
            selected = true
        )

        service.submitBatchReport(
            items = listOf(PayamakReportSubmissionItem(message, PayamakUsabilityClass.Spam, "first")),
            allowAllSpamInfo = false,
            usableInfoSharingOption = UsableInfoSharingOption.PARTIAL
        )
        service.submitBatchReport(
            items = listOf(PayamakReportSubmissionItem(message, PayamakUsabilityClass.Important, "second")),
            allowAllSpamInfo = false,
            usableInfoSharingOption = UsableInfoSharingOption.PARTIAL
        )

        val saved = service.savedReportsByPayamakId()
        assertEquals(1, saved.size)
        assertEquals("second", saved[5L]?.userComment)
        assertEquals(PayamakUsabilityClass.Important, saved[5L]?.userOpinion)
    }
}
