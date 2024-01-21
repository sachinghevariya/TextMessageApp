package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(simpleContact: SimpleContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContacts(vararg simpleContact: SimpleContact)

    @Delete
    fun delete(simpleContact: SimpleContact)

    @Query("Select * from SimpleContact")
    fun getAllContacts(): LiveData<List<SimpleContact>>

    @Query("SELECT COUNT(contactId) FROM SimpleContact")
    fun getContactsCountFromDb(): Int

    @Query("Select * from SimpleContact")
    fun getDbContacts(): List<SimpleContact>


    @Query("Select * from SimpleContact where thread_id = :threadId")
    fun getContactByThreadId(threadId: Long): List<SimpleContact>
}
