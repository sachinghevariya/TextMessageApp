package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import androidx.annotation.Keep

@Keep
data class Backup(
    val messageCount: Int = 0,
    val messages: List<BackupMessage> = listOf()
)