package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.database.Cursor
import android.net.Uri
import javax.inject.Inject

class CursorToRecipientImpl @Inject constructor(
    private val context: Context
) : CursorToRecipient {

    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        const val COLUMN_ID = 0
        const val COLUMN_ADDRESS = 1
    }

    override fun map(from: Cursor) = Recipient(
        id = from.getLong(COLUMN_ID),
        address = from.getString(COLUMN_ADDRESS),
        lastUpdate = System.currentTimeMillis()
    )

    override fun getRecipientCursor(): Cursor? {
        return context.contentResolver.query(URI, null, null, null, null)
    }

    override fun getRecipientCursor(id: Long): Cursor? {
        return context.contentResolver.query(URI, null, "_id = ?", arrayOf(id.toString()), null)
    }

}