package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import androidx.annotation.Keep
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@Keep
@RealmClass
open class PhoneNumber(
    @PrimaryKey var id: Long = 0,
    var accountType: String? = "",
    var address: String = "",
    var type: String = "",
    var isDefault: Boolean = false
) : RealmObject()