package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

interface Mapper<in From, out To> {
    fun map(from: From): To
}
