package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData

@Keep
class DataContainer {
    val conversationsLiveData = MutableLiveData<List<Conversation>>()
    val archivedConversationsLiveData = MutableLiveData<List<Conversation>>()
    val blockedConversationsLiveData = MutableLiveData<List<Conversation>>()
    val scheduledConversationsLiveData = MutableLiveData<List<Conversation>>()
    val starredConversationsLiveData = MutableLiveData<List<Conversation>>()
    val progressLiveData = MutableLiveData<Int>()

    fun updateProgress(progress: Int) {
        progressLiveData.postValue(progress)
    }

    companion object {
        private var instance: DataContainer? = null

        fun getInstance(): DataContainer {
            if (instance == null) {
                instance = DataContainer()
            }
            return instance!!
        }
    }
}
