package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class BackupDb(
    @SerializedName("messageCount")
    val messageCount: Int,
    @SerializedName("uniqueDeviceId")
    val uniqueDeviceId: String,
    @SerializedName("messages")
    val messages: List<SmsBackup>
) : Serializable