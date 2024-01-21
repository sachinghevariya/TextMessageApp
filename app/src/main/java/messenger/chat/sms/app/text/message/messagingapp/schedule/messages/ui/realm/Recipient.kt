package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.telephony.PhoneNumberUtils
import androidx.annotation.Keep
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.Locale

@Keep
@RealmClass
open class Recipient(
    @PrimaryKey var id: Long = 0,
    var address: String = "",
    var contact: Contact? = null,
    var lastUpdate: Long = 0
) : RealmObject() {

    fun getDisplayName(): String = contact?.name?.takeIf { it.isNotBlank() }
        ?: PhoneNumberUtils.formatNumber(
            address,
            Locale.getDefault().country
        ) // TODO: Use our own PhoneNumberUtils
        ?: address

}