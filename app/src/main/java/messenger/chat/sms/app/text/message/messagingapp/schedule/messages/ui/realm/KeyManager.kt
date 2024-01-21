package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

interface KeyManager {
    fun reset()
    fun newId(): Long

}