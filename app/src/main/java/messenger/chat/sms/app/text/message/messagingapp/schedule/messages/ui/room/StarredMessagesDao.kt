package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message

@Dao
interface StarredMessagesDao {
    @Query("UPDATE messages SET is_starred = 1  WHERE id = :msgId")
    fun starredMessagesById(msgId: Long)

    @Query("UPDATE messages SET is_starred = 0  WHERE id = :msgId")
    fun unStarredMessagesById(msgId: Long)

    @Query("SELECT * FROM messages WHERE thread_id = :threadId AND id = :messageId AND is_starred = 1 ")
    fun getStarredMessageWithId(threadId: Long, messageId: Long): List<Message>

    @Query("SELECT * FROM messages WHERE is_starred = 1 ORDER BY date")
    fun getStarredMessages(): LiveData<List<Message>>
    @Query("SELECT * FROM messages WHERE is_starred = 1 ORDER BY date")
    fun getStarredMessagesLocal(): List<Message>
}
