package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagesImportExport
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.countRows
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.countRowsThreads
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getConversationsInsertDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessagesForConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessagesInsertDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadIdMessageCountMapFromLocalDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadIdMessageIdListMapFromCursor
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.updateMessagesByTimeInterval
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BackupDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.anyOf
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services.DataFetchService
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.DB_NAME
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ConversationViewModel @Inject constructor(
    val mRepository: DataRepository
) : ViewModel() {

//    val dataContainer = DataContainer()

    fun updateCategoryFilter(category: MessageType) {/*when (category) {
            MessageType.ALL -> {
                dataContainer.conversationsLiveData.postValue(conversations)
            }

            else -> {
                val filteredConversations = conversations.filter { it.categoryType == category }
                dataContainer.conversationsLiveData.postValue(filteredConversations)
            }
        }*/
    }

    fun getAllConversationsLiveData(): LiveData<List<Conversation>> {
        mRepository.getAllConversationsLiveData().observeForever { conversationsFromDb ->
            CoroutineScope(Dispatchers.IO).launch {
//                val deferredJobs = conversationsFromDb.map { conversation ->
//                    async {
//                        val message = if (conversation.msgId != -1L) {
//                            mRepository.getMsgById(conversation.msgId)
//                        } else {
//                            null
//                        }
//                        val updatedConversation = Conversation(
//                            conversation.threadId,
//                            conversation.snippet,
//                            conversation.date,
//                            conversation.read,
//                            conversation.title,
//                            conversation.photoUri,
//                            conversation.isGroupConversation,
//                            conversation.phoneNumber,
//                            conversation.categoryType,
//                            conversation.msgId,
//                            message,
//                            conversation.isArchive,
//                            conversation.isPin,
//                            conversation.isBlocked,
//                            conversation.isScheduled
//                        )
//                        updatedConversation
//                    }
//                }
//                val updatedConversations = deferredJobs.awaitAll()
//                val filteredConversations = updatedConversations.filter { it.message != null }
//                withContext(Dispatchers.Main) {
//                    mRepository.dataContainer.conversationsLiveData.postValue(filteredConversations)
//                }

                withContext(Dispatchers.Main) {
                    mRepository.dataContainer.conversationsLiveData.postValue(conversationsFromDb)
                }
            }
        }
        return mRepository.dataContainer.conversationsLiveData
    }

    fun getAllScheduleConversationsLiveData(): LiveData<List<Conversation>> {
        mRepository.getAllScheduleConversationsLiveData().observeForever { scheduleMsgList ->
            CoroutineScope(Dispatchers.IO).launch {
                if (scheduleMsgList != null && scheduleMsgList.isNotEmpty()) {
                    val deferredConversations = scheduleMsgList.map { scheduleMessage ->
                        async {
                            val conversation =
                                mRepository.getConversationByThreadId(scheduleMessage.threadId)
                            conversation.isScheduled = true
                            conversation.msgId = scheduleMessage.id
                            conversation.date = scheduleMessage.date
                            conversation.snippet = scheduleMessage.getMessage().body
                            conversation.title = scheduleMessage.getMessage().senderName
                            conversation.photoUri = scheduleMessage.getMessage().senderPhotoUri
                            conversation.read = scheduleMessage.getMessage().read
                            conversation
                        }
                    }

                    val scheduledConversations = deferredConversations.awaitAll()
                    val sortedConversations = scheduledConversations.sortedBy { it.date }
                    withContext(Dispatchers.Main) {
                        mRepository.dataContainer.scheduledConversationsLiveData.postValue(
                            sortedConversations
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        mRepository.dataContainer.scheduledConversationsLiveData.postValue(
                            arrayListOf()
                        )
                    }
                }
            }
        }
        return mRepository.dataContainer.scheduledConversationsLiveData
    }

    fun getAllStarredConversationsLiveData(): LiveData<List<Conversation>> {
        mRepository.getAllStarredConversationsLiveData().observeForever { starredMsgList ->
            CoroutineScope(Dispatchers.IO).launch {
                val deferredConversations = starredMsgList.map { starredMessage ->
                    async {
                        val conversation =
                            mRepository.getConversationByThreadId(starredMessage.threadId)
                        conversation.msgId = starredMessage.id
                        conversation.date = starredMessage.date
                        conversation.message = starredMessage
                        conversation.snippet = starredMessage.body
                        conversation.title = starredMessage.senderName
                        conversation.photoUri = starredMessage.senderPhotoUri
                        conversation.read = starredMessage.read
                        conversation
                    }
                }

                val scheduledConversations = deferredConversations.awaitAll()
                val sortedConversations = scheduledConversations.sortedByDescending { it.date }
                withContext(Dispatchers.Main) {
                    mRepository.dataContainer.starredConversationsLiveData.postValue(
                        sortedConversations
                    )
                }
            }
        }
        return mRepository.dataContainer.starredConversationsLiveData
    }

    fun getAllBlockedConversationsLiveData(): LiveData<List<Conversation>> {
        mRepository.getAllBlockedConversationsLiveData().observeForever { conversationsFromDb ->
            CoroutineScope(Dispatchers.IO).launch {
                /*val deferredJobs = conversationsFromDb.map { conversation ->
                    async {
                        val message = if (conversation.msgId != -1L) {
                            mRepository.getMsgById(conversation.msgId)
                        } else {
                            null
                        }
                        val updatedConversation = Conversation(
                            conversation.threadId,
                            conversation.snippet,
                            conversation.date,
                            conversation.read,
                            conversation.title,
                            conversation.photoUri,
                            conversation.isGroupConversation,
                            conversation.phoneNumber,
                            conversation.categoryType,
                            conversation.msgId,
                            message,
                            conversation.isArchive,
                            conversation.isPin,
                            conversation.isBlocked,
                            conversation.isScheduled
                        )
                        updatedConversation
                    }
                }
                val updatedConversations = deferredJobs.awaitAll()
                val filteredConversations = updatedConversations.filter { it.message != null }
                withContext(Dispatchers.Main) {
                    dataContainer.blockedConversationsLiveData.postValue(filteredConversations)
                }*/

                withContext(Dispatchers.Main) {
                    mRepository.dataContainer.blockedConversationsLiveData.postValue(
                        conversationsFromDb
                    )
                }
            }
        }
        return mRepository.dataContainer.blockedConversationsLiveData
    }

    fun getAllArchivedConversationsLiveData(): LiveData<List<Conversation>> {
        mRepository.getAllArchivedConversationsLiveData().observeForever { conversationsFromDb ->
            /* CoroutineScope(Dispatchers.IO).launch {
                 val deferredJobs = conversationsFromDb.map { conversation ->
                     async {
                         val message = mRepository.getMsgById(conversation.msgId)
                         val updatedConversation = Conversation(
                             conversation.threadId,
                             conversation.snippet,
                             conversation.date,
                             conversation.read,
                             conversation.title,
                             conversation.photoUri,
                             conversation.isGroupConversation,
                             conversation.phoneNumber,
                             conversation.categoryType,
                             conversation.msgId,
                             message,
                             conversation.isArchive,
                             conversation.isPin,
                             conversation.isBlocked,
                             conversation.isScheduled
                         )
                         updatedConversation
                     }
                 }
                 val updatedConversations = deferredJobs.awaitAll()
                 withContext(Dispatchers.Main) {
                     dataContainer.archivedConversationsLiveData.postValue(updatedConversations)
                 }
             }*/
            mRepository.dataContainer.archivedConversationsLiveData.postValue(conversationsFromDb)
        }
        return mRepository.dataContainer.archivedConversationsLiveData
    }

    fun getMsgById(id: Long): Message {
        return mRepository.getMsgById(id)
    }

    //---------------------------------------------Data Source--------------------------------------

    private fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val conversations = try {
                mRepository.getConversationFromDb() as ArrayList<Conversation>
            } catch (e: Exception) {
                ArrayList()
            }
            getNewConversationsNNNNNN(conversations)
        }
    }

    fun getLatestMessageTimestamp(): Long {
        return mRepository.mAppDb.getMessagesDao().getLatestMessageTimestamp()
    }

    private fun updateDatabaseNNNN() {
        val cachedConversations = mRepository.getConversationFromDb() as ArrayList
        val total = mRepository.mContext.countRows(Telephony.Sms.CONTENT_URI)
        val totalDbCount = mRepository.getTotalMessageCount()

        mRepository.dataContainer.updateProgress(0)

        var counter = 0
        val totalProgressCount =
            mRepository.mContext.countRowsThreads(Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true"))
        if (totalProgressCount == 0) {
            return
        }
        var progressFraction = 0f
        val conversations = mRepository.mContext.getConversationsInsertDb(null, mRepository) {
            counter++
            progressFraction = (counter.toFloat() / totalProgressCount) * 100
            mRepository.dataContainer.updateProgress(progressFraction.toInt())
            if (counter >= totalProgressCount) {
                mRepository.insertOrUpdateSynDb(SyncDb(date = (System.currentTimeMillis() / 1000).toInt()))
                MyPreferences(mRepository.mContext).lastUpdateDbTime =
                    System.currentTimeMillis() / 1000
            }
        }
        MyPreferences.getPreferences(mRepository.mContext)?.setDefaultApp = false

        conversations.forEach { clonedConversation ->
            val threadIds = cachedConversations.map { it.threadId }
            if (threadIds.contains(clonedConversation.threadId)) {
                clonedConversation.isArchive =
                    cachedConversations.first { it.threadId == clonedConversation.threadId }.isArchive
                mRepository.insertOrUpdateConversation(clonedConversation)
                cachedConversations.add(clonedConversation)
            }
        }
        cachedConversations.forEach { cachedConversation ->
            val threadId = cachedConversation.threadId
            val isConversationDeleted = !conversations.map { it.threadId }.contains(threadId)
            if (isConversationDeleted) {
                mRepository.deleteConversation(threadId)
                mRepository.deleteMessageByThreadId(threadId)
            }
        }

        mRepository.mContext.updateMessagesByTimeInterval(
            MyPreferences(mRepository.mContext).lastUpdateDbTime,
            mRepository = mRepository
        )

        if (totalDbCount != total) {
            var totalCount = 0
            val cursorDbMap = mRepository.mContext.getThreadIdMessageIdListMapFromCursor()
            val localDbMap = getThreadIdMessageCountMapFromLocalDb(mRepository)
            val allKeys = cursorDbMap.keys + localDbMap.keys

            val uniqueKeys = allKeys.distinct()
            val missingInMap1 = uniqueKeys.filter { key ->
                cursorDbMap[key]?.size != localDbMap[key]?.size
            }

            totalCount = totalProgressCount + missingInMap1.size
            progressFraction = (counter.toFloat() / totalCount) * 100
            mRepository.dataContainer.updateProgress(progressFraction.toInt())

            missingInMap1.forEach {
                if (cursorDbMap[it] != null && localDbMap[it] != null) {
                    val localList = localDbMap[it]!!
                    val cursorList = cursorDbMap[it]!!

                    val deletedMessages = localList.minus(cursorList.toSet())

                    if (deletedMessages.isNotEmpty()) {
                        deletedMessages.forEach { msgId ->
                            mRepository.deleteMessagesById(msgId)
                        }

                        val lastMsg = mRepository.getLastMessageByThreadId(it)
                        val conversation = mRepository.getConversationByThreadId(it)
                        if (conversation != null) {
                            conversation.msgId = lastMsg.id
                            conversation.snippet = lastMsg.body
                            conversation.date = lastMsg.date
                            conversation.read = lastMsg.read
                            mRepository.insertOrUpdateConversation(conversation)
                        }

                    }
                }

                counter++
                progressFraction = (counter.toFloat() / totalCount) * 100
                mRepository.dataContainer.updateProgress(progressFraction.toInt())
            }
            MyPreferences(mRepository.mContext).lastUpdateDbTime =
                System.currentTimeMillis() / 1000
        }
    }


    private fun getNewConversationsNNNNNN(cachedConversations: ArrayList<Conversation>) {
//        var counterFailed = 0
        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
            val totalCount = mRepository.mContext.countRowsThreads(uri)
            if (totalCount == 0) {
                return@launch
            }
            var progressFraction = 0f
            var counter = 0
            val listThreadId = arrayListOf<Long>()
            val tempThreads = arrayListOf<Long>()

            mRepository.mContext.getConversationsInsertDb(null, mRepository) {
                counter++
                progressFraction = (counter.toFloat() / totalCount) * 100
                mRepository.dataContainer.updateProgress(progressFraction.toInt())

                if (counter >= totalCount) {
                    mRepository.insertOrUpdateSynDb(SyncDb(date = (System.currentTimeMillis() / 1000).toInt()))
                    MyPreferences(mRepository.mContext).lastUpdateDbTime =
                        System.currentTimeMillis() / 1000
                }

                if (it > 0) {
                    listThreadId.add(it)
                    tempThreads.add(it)
                }
                /*else{
                        counterFailed++
                        MyPreferences(mRepository.mContext).failedConversation = counterFailed
                    }*/
            }

            listThreadId.forEach {
                val messages = mRepository.mContext.getMessagesForConversation(
                    it, limit = 5, offset = 0, mRepository = mRepository
                )
                mRepository.insertBulkMessages(messages)
            }

            // Remove deleted conversations
            cachedConversations.forEach { cachedConversation ->
                val threadId = cachedConversation.threadId
                val isConversationDeleted = !listThreadId.contains(threadId)
                if (isConversationDeleted) {
                    tempThreads.remove(threadId)
                    mRepository.deleteConversation(threadId)
                    mRepository.deleteMessageByThreadId(threadId)
                }
            }

            MyPreferences(mRepository.mContext).lastUpdateDbTime = System.currentTimeMillis() / 1000
            getAllMessageAndInsertForLargeDataSet(tempThreads)

        }
    }

    private fun getAllMessageAndInsertForLargeDataSet(listThreadId: List<Long>) {
        CoroutineScope(Dispatchers.IO).launch {
            listThreadId.forEach {
                mRepository.mContext.getMessagesInsertDb(
                    it, limit = Int.MAX_VALUE, mRepository = mRepository
                )
            }
            MyPreferences(mRepository.mContext).isAllMessageFetched = true
        }
    }


    fun updateConversation(conversation: Conversation) {
        mRepository.updateConversation(conversation)
    }

    fun getConversationByThreadId(threadId: Long): Conversation {
        return mRepository.getConversationByThreadId(threadId)
    }

    fun getMessagesByThreadId(threadId: Long): List<Message> {
        return mRepository.getMessagesByThreadId(threadId)
    }

    fun deleteConversationByThreadId(threadId: Long) {
        getRealmThread { realm ->
            val conversation =
                realm.where(ConversationNew::class.java).anyOf("id", listOf(threadId).toLongArray())
                    .findAll()
            val messages = realm.where(MessageNew::class.java)
                .anyOf("threadId", listOf(threadId).toLongArray()).findAll()
            realm.executeTransaction {
                conversation.deleteAllFromRealm()
                messages.deleteAllFromRealm()
            }
        }
//        mRepository.deleteConversation(threadId)
    }

    fun moveToArchive(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = true }
            }
        }
//        mRepository.moveConversationToArchive(threadId)
    }

    fun moveToUnArchive(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = false }
            }
        }
//        mRepository.moveConversationToUnArchive(threadId)
    }

    fun pinConversation(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = true }
            }
        }
//        mRepository.pinConversation(threadId)
    }

    fun unPinConversation(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = false }
            }
        }
//        mRepository.unPinConversation(threadId)
    }

    fun markAsReadConversation(threadId: Long) {
        mRepository.markAsReadConversation(threadId)
    }

    fun markAsUnReadConversation(threadId: Long) {
        mRepository.markAsUnReadConversation(threadId)
    }

    fun addToBlockConversation(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .equalTo("blocked", false)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.blocked = true
                }
            }
        }
//        mRepository.addToBlockConversation(threadId)
    }

    fun unblockConversation(threadId: Long) {
        getRealmThread { realm ->
            val conversations = realm.where(ConversationNew::class.java)
                .anyOf("id", listOf(threadId).toLongArray())
                .equalTo("blocked", true)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.blocked = false
                }
            }
        }
//        mRepository.unblockConversation(threadId)
    }

    fun updateMessages(message: Message) {
        mRepository.insertOrUpdateMessages(message)
    }

    fun markAllMessageThreadRead(threadId: Long) {
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("threadId", listOf(threadId).toLongArray())
                .beginGroup()
                .equalTo("read", false)
                .or()
                .equalTo("seen", false)
                .endGroup()
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
        }
//        mRepository.markAllMessageThreadRead(threadId)
    }

    fun markAsUnReadMessage(id: Long) {
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("threadId", listOf(id).toLongArray())
                .beginGroup()
                .equalTo("read", true)
                .or()
                .equalTo("seen", true)
                .endGroup()
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = false
                    message.read = false
                }
            }
        }
//        mRepository.markAsUnReadMessage(id)
    }

    fun deleteMessageByThreadId(threadId: Long) {
        mRepository.deleteMessageByThreadId(threadId)
    }

    fun deleteMessagesById(threadId: Long) {
        mRepository.deleteMessagesById(threadId)
    }

    fun deleteScheduleMessagesById(threadId: Long) {
        mRepository.deleteScheduleMessagesById(threadId)
    }

    fun unStarredMessageById(msgId: Long) {
        mRepository.unStarredMessageById(msgId)
    }

    fun getContactsFromDbLocal(): List<SimpleContact> {
        val tempList = mRepository.getContactsFromDbLocal()
        return if (tempList != null && tempList.isNotEmpty()) {
            tempList
        } else {
            arrayListOf()
        }
    }

    fun insertOrUpdateContact(simpleContact: SimpleContact) {
        mRepository.insertOrUpdateContact(simpleContact)
    }

    fun deleteContact(simpleContact: SimpleContact) {
        mRepository.deleteContact(simpleContact)
    }

    fun checkIfLastSync(
        syncDb: Boolean,
        isFromRestore: Boolean = false,
        callback: (isProgressShow: Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (syncDb) {
                callback(true)
//                getConversations()
                MyPreferences.getPreferences(mRepository.mContext)?.setDefaultApp = false
                val serviceIntent = Intent(mRepository.mContext, DataFetchService::class.java)
                serviceIntent.putExtra("isFromRestore", isFromRestore)

                mRepository.mContext.startService(serviceIntent)
            } else {
                /*val listSyncDates = mRepository.getSyncData()
                if (listSyncDates.isNotEmpty()) {
                    callback(false)
                    dataContainer.updateProgress(100)
                } else {
                    callback(true)
                    getConversations()
                }*/
                val listSyncDates = mRepository.getSyncData()
                if (listSyncDates.isNotEmpty()) {
                    callback(false)
                    if (MyPreferences.getPreferences(mRepository.mContext)?.isAllMessageFetched!!) {
                        if (MyPreferences.getPreferences(mRepository.mContext)?.setDefaultApp!!) {
                            updateDatabaseNNNN()
                        }
                    } /*else {
                        val tempThreads = mRepository.mContext.getThreadIdListFromCursor()
                        getAllMessageAndInsertForLargeDataSet(tempThreads)
                    }*/
                } else {
                    callback(true)
                    MyPreferences.getPreferences(mRepository.mContext)?.setDefaultApp = false
//                    getConversations()
                    val serviceIntent = Intent(mRepository.mContext, DataFetchService::class.java)
                    mRepository.mContext.startService(serviceIntent)
                }
            }
        }
    }

    fun getSyncData(): List<SyncDb> {
        return mRepository.getSyncData()
    }

    fun exportMessages(uri: Uri, callBack: (Int, Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MessagesImportExport(
                    mRepository.mContext, mRepository
                ).getMessagesToExport(callBack) { messagesToExport ->
                    val jsonString = Gson().toJson(messagesToExport)
                    val outputStream = mRepository.mContext.contentResolver.openOutputStream(uri)!!

                    outputStream.use {
                        it.write(jsonString.toByteArray())
                    }

                    callBack.invoke(messagesToExport.messageCount, messagesToExport.messageCount)
                }
            } catch (e: Exception) {
                callBack.invoke(100, 100)
                e.printStackTrace()
            }
        }
    }

    fun importMessages(uri: Uri, callBack: (Int, Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jsonString =
                    mRepository.mContext.contentResolver.openInputStream(uri)!!.use { inputStream ->
                        inputStream.bufferedReader().readText()
                    }

                val deserializedList = Gson().fromJson(jsonString, BackupDb::class.java)
                val backUpList = deserializedList.messages
                MessagesImportExport(
                    mRepository.mContext, mRepository
                ).restoreMessages(backUpList) {
                    callBack.invoke(it, backUpList.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}