package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(
    tableName = "conversations",
    indices = [(Index(value = ["thread_id"], unique = true))]
)
data class Conversation(
    @PrimaryKey @ColumnInfo(name = "thread_id") var threadId: Long,
    @ColumnInfo(name = "snippet") var snippet: String = "",
    @ColumnInfo(name = "date") var date: Int,
    @ColumnInfo(name = "read") var read: Boolean = false,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "photo_uri") var photoUri: String = "",
    @ColumnInfo(name = "is_group_conversation") var isGroupConversation: Boolean = false,
    @ColumnInfo(name = "phone_number") var phoneNumber: String = "",
    @ColumnInfo(name = "cat_type") var categoryType: MessageType = MessageType.OTHER,
    @ColumnInfo(name = "msg_id") var msgId: Long = 0,
    @ColumnInfo(name = "isArchive") var isArchive: Int = 0,
    @ColumnInfo(name = "isPin") var isPin: Int = 0,
    @ColumnInfo(name = "isBlocked") var isBlocked: Int = 0,
    @ColumnInfo(name = "is_scheduled") var isScheduled: Boolean = false
) : Serializable {

    @Ignore
    var message: Message? = null

    @Ignore
    var selected: Boolean = false

    fun isItemPinned(): Boolean {
        return isPin == 1
    }

    constructor(
        threadId: Long,
        snippet: String,
        date: Int,
        read: Boolean,
        title: String,
        photoUri: String,
        isGroupConversation: Boolean,
        phoneNumber: String,
        categoryType: MessageType,
        msgId: Long,
        message: Message?,
        isArchive: Int,
        isPin: Int,
        isBlocked: Int,
        isScheduled: Boolean
    ) : this(
        threadId,
        snippet,
        date,
        read,
        title,
        photoUri,
        isGroupConversation,
        phoneNumber,
        categoryType,
        msgId,
        isArchive,
        isPin,
        isBlocked,
        isScheduled
    ) {
        this.message = message
    }

    companion object {
        fun areItemsTheSame(old: Conversation, new: Conversation): Boolean {
            return old.threadId == new.threadId
        }

        fun areContentsTheSame(old: Conversation, new: Conversation): Boolean {
            return old.snippet == new.snippet &&
                    old.read == new.read &&
                    old.title == new.title &&
                    old.photoUri == new.photoUri &&
                    old.isGroupConversation == new.isGroupConversation &&
                    old.phoneNumber == new.phoneNumber &&
                    old.date == new.date &&
                    old.isPin == new.isPin &&
                    old.msgId == new.msgId &&
                    old.message?.body == new.message?.body &&
                    old.message?.read == new.message?.read &&
                    old.selected == new.selected &&
                    old.isBlocked == new.isBlocked &&
                    old.isScheduled == new.isScheduled
        }

        fun getAdObject(): Conversation {
            return Conversation(
                -1,
                "",
                0,
                true,
                "",
                "",
                false,
                "",
                MessageType.OTHER,
                -1,
                null,
                0,
                0,
                0,
                false
            )
        }

    }
}
