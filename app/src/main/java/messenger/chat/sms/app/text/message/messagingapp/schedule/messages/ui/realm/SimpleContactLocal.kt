//package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm
//
//import android.telephony.PhoneNumberUtils
//import androidx.annotation.Keep
//import io.realm.RealmList
//import io.realm.RealmObject
//import io.realm.annotations.PrimaryKey
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizePhoneNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizeString
//
//@Keep
//open class SimpleContactLocal(
//    var rawId: Int = 0,
//    @PrimaryKey var contactId: Int = 0,
//    var name: String = "",
//    var photoUri: String = "",
//    var phoneNumbers: RealmList<PhoneNumberLocal> = RealmList(),
//    var birthdays: RealmList<String> = RealmList(),
//    var anniversaries: RealmList<String> = RealmList(),
//    var threadId: Long = -1
//) : RealmObject() {
//
//    companion object {
//        var sorting = -1
//
//
//        fun areItemsTheSame(old: SimpleContactLocal, new: SimpleContactLocal): Boolean {
//            return old.rawId == new.rawId
//        }
//
//        fun areContentsTheSame(old: SimpleContactLocal, new: SimpleContactLocal): Boolean {
//            return old.rawId == new.rawId &&
//                    old.contactId == new.contactId &&
//                    old.name == new.name &&
//                    old.photoUri == new.photoUri &&
//                    old.phoneNumbers == new.phoneNumbers &&
//                    old.birthdays == new.birthdays &&
//                    old.anniversaries == new.anniversaries &&
//                    old.threadId == new.threadId
//        }
//    }
//
//
//    private fun compareByFullName(other: SimpleContactLocal): Int {
//        val firstString = name.normalizeString()
//        val secondString = other.name.normalizeString()
//
//        return if (firstString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()
//                ?.isLetter() == false
//        ) {
//            -1
//        } else if (firstString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()
//                ?.isLetter() == true
//        ) {
//            1
//        } else {
//            if (firstString.isEmpty() && secondString.isNotEmpty()) {
//                1
//            } else if (firstString.isNotEmpty() && secondString.isEmpty()) {
//                -1
//            } else {
//                firstString.compareTo(secondString, true)
//            }
//        }
//    }
//
//    fun doesContainPhoneNumber(text: String): Boolean {
//        return if (text.isNotEmpty()) {
//            val normalizedText = text.normalizePhoneNumber()
//            if (normalizedText.isEmpty()) {
//                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
//                    phoneNumber.contains(text)
//                }
//            } else {
//                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
//                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
//                            phoneNumber.contains(text) ||
//                            phoneNumber.normalizePhoneNumber().contains(normalizedText) ||
//                            phoneNumber.contains(normalizedText)
//                }
//            }
//        } else {
//            false
//        }
//    }
//
//    fun doesHavePhoneNumber(text: String): Boolean {
//        return if (text.isNotEmpty()) {
//            val normalizedText = text.normalizePhoneNumber()
//            if (normalizedText.isEmpty()) {
//                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
//                    phoneNumber == text
//                }
//            } else {
//                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
//                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
//                            phoneNumber == text ||
//                            phoneNumber.normalizePhoneNumber() == normalizedText ||
//                            phoneNumber == normalizedText
//                }
//            }
//        } else {
//            false
//        }
//    }
//
//
//}
//
//
//open class PhoneNumberLocal(
//    var value: String = "",
//    var type: Int = 0,
//    var label: String = "",
//    var normalizedNumber: String = "",
//    var isPrimary: Boolean = false
//):RealmObject()