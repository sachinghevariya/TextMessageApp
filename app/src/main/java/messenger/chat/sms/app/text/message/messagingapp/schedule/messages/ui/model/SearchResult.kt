package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep

@Keep
data class SearchResult(
    val messageId: Long,
    val title: String,
    val snippet: String,
    val date: Int,
    val threadId: Long,
    var photoUri: String
) {
    companion object {
        fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
            return old.threadId == new.threadId
        }

        fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
            return old.messageId == new.messageId &&
                    old.title == new.title &&
                    old.snippet == new.snippet &&
                    old.date == new.date &&
                    old.photoUri == new.photoUri
        }
    }
}
