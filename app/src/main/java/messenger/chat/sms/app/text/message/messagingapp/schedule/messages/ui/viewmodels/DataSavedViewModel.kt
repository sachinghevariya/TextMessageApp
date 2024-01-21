package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber
import javax.inject.Inject

@HiltViewModel
class DataSavedViewModel @Inject constructor() : ViewModel() {
    @JvmField
    var callback: ((Int) -> Unit)? = null

    @JvmField
    var callbackString: ((String) -> Unit)? = null

    @JvmField
    var phoneNumbers: List<PhoneNumber> = arrayListOf()

    @JvmField
    var isRightAction: Boolean = false


    fun setCallback(callback: (Int) -> Unit) {
        this.callback = callback
    }

    fun invokeCallback(value: Int) {
        callback?.invoke(value)
    }

    fun setCallbackString(callbackString: (String) -> Unit) {
        this.callbackString = callbackString
    }

    fun invokeCallbackString(value: String) {
        callbackString?.invoke(value)
    }

    fun setPhoneNumber(phoneNumbers: List<PhoneNumber>) {
        this.phoneNumbers = phoneNumbers
    }

    fun getPhoneNumber(): List<PhoneNumber> {
        return phoneNumbers
    }

    fun setIsRightAction(isRightAction: Boolean) {
        this.isRightAction = isRightAction
    }

    fun getIsRightAction(): Boolean {
        return isRightAction
    }
}