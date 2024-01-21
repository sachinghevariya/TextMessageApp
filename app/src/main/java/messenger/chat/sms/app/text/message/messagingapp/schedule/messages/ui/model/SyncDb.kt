package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity(tableName = "sync")
data class SyncDb(
    @PrimaryKey val id: Long = 0,
    @ColumnInfo(name = "date") val date: Int,
) : Serializable
