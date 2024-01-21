package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.database.Cursor

interface CursorToConversation : Mapper<Cursor, ConversationNew> {

    fun getConversationsCursor(): Cursor?

}