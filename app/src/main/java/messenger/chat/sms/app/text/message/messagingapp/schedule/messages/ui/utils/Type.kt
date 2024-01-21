package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.format.Time
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun String.areDigitsOnly() = matches(Regex("[0-9]+"))

fun String.areLettersOnly() = matches(Regex("[a-zA-Z]+"))
fun String.normalizeString() =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")

fun String.normalizePhoneNumber() = PhoneNumberUtils.normalizeNumber(this)

// checks if string is a phone number
fun String.isPhoneNumber(): Boolean {
    return this.matches("^[0-9+\\-\\)\\( *#]+\$".toRegex())
}

// if we are comparing phone numbers, compare just the last 9 digits
fun String.trimToComparableNumber(): String {
    // don't trim if it's not a phone number
    if (!this.isPhoneNumber()) {
        return this
    }
    val normalizedNumber = this.normalizeString()
    val startIndex = Math.max(0, normalizedNumber.length - 9)
    return normalizedNumber.substring(startIndex)
}

fun ArrayList<SimpleContact>.getAddresses() =
    flatMap { it.phoneNumbers }.map { it.normalizedNumber }

fun Int.formatDateOrTime(
    context: Context,
    hideTimeAtOtherDays: Boolean,
    showYearEvenIfCurrent: Boolean,
    hideSevenDays: Boolean
): String {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.timeInMillis = this * 1000L

    val currentDate = Calendar.getInstance(Locale.getDefault())
    val difference = currentDate.timeInMillis - cal.timeInMillis
    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val daysDifference = difference / (1000 * 60 * 60 * 24)

    if (hideSevenDays) {
        return if (DateUtils.isToday(this * 1000L)) {
            DateFormat.format(context.getTimeFormat(), cal).toString()
        } else {
            var format = getDefaultDateFormat(context)
            if (!showYearEvenIfCurrent && isThisYear()) {
                format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
            }
            if (!hideTimeAtOtherDays) {
                format += ", ${context.getTimeFormat()}"
            }
            DateFormat.format(format, cal).toString()
        }
    } else {
        return if (DateUtils.isToday(this * 1000L)) {
            DateFormat.format(context.getTimeFormat(), cal).toString()
        } else if (daysDifference <= 7) {
            if (difference < 0) {
                var format = getDefaultDateFormat(context)
                if (!showYearEvenIfCurrent && isThisYear()) {
                    format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
                }
                if (!hideTimeAtOtherDays) {
                    format += ", ${context.getTimeFormat()}"
                }
                DateFormat.format(format, cal).toString()
            } else {
                dateFormat.format(cal.timeInMillis)
            }
        } else {
            var format = getDefaultDateFormat(context)
            if (!showYearEvenIfCurrent && isThisYear()) {
                format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
            }
            if (!hideTimeAtOtherDays) {
                format += ", ${context.getTimeFormat()}"
            }
            DateFormat.format(format, cal).toString()
        }
    }
}

private fun getDefaultDateFormat(context: Context): String {
    val format = DateFormat.getDateFormat(context)
    val pattern = (format as SimpleDateFormat).toLocalizedPattern()
    return when (pattern.lowercase().replace(" ", "")) {
        "d.M.y" -> CommonClass.DATE_FORMAT_ONE
        "dd/mm/y" -> CommonClass.DATE_FORMAT_TWO
        "mm/dd/y" -> CommonClass.DATE_FORMAT_THREE
        "y-mm-dd" -> CommonClass.DATE_FORMAT_FOUR
        "dmmmmy" -> CommonClass.DATE_FORMAT_FIVE
        "mmmmdy" -> CommonClass.DATE_FORMAT_SIX
        "mm-dd-y" -> CommonClass.DATE_FORMAT_SEVEN
        "dd-mm-y" -> CommonClass.DATE_FORMAT_EIGHT
        else -> CommonClass.DATE_FORMAT_FIFTEEN
    }
}

fun Int.isThisYear(): Boolean {
    val time = Time()
    time.set(this * 1000L)

    val thenYear = time.year
    time.set(System.currentTimeMillis())

    return (thenYear == time.year)
}