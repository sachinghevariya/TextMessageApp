package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import javax.inject.Inject

@HiltViewModel
class NewConversationViewModel @Inject constructor(
    private val mRepository: DataRepository
) : ViewModel() {

    //---------------------------------------------Data Source--------------------------------------

    fun getContactsFromDb(): LiveData<List<SimpleContact>> {
        return mRepository.getContactsFromDb()
    }

    fun getContactsFromDbLocal(): List<SimpleContact> {
        return mRepository.getContactsFromDbLocal()
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