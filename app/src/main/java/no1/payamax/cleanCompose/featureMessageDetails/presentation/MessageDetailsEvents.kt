package no1.payamax.cleanCompose.featureMessageDetails.presentation

import no1.payamax.cleanCompose.core.presentation.base.BaseEvent
import no1.payamax.model.ReviewableProcessedPayamak

sealed class MessageDetailsEvents: BaseEvent()  {

    data class OnMessageReportWrongDetected(val msg: ReviewableProcessedPayamak) : MessageDetailsEvents()

}