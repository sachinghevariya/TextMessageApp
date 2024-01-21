package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
sealed class ThreadItem : Serializable {
    data class ThreadLoading(val id: Long) : ThreadItem()
    data class ThreadDateTime(val date: Int, val simID: String) : ThreadItem()
    data class ThreadError(val messageId: Long, val messageText: String) : ThreadItem()
    data class ThreadSent(val messageId: Long, val delivered: Boolean) : ThreadItem()
    data class ThreadSending(val messageId: Long) : ThreadItem()
}
