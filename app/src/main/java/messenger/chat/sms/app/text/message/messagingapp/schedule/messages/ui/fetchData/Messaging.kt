package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.content.Context
import com.klinker.android.send_message.Settings
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.SmsException.Companion.EMPTY_DESTINATION_ADDRESS
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.SmsException.Companion.ERROR_PERSISTING_MESSAGE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.SmsException.Companion.ERROR_SENDING_MESSAGE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast

fun Context.getSendMessageSettings(): Settings {
    val settings = Settings()
    settings.useSystemSending = true
    settings.deliveryReports = false
    settings.sendLongAsMms = false
    settings.sendLongAsMmsAfter = 1
    settings.group = false
    return settings
}

/** Sends the message using the in-app SmsManager API wrappers if it's an SMS or using android-smsmms for MMS. */
fun Context.sendMessageCompat(
    text: String,
    addresses: List<String>,
    subId: Int?,
    messageId: Long? = null,
    messagingUtils: MessagingUtils
) {
    val settings = getSendMessageSettings()
    if (subId != null) {
        settings.subscriptionId = subId
    }
    try {
        messagingUtils.sendSmsMessage(
            text,
            addresses.toSet(),
            settings.subscriptionId,
            settings.deliveryReports,
            messageId
        )
    } catch (e: SmsException) {
        when (e.errorCode) {
            EMPTY_DESTINATION_ADDRESS -> showToast(getString(R.string.empty_destination_address))
            ERROR_PERSISTING_MESSAGE -> showToast(getString(R.string.unable_to_save_message))
            ERROR_SENDING_MESSAGE -> showToast(
                getString(
                    R.string.unknown_error_occurred_sending_message
                )
            )
        }
    } catch (e: Exception) {
        showToast(e.message ?: "")
    }

}