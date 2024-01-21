package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import android.content.ContentValues
import android.provider.Telephony
import androidx.annotation.Keep
import androidx.core.content.contentValuesOf
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class SmsBackup(
    @SerializedName("sub_id")
    val subscriptionId: Long,
    @SerializedName("address")
    val address: String,
    @SerializedName("body")
    val body: String?,
    @SerializedName("date")
    val date: Long,
    @SerializedName("date_sent")
    val dateSent: Long,
    @SerializedName("locked")
    val locked: Int,
    @SerializedName("protocol")
    val protocol: String?,
    @SerializedName("read")
    val read: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("service_center")
    val serviceCenter: String?,
    val backupType: BackupType = BackupType.SMS
) : Serializable {

    fun toContentValues(): ContentValues {
        return contentValuesOf(
            Telephony.Sms.SUBSCRIPTION_ID to subscriptionId,
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE to date,
            Telephony.Sms.DATE_SENT to dateSent,
            Telephony.Sms.LOCKED to locked,
            Telephony.Sms.PROTOCOL to protocol,
            Telephony.Sms.READ to read,
            Telephony.Sms.STATUS to status,
            Telephony.Sms.TYPE to type,
            Telephony.Sms.SERVICE_CENTER to serviceCenter,
        )
    }
}
