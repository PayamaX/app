package no1.payamax.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import no1.payamax.model.ProcessedPayamakModel

class MessagesViewModel(msgs: MutableLiveData<List<ProcessedPayamakModel>>) : ViewModel() {
    val messages: LiveData<List<ProcessedPayamakModel>> = msgs
}