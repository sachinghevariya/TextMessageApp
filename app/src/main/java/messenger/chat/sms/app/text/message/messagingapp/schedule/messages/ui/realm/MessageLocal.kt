//package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm
//
//import android.provider.Telephony
//import androidx.annotation.Keep
//import androidx.room.Ignore
//import io.realm.RealmList
//import io.realm.RealmObject
//import io.realm.annotations.PrimaryKey
//import java.io.Serializable
//
//open class MessageLocal(
//    @PrimaryKey var id: Long = 0,
//    var body: String = "",
//    var type: Int = 0,
//    var status: Int = 0,
//    var participants: RealmList<SimpleContactLocal> = RealmList(),
//    var date: Int = 0,
//    var read: Boolean = false,
//    var threadId: Long = 0,
//    var isMMS: Boolean = false,
//    var senderPhoneNumber: String = "",
//    var senderName: String = "",
//    var senderPhotoUri: String = "",
//    var subscriptionId: Int = 0,
//    var isScheduled: Boolean = false,
//    var isStarred: Boolean = false
//) : RealmObject(), Serializable {
//
//    @Ignore
//    var selected: Boolean = false
//
//    fun isReceivedMessage() = type == Telephony.Sms.MESSAGE_TYPE_INBOX
//
//    fun millis() = date * 1000L
//
//    fun isMe(): Boolean {
//        return type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT || type == Telephony.Sms.MESSAGE_TYPE_OUTBOX
//    }
//}
//
//@Keep
//sealed class ThreadItemNew() : Serializable {
//    data class ThreadLoading(val id: Long) : ThreadItemNew()
//    data class ThreadDateTime(val date: Int, val simID: String) : ThreadItemNew()
//    data class ThreadError(val messageId: Long, val messageText: String) : ThreadItemNew()
//    data class ThreadSent(val messageId: Long, val delivered: Boolean) : ThreadItemNew()
//    data class ThreadSending(val messageId: Long) : ThreadItemNew()
//}