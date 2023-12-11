package no1.payamax.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import no1.payamax.model.MessageModel

class MessagesViewModel(msgs: MutableLiveData<List<MessageModel>>) : ViewModel() {
    val messages: LiveData<List<MessageModel>> = msgs
}