package no1.payamax.modules.report

import no1.payamax.contracts.PayamakUsabilityClass
import no1.payamax.model.ReviewableProcessedPayamak

enum class UsableInfoSharingOption {
    PARTIAL, FULL
}

data class PayamakReportSubmissionItem(
    val message: ReviewableProcessedPayamak,
    val userOpinion: PayamakUsabilityClass,
    val userComment: String
)

data class SavedPayamakReport(
    val payamakId: Long,
    val userOpinion: PayamakUsabilityClass,
    val userComment: String,
)

fun applySavedPayamakReports(
    saved: Map<Long, SavedPayamakReport>,
    payamakIds: Iterable<Long>,
    userComments: MutableMap<Long, String>,
    userOpinions: MutableMap<Long, PayamakUsabilityClass>,
) {
    for (payamakId in payamakIds) {
        val report = saved[payamakId] ?: continue
        if (!userComments.containsKey(payamakId)) {
            userComments[payamakId] = report.userComment
        }
        if (!userOpinions.containsKey(payamakId)) {
            userOpinions[payamakId] = report.userOpinion
        }
    }
}

sealed class ReportSubmissionResult {
    data class Success(val count: Int) : ReportSubmissionResult()
    data class SavedLocallyOnly(val count: Int, val throwable: Throwable) : ReportSubmissionResult()
    data class Failure(val throwable: Throwable) : ReportSubmissionResult()
}

interface PayamakReportContract {
    suspend fun savedReportsByPayamakId(): Map<Long, SavedPayamakReport>

    suspend fun submitBatchReport(
        items: List<PayamakReportSubmissionItem>,
        allowAllSpamInfo: Boolean,
        usableInfoSharingOption: UsableInfoSharingOption
    ): ReportSubmissionResult
}
