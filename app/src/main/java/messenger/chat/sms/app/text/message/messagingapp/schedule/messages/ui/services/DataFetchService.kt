package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.countRowsThreads
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getConversationsInsertDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessagesForConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessagesInsertDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SyncDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.FetchDataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import javax.inject.Inject

@AndroidEntryPoint
class DataFetchService : Service() {

    @Inject
    lateinit var mRepository: FetchDataRepository

    @Inject
    lateinit var mRepositoryData: DataRepository

    private var serviceJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start fetching data in a coroutine
        val isFromRestore = intent?.getBooleanExtra("isFromRestore", false) ?: false
        Log.e("TAG", "onStartCommand: $isFromRestore")

        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            fetchDataInBackground(isFromRestore)
        }

        return START_STICKY
    }

    private suspend fun fetchDataInBackground(isFromRestore: Boolean) {
        withContext(Dispatchers.IO) {
            isDataFetched(isFromRestore)
        }
    }

    private fun isDataFetched(isFromRestore: Boolean): Boolean {
        getConversations(isFromRestore)
        return false
    }

    private fun getConversations(isFromRestore: Boolean) {
        val conversations = try {
            mRepository.getConversationFromDb() as ArrayList<Conversation>
        } catch (e: Exception) {
            ArrayList()
        }
        getNewConversations(conversations, isFromRestore)
    }

    private fun getNewConversations(
        cachedConversations: ArrayList<Conversation>, isFromRestore: Boolean
    ) {
        Log.e("TAG", "getNewConversations:Start ${System.currentTimeMillis()}")
        val totalCount =
            mRepository.mContext.countRowsThreads(Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true"))
        if (totalCount == 0) {
            return
        }
        var progressFraction = 0f
        var counter = 0
        mRepository.insertOrUpdateSynDb(SyncDb(date = (System.currentTimeMillis() / 1000).toInt()))

        val conversations = mRepository.mContext.getConversationsInsertDb(null, mRepositoryData) {
            counter++
            progressFraction = (counter.toFloat() / totalCount) * 100
            mRepositoryData.dataContainer.updateProgress(progressFraction.toInt())

            if (counter >= totalCount) {
                mRepository.insertOrUpdateSynDb(SyncDb(date = (System.currentTimeMillis() / 1000).toInt()))
                MyPreferences(mRepository.mContext).lastUpdateDbTime =
                    System.currentTimeMillis() / 1000
            }
        }
        conversations.forEach { clonedConversation ->
            val threadIds = cachedConversations.map { it.threadId }
            if (!threadIds.contains(clonedConversation.threadId)) {
//                 mRepository.insertOrUpdateConversation(clonedConversation)
                cachedConversations.add(clonedConversation)
            }
        }

        cachedConversations.forEach { cachedConversation ->
            val threadId = cachedConversation.threadId
            val isConversationDeleted = !conversations.map { it.threadId }.contains(threadId)
            if (isConversationDeleted) {
                mRepository.deleteConversation(threadId)
            }
        }

        if (!isFromRestore) {
            val messageInsertJobs = conversations.map { it.threadId }.map { threadId ->
//             val messages = mRepository.getMessagesFromCursor(threadId)
                val messages = getMessagesForConversation(
                    threadId, limit = 30, offset = 0, mRepository = mRepositoryData
                )
                mRepositoryData.insertBulkMessages(messages)
            }
        }

        MyPreferences(mRepository.mContext).isAllMessageFetched = true
        Log.e("TAG", "getNewConversations:End ${System.currentTimeMillis()}")

        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "DataFetchService Channel"
            val descriptionText = "DataFetchService Channel Description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder =
            NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_messenger)
                .setContentTitle("Syncing message").setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    companion object {
        const val NOTIFICATION_ID = 111111
        const val CHANNEL_ID = "DataFetchServiceChannel"
    }

    private fun getNewConversationsNNNNNN(cachedConversations: ArrayList<Conversation>) {
        val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
        val totalCount = mRepository.mContext.countRowsThreads(uri)
        if (totalCount == 0) {
            return
        }
        var progressFraction = 0f
        var counter = 0
        val listThreadId = arrayListOf<Long>()
        val tempThreads = arrayListOf<Long>()

        mRepository.mContext.getConversationsInsertDb(null, mRepositoryData) {
            counter++
            progressFraction = (counter.toFloat() / totalCount) * 100
//            dataContainer.updateProgress(progressFraction.toInt())

            if (counter >= totalCount) {
                mRepository.insertOrUpdateSynDb(SyncDb(date = (System.currentTimeMillis() / 1000).toInt()))
                MyPreferences(mRepository.mContext).lastUpdateDbTime =
                    System.currentTimeMillis() / 1000
            }

            if (it > 0) {
                listThreadId.add(it)
                tempThreads.add(it)
            }
        }

        listThreadId.forEach {
            val messages = mRepository.mContext.getMessagesForConversation(
                it, limit = 5, offset = 0, mRepository = mRepositoryData
            )
            mRepositoryData.insertBulkMessages(messages)

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

    private fun getAllMessageAndInsertForLargeDataSet(listThreadId: List<Long>) {
        CoroutineScope(Dispatchers.IO).launch {
            listThreadId.forEach {
                mRepository.mContext.getMessagesInsertDb(
                    it, limit = Int.MAX_VALUE, mRepository = mRepositoryData
                )
            }
            MyPreferences(mRepository.mContext).isAllMessageFetched = true

            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
        Log.d("DataFetchService", "Service destroyed")
    }

}
