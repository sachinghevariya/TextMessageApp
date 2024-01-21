package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.lifecycle.LiveData
import androidx.room.*
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType

@Dao
interface ConversationsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(conversation: Conversation): Long

    @Update
    fun updateConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE thread_id = :threadId")
    fun deleteThreadId(threadId: Long)

    @Query("SELECT * FROM conversations ORDER BY thread_id DESC")
    fun getAllConversations(): List<Conversation>

    @Query("SELECT * FROM conversations WHERE isArchive = 0 AND is_group_conversation = 0 AND isBlocked = 0 ORDER BY isPin DESC, date DESC")
    fun getAllConversationsLiveData(): LiveData<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isPin = 1 AND is_group_conversation = 0 AND isBlocked = 0 ORDER BY isPin DESC, date DESC")
    fun getAllConversationsPinnedData(): List<Conversation>

    @Query("SELECT * FROM conversations WHERE isArchive = 0 AND is_group_conversation = 0 AND isBlocked = 1 ORDER BY isPin DESC, date DESC")
    fun getAllBlockedConversationsLiveData(): LiveData<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isArchive = 0 AND is_group_conversation = 0 AND isBlocked = 0 AND is_scheduled = 1 ORDER BY isPin DESC, date DESC")
    fun getAllScheduleConversationsLiveData(): LiveData<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE isArchive = 1 AND is_group_conversation = 0 AND isBlocked = 0 ORDER BY isPin DESC, date DESC")
    fun getAllArchivedConversationsLiveData(): LiveData<List<Conversation>>
    @Query("SELECT * FROM conversations WHERE isArchive = 1 AND is_group_conversation = 0 AND isBlocked = 0 ORDER BY isPin DESC, date DESC")
    fun getAllArchivedConversationsData(): List<Conversation>
    @Query("SELECT * FROM conversations WHERE cat_type = :catType ORDER BY thread_id DESC")
    fun getConversationsByCategoryLiveData(catType: MessageType): LiveData<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE thread_id = :threadId LIMIT 1")
    fun getConversationByThreadId(threadId: Long): Conversation

    @Query("SELECT * FROM conversations WHERE title LIKE :query")
    fun getConversationByQuery(query: String): List<Conversation>

    @Query("UPDATE conversations SET read = 1 WHERE thread_id = :threadId")
    fun markAsReadConversation(threadId: Long)

    @Query("UPDATE conversations SET read = 0 WHERE thread_id = :threadId")
    fun markAsUnReadConversation(threadId: Long)

    @Query("UPDATE conversations SET isArchive = 1 WHERE thread_id = :threadId")
    fun moveToArchive(threadId: Long)

    @Query("UPDATE conversations SET isArchive = 0 WHERE thread_id = :threadId")
    fun moveToUnArchive(threadId: Long)

    @Query("UPDATE conversations SET isPin = 1 WHERE thread_id = :threadId")
    fun pinConversation(threadId: Long)

    @Query("UPDATE conversations SET isPin = 0 WHERE thread_id = :threadId")
    fun unPinConversation(threadId: Long)

    @Query("UPDATE conversations SET isBlocked = 1 WHERE thread_id = :threadId")
    fun blockConversation(threadId: Long)

    @Query("UPDATE conversations SET isBlocked = 0 WHERE thread_id = :threadId")
    fun unBlockConversation(threadId: Long)

    @Query("SELECT EXISTS (SELECT 1 FROM conversations WHERE thread_id = :threadId AND isBlocked = 1)")
    fun checkIfThreadIdBlocked(threadId: Long): Boolean

    @Query("SELECT thread_id FROM conversations")
    fun getThreadIdList(): List<Long>
}
