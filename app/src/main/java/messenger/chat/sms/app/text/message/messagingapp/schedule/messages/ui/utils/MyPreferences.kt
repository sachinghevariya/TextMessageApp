package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.content.Context
import android.content.SharedPreferences

//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THEME_DEFAULT

class MyPreferences constructor(context: Context) {
    init {
        sharedPreferences =
            context.getSharedPreferences(CommonClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        editor.apply()
    }

//    var themeSelected: Int
//        get() = sharedPreferences.getInt(CommonClass.KEY_THEME, THEME_DEFAULT)
//        set(themeSelected) {
//            editor.putInt(CommonClass.KEY_THEME, themeSelected)
//            editor.apply()
//        }

//    var uiMode: Int
//        get() = sharedPreferences.getInt(CommonClass.UI_MODE, THEME_DEFAULT)
//        set(uiMode) {
//            editor.putInt(CommonClass.UI_MODE, uiMode)
//            editor.apply()
//        }

    var failedConversation: Int
        get() = sharedPreferences.getInt(CommonClass.FAILED_CONVERSATION, 0)
        set(failedConversation) {
            editor.putInt(CommonClass.FAILED_CONVERSATION, failedConversation)
            editor.apply()
        }

    var language: String?
        get() = sharedPreferences.getString(CommonClass.LANGUAGE, "en")
        set(lang) {
            editor.putString(CommonClass.LANGUAGE, lang)
            editor.apply()
        }

    var backUpFileName: String?
        get() = sharedPreferences.getString(CommonClass.BACKUP_FILE_NAME, "")
        set(backUpFileName) {
            editor.putString(CommonClass.BACKUP_FILE_NAME, backUpFileName)
            editor.apply()
        }

    var swipeRightActionLabel: String?
        get() = sharedPreferences.getString(CommonClass.SWIPE_RIGHT_ACTION_LABEL, "swipeNone")
        set(swipeRightActionLabel) {
            editor.putString(CommonClass.SWIPE_RIGHT_ACTION_LABEL, swipeRightActionLabel)
            editor.apply()
        }

    var swipeLeftActionLabel: String?
        get() = sharedPreferences.getString(CommonClass.SWIPE_LEFT_ACTION_LABEL, "swipeNone")
        set(swipeLeftActionLabel) {
            editor.putString(CommonClass.SWIPE_LEFT_ACTION_LABEL, swipeLeftActionLabel)
            editor.apply()
        }

    var needToUpdateThreadDateList: String?
        get() = sharedPreferences.getString(CommonClass.NEED_TO_UPDATE_THREAD_DATE, "")
        set(needToUpdateThreadDateList) {
            editor.putString(CommonClass.NEED_TO_UPDATE_THREAD_DATE, needToUpdateThreadDateList)
            editor.apply()
        }

    var showProfilePhoto: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.SHOW_PROFILE_PHOTO, true)
        set(showProfilePhoto) {
            editor.putBoolean(CommonClass.SHOW_PROFILE_PHOTO, showProfilePhoto)
            editor.apply()
        }
    var languageShown: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.LANGUAGE_SHOWN, false)
        set(languageShown) {
            editor.putBoolean(CommonClass.LANGUAGE_SHOWN, languageShown)
            editor.apply()
        }

    var showNotificationGroup: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.SHOW_NOTIFICATION_GROUP, true)
        set(showNotificationGroup) {
            editor.putBoolean(CommonClass.SHOW_NOTIFICATION_GROUP, showNotificationGroup)
            editor.apply()
        }
    var showNotificationMessage: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.SHOW_NOTIFICATION_MESSAGE, true)
        set(showNotificationMessage) {
            editor.putBoolean(CommonClass.SHOW_NOTIFICATION_MESSAGE, showNotificationMessage)
            editor.apply()
        }

    var lastUpdateDbTime: Long
        get() = sharedPreferences.getLong(CommonClass.LAST_UPDATE_GB_TIME, 0L)
        set(lastUpdateDbTime) {
            editor.putLong(CommonClass.LAST_UPDATE_GB_TIME, lastUpdateDbTime)
            editor.apply()
        }

    var isAllMessageFetched: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.IS_ALL_MSG_FETCHED, false)
        set(isAllMessageFetched) {
            editor.putBoolean(CommonClass.IS_ALL_MSG_FETCHED, isAllMessageFetched)
            editor.apply()
        }
    var setDefaultApp: Boolean
        get() = sharedPreferences.getBoolean(CommonClass.SET_DEFAULT_APP, false)
        set(setDefaultApp) {
            editor.putBoolean(CommonClass.SET_DEFAULT_APP, setDefaultApp)
            editor.apply()
        }


    companion object {
        private var myPreferences: MyPreferences? = null
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor
        fun getPreferences(context: Context): MyPreferences? {
            if (myPreferences == null) myPreferences = MyPreferences(context)
            return myPreferences
        }
    }
}