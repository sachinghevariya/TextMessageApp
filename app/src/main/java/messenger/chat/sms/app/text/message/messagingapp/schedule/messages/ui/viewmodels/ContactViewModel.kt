package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    @ApplicationContext val mContext: Context,
    private val mRepository: DataRepository,
    private val myPreferences: MyPreferences
) : ViewModel() {


    //---------------------------------------------Data Source--------------------------------------

    fun getContactsByThreadId(threadId: Long): List<SimpleContact> {
        return mRepository.getContactsByThreadId(threadId)
    }


}