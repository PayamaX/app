package no1.payamax.model

enum class SmsSendStatus {
    NONE,
    SENDING,
    SENT,
    FAILED
}

data class ReviewableProcessedPayamak(
    val pp: ProcessedPayamakModel,
    var selected: Boolean,
    val sendStatus: SmsSendStatus = SmsSendStatus.NONE
)