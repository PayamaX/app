package no1.payamax.cleanCompose.feutureMessagesDetails.presentation

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import no1.payamax.PayamaxApp
import no1.payamax.cleanCompose.core.presentation.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class MessageDetailsViewModel @Inject constructor(app: PayamaxApp) :
    BaseViewModel<MessageDetailsUIStates>(app, MessageDetailsUIStates()) {

    fun onEvent(event: MessageDetailsEvents) {
        when (event) {
          is MessageDetailsEvents.OnMessageReportWrongDetected -> {
              Log.d("MSG", "onEvent: ${event.msg}")
          }
        }
    }
}