package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage

@Dao
interface ScheduleMessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(message: ScheduleMessage)

    @Query("DELETE FROM scheduleMessages WHERE id = :msgId")
    fun deleteMessagesById(msgId: Long)

    @Query("SELECT * FROM scheduleMessages WHERE thread_id = :threadId AND id = :messageId AND is_scheduled = 1")
    fun getScheduledMessageWithId(threadId: Long, messageId: Long): List<ScheduleMessage>

    @Query("SELECT * FROM scheduleMessages WHERE is_scheduled = 1 ORDER BY date ASC")
    fun getScheduledMessages(): LiveData<List<ScheduleMessage>>

}
