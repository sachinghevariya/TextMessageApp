package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Keep
class Converter {

    @TypeConverter
    fun fromList(contactList: List<SimpleContact>?): String? {
        if (contactList == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(contactList)
    }

    @TypeConverter
    fun toList(contactListString: String?): List<SimpleContact>? {
        if (contactListString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<SimpleContact>>() {}.type
        return gson.fromJson(contactListString, type)
    }

    @TypeConverter
    fun fromMessageList(contactList: List<Message>?): String? {
        if (contactList == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(contactList)
    }

    @TypeConverter
    fun toMessageList(contactListString: String?): List<Message>? {
        if (contactListString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Message>>() {}.type
        return gson.fromJson(contactListString, type)
    }


    @TypeConverter
    fun fromPhoneNumberList(phoneNumberList: List<PhoneNumber>?): String? {
        if (phoneNumberList == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(phoneNumberList)
    }

    @TypeConverter
    fun toPhoneNumberList(phoneNumberListString: String?): List<PhoneNumber>? {
        if (phoneNumberListString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<PhoneNumber>>() {}.type
        return gson.fromJson(phoneNumberListString, type)
    }


    @TypeConverter
    fun fromStringList(stringList: List<String>?): String? {
        if (stringList == null) {
            return null
        }
        val gson = Gson()
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringListString: String?): List<String>? {
        if (stringListString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringListString, type)
    }


}
