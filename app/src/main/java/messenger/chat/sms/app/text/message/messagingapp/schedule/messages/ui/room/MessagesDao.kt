package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message

@Dao
interface MessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(vararg message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBulkMessages(message: List<Message>)

    @Query("SELECT * FROM messages WHERE thread_id = :threadId")
    fun getThreadMessages(threadId: Long): LiveData<List<Message>>

    @Query("UPDATE messages SET status = :status WHERE id = :id ")
    fun updateStatus(id: Long, status: Int): Int

    @Query("UPDATE messages SET type = :type WHERE id = :id")
    fun updateType(id: Long, type: Int): Int

    @Query("SELECT * FROM messages WHERE thread_id = :threadId")
    fun getMessagesByThreadId(threadId: Long): List<Message>

    @Query("SELECT * FROM messages WHERE id = :msgId LIMIT 1")
    fun getMessageById(msgId: Long): Message

    @Query("SELECT * FROM messages WHERE body LIKE :query")
    fun getMessageByQuery(query: String): List<Message>

    @Query("UPDATE messages SET read = 1 WHERE id = :id")
    fun markAsReadMessage(id: Long)

    @Query("UPDATE messages SET read = 0 WHERE id = :id")
    fun markAsUnReadMessage(id: Long)

    @Query("UPDATE messages SET read = 1 WHERE thread_id = :id")
    fun markAllMessageThreadRead(id: Long)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    fun getMsgById(id: Long): Message

    @Query("SELECT * FROM messages WHERE thread_id = :id ORDER BY date DESC LIMIT 1")
    fun getMsgByThreadId(id: Long): Message

    @Query("DELETE FROM messages WHERE thread_id = :threadId")
    fun deleteAllThreadMessages(threadId: Long)

    @Query("DELETE FROM messages WHERE id = :msgId")
    fun deleteMessagesById(msgId: Long)

    @Query("SELECT * FROM messages WHERE thread_id = :threadId AND id = :messageId AND is_scheduled = 1")
    fun getScheduledMessageWithId(threadId: Long, messageId: Long): List<Message>

    @Query("SELECT * FROM messages WHERE is_scheduled = 1")
    fun getScheduledMessages(): List<Message>

    @Query("SELECT * FROM messages")
    fun getAllMessages(): List<Message>

    @Query("SELECT COUNT(*) FROM messages WHERE thread_id = :threadId")
    fun getStoredMessageCount(threadId: Long): Int

    @Query("SELECT MAX(date) FROM messages")
    fun getLatestMessageTimestamp(): Long

    @Query("SELECT id FROM messages WHERE thread_id = :threadId")
    fun getMessageIdList(threadId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessageCount(): Int

    @Query("SELECT * FROM messages WHERE thread_id = :threadId ORDER BY date DESC LIMIT 1")
    fun getLastMessageByThreadId(threadId: Long): Message

}
