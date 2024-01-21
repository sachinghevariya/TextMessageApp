package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getContactsFromCursorN
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getConversations
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessages
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.DataContainer
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb
import javax.inject.Inject

class DataRepository @Inject constructor(
    @ApplicationContext val mContext: Context,
    val mAppDb: MessagesDatabase
) {
    val dataContainer: DataContainer = DataContainer.getInstance()

    fun getConversationsFromCursor(): ArrayList<Conversation> {
        return mContext.getConversations()
    }

    fun getMessagesFromCursor(threadId: Long): ArrayList<Message> {
        return mContext.getMessages(threadId)
    }

    fun getConversationFromDb(): List<Conversation> {
        return mAppDb.getConversationsDao().getAllConversations()
    }

    fun getAllConversationsLiveData(): LiveData<List<Conversation>> {
        return mAppDb.getConversationsDao().getAllConversationsLiveData()
    }

    fun getAllScheduleConversationsLiveData(): LiveData<List<ScheduleMessage>> {
        return mAppDb.getScheduleMessageDao().getScheduledMessages()
    }

    fun getAllStarredConversationsLiveData(): LiveData<List<Message>> {
        return mAppDb.getStarredMessageDao().getStarredMessages()
    }

    fun getAllBlockedConversationsLiveData(): LiveData<List<Conversation>> {
        return mAppDb.getConversationsDao().getAllBlockedConversationsLiveData()
    }

    fun getAllArchivedConversationsLiveData(): LiveData<List<Conversation>> {
        return mAppDb.getConversationsDao().getAllArchivedConversationsLiveData()
    }

    fun getConversationByThreadId(threadId: Long): Conversation {
        return mAppDb.getConversationsDao().getConversationByThreadId(threadId)
    }

    fun checkIfThreadIdBlocked(threadId: Long): Boolean {
        return mAppDb.getConversationsDao().checkIfThreadIdBlocked(threadId)
    }

    fun getConversationByQuery(query: String): List<Conversation> {
        return mAppDb.getConversationsDao().getConversationByQuery(query)
    }

    fun updateConversation(conversation: Conversation) {
        mAppDb.getConversationsDao().insertOrUpdate(conversation)
    }

    fun insertOrUpdateConversation(conversation: Conversation) {
        mAppDb.getConversationsDao().insertOrUpdate(conversation)
    }

    fun deleteConversation(threadId: Long) {
        mAppDb.getConversationsDao().deleteThreadId(threadId)
    }

    fun markAsReadConversation(threadId: Long) {
        mAppDb.getConversationsDao().markAsReadConversation(threadId)
    }

    fun markAsUnReadConversation(threadId: Long) {
        mAppDb.getConversationsDao().markAsUnReadConversation(threadId)
    }

    fun moveConversationToArchive(threadId: Long) {
        mAppDb.getConversationsDao().moveToArchive(threadId)
    }

    fun moveConversationToUnArchive(threadId: Long) {
        mAppDb.getConversationsDao().moveToUnArchive(threadId)
    }

    fun pinConversation(threadId: Long) {
        mAppDb.getConversationsDao().pinConversation(threadId)
    }

    fun unPinConversation(threadId: Long) {
        mAppDb.getConversationsDao().unPinConversation(threadId)
    }

    fun addToBlockConversation(threadId: Long) {
        mAppDb.getConversationsDao().blockConversation(threadId)
    }

    fun unblockConversation(threadId: Long) {
        mAppDb.getConversationsDao().unBlockConversation(threadId)
    }

    fun getThreadIdList(): List<Long> {
        return mAppDb.getConversationsDao().getThreadIdList()
    }


    //----------------------------------------------Messages----------------------------------------

    fun insertMessages(messages: List<Message>) {
        mAppDb.getMessagesDao().insertMessages(*messages.toTypedArray())
    }

    fun insertBulkMessages(messages: List<Message>) {
        mAppDb.getMessagesDao().insertBulkMessages(messages)
    }

    fun insertOrUpdateMessages(messages: Message) {
        mAppDb.getMessagesDao().insertOrUpdate(messages)
    }

    fun insertOrUpdateScheduleMessage(messages: ScheduleMessage) {
        mAppDb.getScheduleMessageDao().insertOrUpdate(messages)
    }

    fun getThreadMessages(threadId: Long): LiveData<List<Message>> {
        return mAppDb.getMessagesDao().getThreadMessages(threadId)
    }

    fun getMessagesByThreadId(threadId: Long): List<Message> {
        return mAppDb.getMessagesDao().getMessagesByThreadId(threadId)
    }

    fun getAllMessages(): List<Message> {
        return mAppDb.getMessagesDao().getAllMessages()
    }

    fun getMessageByQuery(query: String): List<Message> {
        return mAppDb.getMessagesDao().getMessageByQuery(query)
    }

    fun markAsReadMessage(id: Long) {
        mAppDb.getMessagesDao().markAsReadMessage(id)
    }

    fun markAsUnReadMessage(id: Long) {
        mAppDb.getMessagesDao().markAsUnReadMessage(id)
    }

    fun markAllMessageThreadRead(threadId: Long) {
        mAppDb.getMessagesDao().markAllMessageThreadRead(threadId)
    }

    fun starredMessageById(msgId: Long) {
        mAppDb.getStarredMessageDao().starredMessagesById(msgId)
    }

    fun unStarredMessageById(msgId: Long) {
        mAppDb.getStarredMessageDao().unStarredMessagesById(msgId)
    }

    fun insertOrUpdateSynDb(syncDb: SyncDb) {
        mAppDb.getSyncDbDao().insertOrUpdate(syncDb)
    }

    fun getSyncData(): List<SyncDb> {
        return mAppDb.getSyncDbDao().getSyncDate()
    }

    fun getMsgById(id: Long): Message {
        return mAppDb.getMessagesDao().getMsgById(id)
    }

    fun getMsgByThreadId(id: Long): Message {
        return mAppDb.getMessagesDao().getMsgByThreadId(id)
    }

    fun deleteMessagesById(id: Long) {
        mAppDb.getMessagesDao().deleteMessagesById(id)
    }

    fun deleteScheduleMessagesById(id: Long) {
        mAppDb.getScheduleMessageDao().deleteMessagesById(id)
    }

    fun deleteMessageByThreadId(threadId: Long) {
        mAppDb.getMessagesDao().deleteAllThreadMessages(threadId)
    }

    fun getStoredMessageCount(threadId: Long): Int {
        return mAppDb.getMessagesDao().getStoredMessageCount(threadId)
    }

    fun getTotalMessageCount(): Int {
        return mAppDb.getMessagesDao().getTotalMessageCount()
    }

    fun getMessageIdList(threadId: Long): List<Long> {
        return mAppDb.getMessagesDao().getMessageIdList(threadId)
    }

    fun getLastMessageByThreadId(threadId: Long): Message {
        return mAppDb.getMessagesDao().getLastMessageByThreadId(threadId)
    }

    //----------------------------------------------Contacts----------------------------------------
    fun getContactsFromCursor(): List<SimpleContact> {
        return mContext.getContactsFromCursorN(null)
    }

    fun insertOrUpdateContact(contactList: SimpleContact) {
        mAppDb.getContactsDao().insertContacts(contactList)
    }

    fun deleteContact(contactList: SimpleContact) {
        mAppDb.getContactsDao().delete(contactList)
    }

    fun getContactsFromDb(): LiveData<List<SimpleContact>> {
        return mAppDb.getContactsDao().getAllContacts()
    }

    fun getContactsCountFromDb(): Int {
        return mAppDb.getContactsDao().getContactsCountFromDb()
    }

    fun getContactsFromDbLocal(): List<SimpleContact> {
        return mAppDb.getContactsDao().getDbContacts()
    }

    fun getContactsByThreadId(threadId: Long): List<SimpleContact> {
        return mAppDb.getContactsDao().getContactByThreadId(threadId)
    }

}