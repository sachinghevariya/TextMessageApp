package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import androidx.annotation.Keep
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@Keep
@RealmClass
open class Contact(
    @PrimaryKey var lookupKey: String = "",
    var numbers: RealmList<PhoneNumber> = RealmList(),
    var name: String = "",
    var photoUri: String? = null,
    var starred: Boolean = false,
    var lastUpdate: Long = 0
) : RealmObject() {

    fun getDefaultNumber(): PhoneNumber? = numbers.find { number -> number.isDefault }

}