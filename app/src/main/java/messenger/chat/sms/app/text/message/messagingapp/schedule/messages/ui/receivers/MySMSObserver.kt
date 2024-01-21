package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony

class MySMSObserver(
    private val mContext: Context,
) : ContentObserver(null) {
    init {
        mContext.contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            this
        )
    }


    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        if (uri != null && uri.toString() == Telephony.Sms.CONTENT_URI.toString()) {
            // SMS deletion detected
            // Handle the SMS deletion event here
            // You might want to query the content resolver to get more details about the deleted SMS
            val lastPathSegment = uri.lastPathSegment
            querySmsDetails(uri)
        }
    }


    private fun querySmsDetails(uri: Uri) {
        // Query the content resolver to get details about the changed SMS
        val cursor: Cursor? = mContext.contentResolver.query(
            uri,
            arrayOf(
                Telephony.Sms._ID,          // Message ID
                Telephony.Sms.BODY,         // Message body
                Telephony.Sms.ADDRESS,      // Sender's address
                Telephony.Sms.DATE          // Date of the message
                // Add more fields as needed
            ),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                // Retrieve information about the changed SMS
                val messageId = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                val messageBody = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val senderAddress = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val messageDate = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
            }
        }
    }

}