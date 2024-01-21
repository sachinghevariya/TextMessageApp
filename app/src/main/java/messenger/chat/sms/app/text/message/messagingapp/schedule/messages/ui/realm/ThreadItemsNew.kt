package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import androidx.annotation.Keep
import java.io.Serializable

@Keep
open class ThreadItemNew : Serializable {
    data class ThreadLoading(val id: Long) : ThreadItemNew()
    data class ThreadDateTime(val date: Int, val simID: String) : ThreadItemNew()
    data class ThreadError(val messageId: Long, val messageText: String) : ThreadItemNew()
    data class ThreadSent(val messageId: Long, val delivered: Boolean) : ThreadItemNew()
    data class ThreadSending(val messageId: Long) : ThreadItemNew()
}
