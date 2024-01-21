package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import androidx.annotation.Keep

@Keep
data class BackupMessage(
    val type: Int,
    val address: String,
    val date: Long,
    val dateSent: Long,
    val read: Boolean,
    val status: Int,
    val body: String,
    val protocol: Int,
    val serviceCenter: String?,
    val locked: Boolean,
    val subId: Int
)