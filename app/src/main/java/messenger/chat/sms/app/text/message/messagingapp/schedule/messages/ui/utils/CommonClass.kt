package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.util.Util.isOnMainThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.RefreshMessages
import org.greenrobot.eventbus.EventBus
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.random.Random


class CommonClass {
    companion object {
        const val AD_URL = "https://ht.askforad.com/"
        const val SHARED_PREFERENCES_NAME = "my_preference"
        const val LANGUAGE = "language"
        const val BACKUP_FILE_NAME = "backUpFileName"
        const val SHOW_PROFILE_PHOTO = "showProfilePhoto"
        const val LANGUAGE_SHOWN = "languageShown"
        const val SHOW_NOTIFICATION_GROUP = "showNotificationGroup"
        const val SHOW_NOTIFICATION_MESSAGE = "showNotificationMessage"
        const val LAST_UPDATE_GB_TIME = "lastUpdateDbTime"
        const val IS_ALL_MSG_FETCHED = "isAllMessageFetched"
        const val SET_DEFAULT_APP = "setDefaultApp"
        const val SWIPE_RIGHT_ACTION_LABEL = "swipeRightActionLabel"
        const val SWIPE_LEFT_ACTION_LABEL = "swipeLeftActionLabel"
        const val NEED_TO_UPDATE_THREAD_DATE = "needToUpdateThreadDateList"
        const val FAILED_CONVERSATION = "failedConversation"
        const val DB_NAME = "message_db.db"
        const val MESSAGES_LIMIT = 30

        const val CONTACT_LIST = "contact_list"
        const val MESSAGE_ID = "message_id"
        const val THREAD_ID = "thread_id"
        const val SCHEDULED_MESSAGE_ID = "scheduled_message_id"
        const val THREAD_TITLE = "thread_title"
        const val THREAD_NUMBER = "thread_number"
        const val SORT_BY_FULL_NAME = 65536
        const val SORT_DESCENDING = 1024
        const val NOTIFICATION_CHANNEL = "messenger"

        //        const val THEME_LIGHT = 33
//        const val THEME_DARK = 11
//        const val THEME_DEFAULT = 22
        const val KEY_THEME = "key_theme"
        const val UI_MODE = "uiMode"
        const val IS_BLOCKED = "is_blocked"
        const val IS_SCHEDULE = "is_schedule"

        const val THREAD_DATE_TIME = 1
        const val THREAD_RECEIVED_MESSAGE = 2
        const val THREAD_SENT_MESSAGE = 3
        const val THREAD_SENT_MESSAGE_ERROR = 4
        const val THREAD_SENT_MESSAGE_SENT = 5
        const val THREAD_SENT_MESSAGE_SENDING = 6
        const val THREAD_LOADING = 7

        const val TIME_FORMAT_12 = "hh:mm a"
        const val TIME_FORMAT_24 = "HH:mm"
        const val DATE_FORMAT_ONE = "dd.MM.yyyy"
        const val DATE_FORMAT_TWO = "dd/MM/yyyy"
        const val DATE_FORMAT_THREE = "MM/dd/yyyy"
        const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
        const val DATE_FORMAT_FIVE = "d MMMM yyyy"
        const val DATE_FORMAT_SIX = "MMMM d yyyy"
        const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
        const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"
        const val DATE_FORMAT_NINE = "yyyyMMdd"
        const val DATE_FORMAT_TEN = "yyyy.MM.dd"
        const val DATE_FORMAT_ELEVEN = "yy-MM-dd"
        const val DATE_FORMAT_TWELVE = "yyMMdd"
        const val DATE_FORMAT_THIRTEEN = "yy.MM.dd"
        const val DATE_FORMAT_FOURTEEN = "yy/MM/dd"
        const val DATE_FORMAT_FIFTEEN = "MMM dd yyyy"

        const val MESSAGE_FILE_TYPE = "application/json"


        private const val PATH =
            "messenger.chat.sms.app.text.message.messagingapp.schedule.messages.action."
        const val MARK_AS_READ = PATH + "mark_as_read"
        const val REPLY = PATH + "reply"

        var detailScreenThreadId = -1L

        var blockedThreadIds: List<Long> = emptyList<Long>()

    }
}

val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun isShortCode(phoneNumber: String): Boolean {
    // Define regex patterns for short codes
    if (phoneNumber.length > 6 && phoneNumber.all { it.isDigit() }) {
        return false
    }

    val shortCodePattern1 = "^(?![0-9]{7,}\$)[0-9]{1,6}\$".toRegex()
    val shortCodePattern2 = "^[0-9]{3,}[A-Za-z]\$".toRegex()
//    val shortCodePattern4 = "^[A-Za-z0-9-]{6,}\$".toRegex()
    val shortCodePattern4 = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9-]*\$".toRegex()
    val shortCodePattern5 = "^[A-Za-z]{2}-\\d+$".toRegex()
    val shortCodePattern6 = "^[A-Za-z]{2}-[A-Za-z-]*\$".toRegex()

    // Check if the phoneNumber matches any of the short code patterns
    return shortCodePattern1.matches(phoneNumber) ||
            shortCodePattern2.matches(phoneNumber) ||
            shortCodePattern4.matches(phoneNumber) ||
            shortCodePattern5.matches(phoneNumber) ||
            shortCodePattern6.matches(phoneNumber)
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

fun generateRandomInt(): Int {
    return Random.nextInt(2, 999999999 + 1) // +1 to include the upper bound
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun showCustomDialog(activity: AppCompatActivity, dialogFragment: DialogFragment) {
    val fragmentManager = activity.supportFragmentManager
    if (!fragmentManager.isStateSaved && !activity.isDestroyed) {
        dialogFragment.show(fragmentManager, dialogFragment.tag)
        fragmentManager.executePendingTransactions()
    }
}

fun generateRandomId(length: Int = 9): Long {
    val millis = System.currentTimeMillis()
    val random = abs(Random(millis).nextLong())
    return random.toString().takeLast(length).toLong()
}

fun getCustomDateTimeInMillis(
    year: Int,
    month: Int,
    dayOfMonth: Int,
    hourOfDay: Int,
    minute: Int
): Long {
    val specificDate = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    return specificDate.time
}

fun getCurrentYearMonthDay(): Triple<Int, Int, Int> {
    val currentTimeMillis = System.currentTimeMillis()
    val currentDate = Date(currentTimeMillis)

    val calendar = Calendar.getInstance().apply {
        time = currentDate
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based, so add 1
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    return Triple(year, month, dayOfMonth)
}

fun getNextDay(currentYear: Int, currentMonth: Int, currentDay: Int): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth - 1) // Month is zero-based, so subtract 1
        set(Calendar.DAY_OF_MONTH, currentDay)
        add(Calendar.DAY_OF_MONTH, 1) // Add one day to get the next day
    }

    val nextYear = calendar.get(Calendar.YEAR)
    val nextMonth = calendar.get(Calendar.MONTH) + 1 // Month is zero-based, so add 1
    val nextDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    return Triple(nextYear, nextMonth, nextDayOfMonth)
}

fun getDeviceId(context: Context): String? {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

fun refreshMessages(isScrollBottom: Boolean = false) {
    EventBus.getDefault().post(RefreshMessages(isScrollBottom))
}