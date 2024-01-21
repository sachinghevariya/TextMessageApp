package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessagesForConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val mRepository: DataRepository
) : ViewModel() {


    fun getThreadMessages(threadId: Long): LiveData<List<Message>> {
        return mRepository.getThreadMessages(threadId)
    }

    fun getMessagesByThreadId(threadId: Long): List<Message> {
        return mRepository.getMessagesByThreadId(threadId)
    }

    fun insertOrUpdateMessages(message: Message) {
        mRepository.insertOrUpdateMessages(message)
    }

    fun insertOrUpdateScheduleMessage(message: ScheduleMessage) {
        mRepository.insertOrUpdateScheduleMessage(message)
    }

    fun checkIfThreadIdBlocked(threadId: Long): Boolean {
        return mRepository.checkIfThreadIdBlocked(threadId)
    }

    fun getConversationByThreadId(threadId: Long): Conversation {
        return mRepository.getConversationByThreadId(threadId)
    }

    fun insertOrUpdateConversation(conversation: Conversation) {
        mRepository.insertOrUpdateConversation(conversation)
    }

    fun unblockConversation(threadId: Long) {
        mRepository.unblockConversation(threadId)
    }

    fun markAsReadMessage(id: Long) {
        mRepository.markAsReadMessage(id)
    }

    fun markAsReadConversation(threadId: Long) {
        mRepository.markAsReadConversation(threadId)
    }

    fun deleteConversationByThreadId(threadId: Long) {
        mRepository.deleteConversation(threadId)
    }

    fun deleteMessageByThreadId(threadId: Long) {
        mRepository.deleteMessageByThreadId(threadId)
    }

    fun deleteSelectedMessage(msgId: Long) {
        mRepository.deleteMessagesById(msgId)
    }

    fun addToBlockConversation(threadId: Long) {
        mRepository.addToBlockConversation(threadId)
    }

    fun starredMessageById(msgId: Long) {
        mRepository.starredMessageById(msgId)
    }

    fun unStarredMessageById(msgId: Long) {
        mRepository.unStarredMessageById(msgId)
    }

    fun getStoredMessageCount(threadId: Long): Int {
        return mRepository.getStoredMessageCount(threadId)
    }


    //------------------------------------------Data Source--------------------------------------------

    fun getMessageFromCursorByThreadId(
        threadId: Long,
        oldestMessageDate: Int,
        limit: Int,
        offSet: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val messages = mRepository.mContext.getMessagesForConversation(
                threadId, oldestMessageDate, limit = limit, offset = offSet,
                mRepository = mRepository
            )
            mRepository.insertBulkMessages(messages)
        }
    }

    fun getMessageFromCursorByThreadId(threadId: Long, oldestMessageDate: Int): List<Message> {
        val messages = mRepository.mContext.getMessagesForConversation(
            threadId,
            oldestMessageDate,
            mRepository = mRepository
        )
        mRepository.insertBulkMessages(messages)
        return messages
    }

}