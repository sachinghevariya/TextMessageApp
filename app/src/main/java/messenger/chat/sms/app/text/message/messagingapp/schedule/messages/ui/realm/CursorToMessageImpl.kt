package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import android.provider.Telephony.Sms
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPersister
import javax.inject.Inject

class CursorToMessageImpl @Inject constructor(
    private val context: Context,
    private val keys: KeyManager
) : CursorToMessage {

    private val uri = Uri.parse("content://mms-sms/complete-conversations")
//    private val uri = Telephony.Sms.CONTENT_URI
    private val projection = arrayOf(
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        MmsSms._ID,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.THREAD_ID,
        Mms.LOCKED,

       /* Sms._ID,
        Sms.BODY,
        Sms.TYPE,
        Sms.ADDRESS,
        Sms.DATE,
        Sms.READ,
        Sms.THREAD_ID,
        Sms.SUBSCRIPTION_ID,
        Sms.STATUS,*/


        Sms.ADDRESS,
        Sms.BODY,
        Sms.SEEN,
        Sms.TYPE,
        Sms.STATUS,
        Sms.ERROR_CODE,

        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.SEEN,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        MmsSms.PendingMessages.ERROR_TYPE,
        Mms.STATUS
    )

    override fun map(from: Pair<Cursor, CursorToMessage.MessageColumns>): MessageNew {
        val cursor = from.first
        val columnsMap = from.second

        return MessageNew().apply {
            type = when {
                cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(
                    columnsMap.msgType
                )

                cursor.getColumnIndex(Mms.SUBJECT) != -1 -> "mms"
                cursor.getColumnIndex(Sms.ADDRESS) != -1 -> "sms"
                else -> "unknown"
            }
//            type = "sms"
            id = keys.newId()
            threadId = cursor.getLong(columnsMap.threadId)
            contentId = cursor.getLong(columnsMap.msgId)
            date = cursor.getLong(columnsMap.date)
//            dateSent = cursor.getLong(columnsMap.dateSent)
            dateSent = date
            read = cursor.getInt(columnsMap.read) != 0
//            locked = cursor.getInt(columnsMap.locked) != 0
            subId = if (columnsMap.subId != -1) cursor.getInt(columnsMap.subId) else -1
            isStarred = false

            when (type) {
                "sms" -> {
                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0

                    body = columnsMap.smsBody
                        .takeIf { column -> column != -1 } // The column may not be set
                        ?.let { column -> cursor.getString(column) }
                        ?: "" // cursor.getString() may return null

                    errorCode = cursor.getInt(columnsMap.smsErrorCode)
                    deliveryStatus = cursor.getInt(columnsMap.smsStatus)
                }

                "mms" -> {
                    address = getMmsAddress(contentId)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date *= 1000L
                    dateSent *= 1000L
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport) ?: ""
                    errorType =
                        if (columnsMap.mmsErrorType != -1) cursor.getInt(columnsMap.mmsErrorType) else 0
                    messageSize = 0
                    readReportString = cursor.getString(columnsMap.mmsReadReport) ?: ""
                    messageType = cursor.getInt(columnsMap.mmsMessageType)
                    mmsStatus = cursor.getInt(columnsMap.mmsStatus)
                    val subjectCharset = cursor.getInt(columnsMap.mmsSubjectCharset)
                    subject = cursor.getString(columnsMap.mmsSubject)
                        ?.takeIf { it.isNotBlank() }
                        ?.let(PduPersister::getBytes)
                        ?.let { EncodedStringValue(subjectCharset, it).string } ?: ""
                    textContentType = ""
                    attachmentType = MessageNew.AttachmentType.NOT_LOADED
                }
            }
        }
    }

    override fun getMessagesCursor(): Cursor? {
        /*return SqliteWrapper.query(
            context,
            uri,
            this.projection,
            sortOrder = "normalized_date desc"
        )*/
        return SqliteWrapper.query(
            context,
            uri,
            this.projection,
            sortOrder = ""
        )
    }

    override fun getMessageCursor(id: Long): Cursor? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getMmsAddress(messageId: Long): String {
        val uri = Mms.CONTENT_URI.buildUpon()
            .appendPath(messageId.toString())
            .appendPath("addr").build()

        //TODO: Use Charset to ensure address is decoded correctly
        val projection = arrayOf(Mms.Addr.ADDRESS, Mms.Addr.CHARSET)
        val selection = "${Mms.Addr.TYPE} = ${PduHeaders.FROM}"

        val cursor = context.contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getString(0) ?: ""
            }
        }

        return ""
    }

}
