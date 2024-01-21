package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep

@Keep
enum class MessageType {
    ALL,
    PERSONAL,
    OFFER,
    OTP,
    TRANSACTION,
    OTHER
}