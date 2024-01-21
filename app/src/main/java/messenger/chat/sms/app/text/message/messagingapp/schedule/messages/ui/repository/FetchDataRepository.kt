package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb
import javax.inject.Inject

class FetchDataRepository @Inject constructor(
    @ApplicationContext val mContext: Context,
    val mRepository: DataRepository,
    val mAppDb: MessagesDatabase
) {

    fun getConversationFromDb(): List<Conversation> {
        return mAppDb.getConversationsDao().getAllConversations()
    }

    fun getConversationByThreadId(threadId: Long): Conversation {
        return mAppDb.getConversationsDao().getConversationByThreadId(threadId)
    }

    fun updateConversation(conversation: Conversation) {
        mAppDb.getConversationsDao().insertOrUpdate(conversation)
    }

    fun deleteConversation(threadId: Long) {
        mAppDb.getConversationsDao().deleteThreadId(threadId)
    }

    fun markAsReadConversation(threadId: Long) {
        mAppDb.getConversationsDao().markAsReadConversation(threadId)
    }

    fun insertOrUpdateSynDb(syncDb: SyncDb) {
        mAppDb.getSyncDbDao().insertOrUpdate(syncDb)
    }

    fun deleteMessageByThreadId(threadId: Long) {
        mAppDb.getMessagesDao().deleteAllThreadMessages(threadId)
    }

}