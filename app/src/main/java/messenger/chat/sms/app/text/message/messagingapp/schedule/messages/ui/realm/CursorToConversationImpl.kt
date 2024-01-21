package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import javax.inject.Inject

class CursorToConversationImpl @Inject constructor(
    private val context: Context,
) : CursorToConversation {

    companion object {
        val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
//        val URI: Uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
        val PROJECTION = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.RECIPIENT_IDS
        )

        const val ID = 0
        const val RECIPIENT_IDS = 1
    }

    override fun map(from: Cursor): ConversationNew {
        return ConversationNew().apply {
            id = from.getLong(ID)
            recipients.addAll(from.getString(RECIPIENT_IDS)
                .split(" ")
                .filter { it.isNotBlank() }
                .map { recipientId -> recipientId.toLong() }
                .map { recipientId -> Recipient().apply { id = recipientId } })
        }
    }

    override fun getConversationsCursor(): Cursor? {
        return context.contentResolver.query(URI, PROJECTION, null, null, "date desc")
    }

}