package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.database.Cursor

interface CursorToContact : Mapper<Cursor, Contact> {

    fun getContactsCursor(): Cursor?

}