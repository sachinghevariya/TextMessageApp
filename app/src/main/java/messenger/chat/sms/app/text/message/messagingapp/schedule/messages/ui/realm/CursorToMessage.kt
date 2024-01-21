package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.database.Cursor
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import android.provider.Telephony.Sms

interface CursorToMessage : Mapper<Pair<Cursor, CursorToMessage.MessageColumns>, MessageNew> {

    fun getMessagesCursor(): Cursor?

    fun getMessageCursor(id: Long): Cursor?

    class MessageColumns(private val cursor: Cursor) {

        val msgType by lazy { getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) }
        val msgId by lazy { getColumnIndex(Sms._ID) }
        val date by lazy { getColumnIndex(Sms.DATE) }
        val dateSent by lazy { getColumnIndex(Sms.DATE_SENT) }
        val read by lazy { getColumnIndex(Sms.READ) }
        val threadId by lazy { getColumnIndex(Sms.THREAD_ID) }
        val locked by lazy { getColumnIndex(Sms.LOCKED) }
        val subId by lazy { getColumnIndex(Sms.SUBSCRIPTION_ID) }

        val smsAddress by lazy { getColumnIndex(Sms.ADDRESS) }
        val smsBody by lazy { getColumnIndex(Sms.BODY) }
        val smsSeen by lazy { getColumnIndex(Sms.SEEN) }
        val smsType by lazy { getColumnIndex(Sms.TYPE) }
        val smsStatus by lazy { getColumnIndex(Sms.STATUS) }
        val smsErrorCode by lazy { getColumnIndex(Sms.ERROR_CODE) }

        val mmsSubject by lazy { getColumnIndex(Mms.SUBJECT) }
        val mmsSubjectCharset by lazy { getColumnIndex(Mms.SUBJECT_CHARSET) }
        val mmsSeen by lazy { getColumnIndex(Mms.SEEN) }
        val mmsMessageType by lazy { getColumnIndex(Mms.MESSAGE_TYPE) }
        val mmsMessageBox by lazy { getColumnIndex(Mms.MESSAGE_BOX) }
        val mmsDeliveryReport by lazy { getColumnIndex(Mms.DELIVERY_REPORT) }
        val mmsReadReport by lazy { getColumnIndex(Mms.READ_REPORT) }
        val mmsErrorType by lazy { getColumnIndex(MmsSms.PendingMessages.ERROR_TYPE) }
        val mmsStatus by lazy { getColumnIndex(Mms.STATUS) }

        private fun getColumnIndex(columnsName: String) = try {
            cursor.getColumnIndexOrThrow(columnsName)
        } catch (e: Exception) {
            -1
        }
    }
}
