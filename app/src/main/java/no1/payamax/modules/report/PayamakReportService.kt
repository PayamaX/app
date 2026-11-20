package no1.payamax.modules.report

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no1.payamax.api.apis.PayamakReportsPublicV1ControllerApi
import no1.payamax.api.models.PayamakReportPublicInput
import no1.payamax.api.models.PayamakReportPublicItemInput
import no1.payamax.api.models.PayamakReportRuleProcessResultPublicInput
import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.db.PayamakReportQueries
import no1.payamax.enums.OriginRankSource
import no1.payamax.modules.rankedorigin.RankedOriginContract
import no1.payamax.services.UsabilityProcessorEngine
import no1.payamax.utils.toCompact
import java.security.MessageDigest
import java.time.Instant

class PayamakReportService(
    private val reportQueries: PayamakReportQueries,
    private val rankedOriginContract: RankedOriginContract,
    private val engine: UsabilityProcessorEngine,
    private val gson: Gson = Gson()
) : PayamakReportContract {

    override suspend fun submitBatchReport(
        items: List<PayamakReportSubmissionItem>,
        allowAllSpamInfo: Boolean,
        usableInfoSharingOption: UsableInfoSharingOption
    ): ReportSubmissionResult {
        if (items.isEmpty()) return ReportSubmissionResult.Success(0)

        val insertedIds = mutableListOf<Long>()
        val apiItems = mutableListOf<PayamakReportPublicItemInput>()
        val now = Instant.now().toEpochMilli()

        for (item in items) {
            val payamak = item.message.pp.payamak
            val rawBody = payamak.body
            val sha512Hash = calculateSha512(rawBody)

            val detectedClass = item.message.pp.usability.clazz
            val userOpinionClass = item.userOpinion

            val isSpam = userOpinionClass == PayamakUsabilityClass.Spam ||
                (userOpinionClass == PayamakUsabilityClass.Unknown && detectedClass == PayamakUsabilityClass.Spam)

            val bodyToSend: String? = if (isSpam) {
                if (allowAllSpamInfo) rawBody else null
            } else {
                if (usableInfoSharingOption == UsableInfoSharingOption.FULL) rawBody else null
            }

            val moment = Instant.ofEpochMilli(payamak.received).toCompact()
            val detectedDiag = when (detectedClass) {
                PayamakUsabilityClass.Important -> PayamakReportPublicItemInput.DetectedDiagnostics.Important
                PayamakUsabilityClass.Usable -> PayamakReportPublicItemInput.DetectedDiagnostics.Useful
                PayamakUsabilityClass.Spam, PayamakUsabilityClass.Unknown -> PayamakReportPublicItemInput.DetectedDiagnostics.Spam
            }
            val expectedDiag = when (userOpinionClass) {
                PayamakUsabilityClass.Important -> PayamakReportPublicItemInput.ExpectedDiagnostics.Important
                PayamakUsabilityClass.Usable -> PayamakReportPublicItemInput.ExpectedDiagnostics.Useful
                PayamakUsabilityClass.Spam, PayamakUsabilityClass.Unknown -> PayamakReportPublicItemInput.ExpectedDiagnostics.Spam
            }

            val processorReports = item.message.pp.usability.processResults.mapIndexed { index, pr ->
                PayamakReportRuleProcessResultPublicInput(
                    processorName = pr.name,
                    processorVersion = "1.0",
                    generalRank = pr.rate?.toFloat() ?: 0f,
                    userOverrideRank = null,
                    processorProvidedComment = pr.message.ifBlank { pr.name },
                    position = index
                )
            }.ifEmpty {
                listOf(
                    PayamakReportRuleProcessResultPublicInput(
                        processorName = "GeneralEngine",
                        processorVersion = "1.0",
                        generalRank = item.message.pp.usability.rate.probability.toFloat(),
                        userOverrideRank = null,
                        processorProvidedComment = "Engine result",
                        position = 0
                    )
                )
            }

            val calcedRank = item.message.pp.usability.rate.probability.toFloat().coerceIn(0.0f, 1.0f)
            val originStr = payamak.origin.origin()
            val commentStr = item.userComment.trim()

            // 1. Store in local database
            reportQueries.insert(
                origin = originStr,
                body = bodyToSend,
                sha512 = sha512Hash,
                moment = moment,
                detected_diagnostics = detectedDiag.value,
                expected_diagnostics = expectedDiag.value,
                reporter_comment = commentStr,
                calced_rank = calcedRank.toDouble(),
                processors_report = gson.toJson(processorReports),
                created_at = now,
                synced = 0
            )

            val insertedId = reportQueries.lastInsertedId().executeAsOne()
            insertedIds.add(insertedId)

            // Update user origin rank locally
            val rankForOrigin = when (userOpinionClass) {
                PayamakUsabilityClass.Important -> 1.0
                PayamakUsabilityClass.Usable -> 0.8
                PayamakUsabilityClass.Unknown -> 0.5
                PayamakUsabilityClass.Spam -> 0.0
            }
            rankedOriginContract.upsert(payamak.origin, rankForOrigin, OriginRankSource.User)
            item.message.pp.expectedPayamakUsabilityClass = userOpinionClass
            item.message.pp.usability = engine.detect(payamak)

            // Prepare for API submission
            apiItems.add(
                PayamakReportPublicItemInput(
                    origin = originStr,
                    moment = moment,
                    reporterComment = commentStr,
                    detectedDiagnostics = detectedDiag,
                    expectedDiagnostics = expectedDiag,
                    processorsReports = processorReports,
                    body = bodyToSend,
                    sha512 = sha512Hash,
                    calcedRank = calcedRank
                )
            )
        }

        // 2. Call API endpoint
        return try {
            withContext(Dispatchers.IO) {
                val api = PayamakReportsPublicV1ControllerApi()
                api.postPublicPayamakReport(PayamakReportPublicInput(items = apiItems))
            }
            for (id in insertedIds) {
                reportQueries.markAsSynced(id)
            }
            ReportSubmissionResult.Success(items.size)
        } catch (e: Exception) {
            ReportSubmissionResult.SavedLocallyOnly(items.size, e)
        }
    }

    private fun calculateSha512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
