package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

class SmsException(val errorCode: Int, val exception: Exception? = null) : Exception() {
    companion object {
        const val EMPTY_DESTINATION_ADDRESS = -1
        const val ERROR_PERSISTING_MESSAGE = -2
        const val ERROR_SENDING_MESSAGE = -3
    }
}
