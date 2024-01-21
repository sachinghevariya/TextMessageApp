package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony.Sms
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.ensureBackgroundThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.refreshMessages
import javax.inject.Inject

/** Handles updating databases and states when a sent SMS message is delivered. */
@AndroidEntryPoint
class SmsStatusDeliveredReceiver : SendStatusReceiver() {

    @Inject
    lateinit var messagingUtils: MessagingUtils

    @Inject
    lateinit var appDb: MessagesDatabase

    private var status: Int = Sms.Sent.STATUS_NONE

    override fun updateAndroidDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        val messageUri: Uri? = intent.data
        val smsMessage = messagingUtils.getSmsMessageFromDeliveryReport(intent) ?: return

        try {
            val format = intent.getStringExtra("format")
            status = smsMessage.status
            // Simple matching up CDMA status with GSM status.
            if ("3gpp2" == format) {
                val errorClass = status shr 24 and 0x03
                val statusCode = status shr 16 and 0x3f
                status = when (errorClass) {
                    0 -> {
                        if (statusCode == 0x02 /*STATUS_DELIVERED*/) {
                            Sms.STATUS_COMPLETE
                        } else {
                            Sms.STATUS_PENDING
                        }
                    }

                    2 -> {
                        // TODO: Need to check whether SC still trying to deliver the SMS to destination and will send the report again?
                        Sms.STATUS_PENDING
                    }

                    3 -> {
                        Sms.STATUS_FAILED
                    }

                    else -> {
                        Sms.STATUS_PENDING
                    }
                }
            }
        } catch (e: NullPointerException) {
            // Sometimes, SmsMessage.mWrappedSmsMessage is null causing NPE when we access
            // the methods on it although the SmsMessage itself is not null.
            return
        }

        updateSmsStatusAndDateSent(context, messageUri, System.currentTimeMillis())
    }

    private fun updateSmsStatusAndDateSent(
        context: Context,
        messageUri: Uri?,
        timeSentInMillis: Long = -1L
    ) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            if (status != Sms.Sent.STATUS_NONE) {
                put(Sms.Sent.STATUS, status)
            }
            put(Sms.Sent.DATE_SENT, timeSentInMillis)
        }

        if (messageUri != null) {
            resolver.update(messageUri, values, null, null)
        } else {
            val cursor = resolver.query(Sms.Sent.CONTENT_URI, null, null, null, "date desc")
            cursor?.use {
                if (cursor.moveToFirst()) {
                    @SuppressLint("Range")
                    val id = cursor.getString(cursor.getColumnIndex(Sms.Sent._ID))
                    val selection = "${Sms._ID} = ?"
                    val selectionArgs = arrayOf(id.toString())
                    resolver.update(Sms.Sent.CONTENT_URI, values, selection, selectionArgs)
                }
            }
        }
    }

    override fun updateAppDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        val messageUri: Uri? = intent.data
        if (messageUri != null) {
            val messageId = messageUri.lastPathSegment?.toLong() ?: 0L
            ensureBackgroundThread {
                if (status != Sms.Sent.STATUS_NONE) {
                    markDelivered(messageId, context)
//                    appDb.getMessagesDao().updateStatus(messageId, status)
//                    refreshMessages()
                }
            }
        }
    }

    private fun markDelivered(id: Long, context: Context) {
        getRealmThread { realm ->
            realm.refresh()

            val message = realm.where(MessageNew::class.java).equalTo("contentId", id).findFirst()
            message?.let {
                // Update the message in realm
                realm.executeTransaction {
                    message.deliveryStatus = Sms.STATUS_COMPLETE
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                }

                // Update the message in the native ContentProvider
                val values = ContentValues()
                values.put(Sms.STATUS, Sms.STATUS_COMPLETE)
                values.put(Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Sms.READ, true)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }
}
