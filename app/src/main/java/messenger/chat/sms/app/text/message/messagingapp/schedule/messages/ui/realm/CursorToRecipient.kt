package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.database.Cursor

interface CursorToRecipient : Mapper<Cursor, Recipient> {

    fun getRecipientCursor(): Cursor?

    fun getRecipientCursor(id: Long): Cursor?

}