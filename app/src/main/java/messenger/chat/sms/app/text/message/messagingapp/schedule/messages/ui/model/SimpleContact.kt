package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import android.telephony.PhoneNumberUtils
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.SORT_BY_FULL_NAME
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.SORT_DESCENDING
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizePhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizeString
import java.io.Serializable

@Keep
@Entity
data class SimpleContact(
    @ColumnInfo(name = "rawId") val rawId: Int,
    @PrimaryKey @ColumnInfo(name = "contactId") val contactId: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "photoUri") var photoUri: String,
    @ColumnInfo(name = "phoneNumbers") var phoneNumbers: List<PhoneNumber>,
    @ColumnInfo(name = "birthdays") var birthdays: List<String>,
    @ColumnInfo(name = "anniversaries") var anniversaries: List<String>,
    @ColumnInfo(name = "thread_id") var threadId: Long = -1
) : Comparable<SimpleContact>, Serializable {

    companion object {
        var sorting = -1


        fun areItemsTheSame(old: SimpleContact, new: SimpleContact): Boolean {
            return old.rawId == new.rawId
        }

        fun areContentsTheSame(old: SimpleContact, new: SimpleContact): Boolean {
            return old.rawId == new.rawId &&
                    old.contactId == new.contactId &&
                    old.name == new.name &&
                    old.photoUri == new.photoUri &&
                    old.phoneNumbers == new.phoneNumbers &&
                    old.birthdays == new.birthdays &&
                    old.anniversaries == new.anniversaries &&
                    old.threadId == new.threadId
        }
    }

    override fun compareTo(other: SimpleContact): Int {
        if (sorting == -1) {
            return compareByFullName(other)
        }

        var result = when {
            sorting and SORT_BY_FULL_NAME != 0 -> compareByFullName(other)
            else -> rawId.compareTo(other.rawId)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }

    private fun compareByFullName(other: SimpleContact): Int {
        val firstString = name.normalizeString()
        val secondString = other.name.normalizeString()

        return if (firstString.firstOrNull()?.isLetter() == true && secondString.firstOrNull()
                ?.isLetter() == false
        ) {
            -1
        } else if (firstString.firstOrNull()?.isLetter() == false && secondString.firstOrNull()
                ?.isLetter() == true
        ) {
            1
        } else {
            if (firstString.isEmpty() && secondString.isNotEmpty()) {
                1
            } else if (firstString.isNotEmpty() && secondString.isEmpty()) {
                -1
            } else {
                firstString.compareTo(secondString, true)
            }
        }
    }

    fun doesContainPhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber.contains(text)
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                            phoneNumber.contains(text) ||
                            phoneNumber.normalizePhoneNumber().contains(normalizedText) ||
                            phoneNumber.contains(normalizedText)
                }
            }
        } else {
            false
        }
    }

    fun doesHavePhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    phoneNumber == text
                }
            } else {
                phoneNumbers.map { it.normalizedNumber }.any { phoneNumber ->
                    PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                            phoneNumber == text ||
                            phoneNumber.normalizePhoneNumber() == normalizedText ||
                            phoneNumber == normalizedText
                }
            }
        } else {
            false
        }
    }


}
