package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep

@Keep
data class BlockedNumber(
    val id: Long,
    val number: String,
    val normalizedNumber: String,
    val numberToCompare: String,
    val contactName: String? = null
)
