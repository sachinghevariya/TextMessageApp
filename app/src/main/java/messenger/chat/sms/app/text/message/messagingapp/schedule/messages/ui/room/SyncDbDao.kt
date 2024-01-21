package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb

@Dao
interface SyncDbDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(syncDb: SyncDb)

    @Query("Select * from sync")
    fun getSyncDate(): List<SyncDb>
}
