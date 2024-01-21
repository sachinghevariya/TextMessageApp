package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mRepository: DataRepository
) : ViewModel() {


    //---------------------------------------------Data Source--------------------------------------

    fun getConversationByQuery(query: String): List<Conversation> {
        return mRepository.getConversationByQuery(query)
    }

    fun getMessageByQuery(query: String): List<Message> {
        return mRepository.getMessageByQuery(query)
    }


    fun getContactsFromDb(): LiveData<List<SimpleContact>> {
        return mRepository.getContactsFromDb()
    }

    fun checkIfContactNotExist() {
        CoroutineScope(Dispatchers.IO).launch {
            if (mRepository.getContactsCountFromDb() > 0) {

            } else {
                val contactList = mRepository.getContactsFromCursor()
                contactList.forEach { item ->
                    if (item.name == null || item.name.isEmpty()) {
                    } else {
                        mRepository.insertOrUpdateContact(item)
                    }
                }
            }
        }

    }


}