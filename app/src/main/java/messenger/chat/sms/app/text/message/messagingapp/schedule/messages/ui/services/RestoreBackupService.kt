package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import com.klinker.android.send_message.Utils
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.queryCursor
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgress
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgressStatus
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Backup
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.BackupMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToContactImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToConversationImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToMessageImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToRecipientImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Recipient
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.SyncLog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.forEach
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.tryOrNull
import okio.buffer
import okio.source
import org.greenrobot.eventbus.EventBus

class RestoreBackupService : Service() {

    companion object {
        private const val NOTIFICATION_ID = -1
        private var context: Context? = null
        var isProgressEnable = false

        private const val ACTION_START =
            "messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ACTION_START"
        private const val ACTION_BACKUP_START =
            "messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ACTION_BACKUP_START"
        private const val ACTION_STOP =
            "messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ACTION_STOP"
        const val BACKUP_RESTORE_CHANNEL_ID = "notifications_backup_restore"
        fun start(context: Context, uri: String, type: Int) {
            isProgressEnable = true
            this.context = context
            if (type == 1) {
                val intent =
                    Intent(context, RestoreBackupService::class.java).setAction(ACTION_BACKUP_START)
                        .putExtra("URI", uri)
                ContextCompat.startForegroundService(context, intent)
            } else {
                val intent =
                    Intent(context, RestoreBackupService::class.java).setAction(ACTION_START)
                        .putExtra("URI", uri)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    private val notification by lazy { getNotificationForBackup() }

    private fun getNotificationForBackup(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = getString(R.string.backup_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(BACKUP_RESTORE_CHANNEL_ID, name, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context!!, BACKUP_RESTORE_CHANNEL_ID)
            .setContentTitle(getString(R.string.restore_message))
            .setShowWhen(false)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_messenger)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setProgress(0, 0, true)
            .setOngoing(true)
    }

    override fun onCreate() {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent.action) {
            ACTION_START -> start(intent)
            ACTION_BACKUP_START -> startBackup(intent)
            ACTION_STOP -> stop()
        }

        return START_STICKY
    }

    @SuppressLint("CheckResult")
    private fun start(intent: Intent) {
        startForeground(NOTIFICATION_ID, notification.build())
        val uriData = intent.getStringExtra("URI")
        val uri = Uri.parse(uriData)
        performRestore(uri)
    }


    @SuppressLint("CheckResult")
    private fun startBackup(intent: Intent) {
        startForeground(NOTIFICATION_ID, notification.build())
        val uriData = intent.getStringExtra("URI")
        val uri = Uri.parse(uriData)
        performBackup(uri)
    }

    private fun stop() {
        isProgressEnable = false
        stopForeground(true)
        stopSelf()
    }

    private fun smsExist(smsBackup: BackupMessage): Boolean {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms._ID)
        val selection =
            "${Telephony.Sms.DATE} = ? AND ${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.TYPE} = ?"
        val selectionArgs =
            arrayOf(smsBackup.date.toString(), smsBackup.address, smsBackup.type.toString())
        var exists = false
        queryCursor(uri, projection, selection, selectionArgs) {
            exists = it.count > 0
        }
        return exists
    }

    private fun performRestore(fileUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                EventBus.getDefault().post(UpdateProgressStatus(true, getString(R.string.restore_message)))

                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()

                val backup = contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.source().buffer().use { bufferedSource ->
                        moshi.adapter(Backup::class.java).fromJson(bufferedSource)
                    }
                }


                val messageCount = backup?.messages?.size ?: 0
                var errorCount = 0

                Log.e("TAG", "performRestore: ${messageCount}")
                backup?.messages?.forEachIndexed { index, message ->
                    // Update the progress
                    try {
                        val values = contentValuesOf(
                            Telephony.Sms.TYPE to message.type,
                            Telephony.Sms.ADDRESS to message.address,
                            Telephony.Sms.DATE to message.date,
                            Telephony.Sms.DATE_SENT to message.dateSent,
                            Telephony.Sms.READ to message.read,
                            Telephony.Sms.SEEN to 1,
                            Telephony.Sms.STATUS to message.status,
                            Telephony.Sms.BODY to message.body,
                            Telephony.Sms.PROTOCOL to message.protocol,
                            Telephony.Sms.SERVICE_CENTER to message.serviceCenter,
                            Telephony.Sms.LOCKED to message.locked
                        )

                        if (!smsExist(message)) {
                            val threadId = Utils.getOrCreateThreadId(context!!, message.address)
                            values.put(Telephony.Sms.THREAD_ID, threadId)
                            Log.e("TAG", "smsNotExist: " + message.body)
                            contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
                        } else {
                            Log.e("TAG", "smsExist: ")
                        }
                        // Insert the message into the content provider
                    } catch (e: Exception) {
                        // Handle the exception or log a message if needed
                        Log.e("TAG", "performRestore: ${e.message}" )
                        errorCount++
                    }

                    EventBus.getDefault()
                        .post(UpdateProgress(index, messageCount, getString(R.string.restore)))
                }

                if (errorCount > 0) {
                    Log.e("TAG", "Failed to restore $errorCount/$messageCount messages")
                }

                EventBus.getDefault().post(UpdateProgressStatus(false, ""))

                // Sync the messages
                syncMessages()
            } catch (e: Exception) {
                e.printStackTrace()
                EventBus.getDefault().post(UpdateProgressStatus(false, ""))
                stop()
            } finally {
                EventBus.getDefault().post(UpdateProgressStatus(false, ""))
                stop()
            }
        }
    }

    private fun syncMessages() {
        EventBus.getDefault().post(UpdateProgressStatus(true, getString(R.string.synchronizing_messages)))

        CoroutineScope(Dispatchers.IO).launch {
            Log.e("TAG", "syncMessages:Start ${System.currentTimeMillis()}")
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            val persistedData = realm.copyFromRealm(
                realm.where(ConversationNew::class.java).beginGroup().equalTo("archived", true).or()
                    .equalTo("blocked", true).or().equalTo("pinned", true).or().isNotEmpty("name")
                    .or().isNotNull("blockingClient").or().isNotEmpty("blockReason").endGroup()
                    .findAll()
            ).associateBy { conversation -> conversation.id }.toMutableMap()

            realm.delete(Contact::class.java)
            realm.delete(ConversationNew::class.java)
            realm.delete(MessageNew::class.java)
            realm.delete(Recipient::class.java)
            realm.deleteAll()

            val cursorToMessage = CursorToMessageImpl(context!!, KeyManagerImpl.newInstance())
            val cursorToConversation = CursorToConversationImpl(context!!)
            val cursorToRecipient = CursorToRecipientImpl(context!!)

            val messageCursor = cursorToMessage.getMessagesCursor()
            Log.e("TAG", "messageCursor-count: ${messageCursor?.count}")
            val conversationCursor = cursorToConversation.getConversationsCursor()
            val recipientCursor = cursorToRecipient.getRecipientCursor()

            val max = (messageCursor?.count ?: 0) + (conversationCursor?.count
                ?: 0) + (recipientCursor?.count ?: 0)

            var progress = 0


            // Sync messages
            messageCursor?.use {
                val messageColumns = CursorToMessage.MessageColumns(messageCursor)
                messageCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        EventBus.getDefault()
                            .post(UpdateProgress(progress, max, getString(R.string.message)))
                        val message = cursorToMessage.map(Pair(cursor, messageColumns))
                        realm.insertOrUpdate(message)
                    }
                }
            }


            // Sync conversations
            conversationCursor?.use {
                conversationCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        EventBus.getDefault()
                            .post(UpdateProgress(progress, max, getString(R.string.message)))
                        val conversation = cursorToConversation.map(cursor).apply {
                            persistedData[id]?.let { persistedConversation ->
                                archived = persistedConversation.archived
                                blocked = persistedConversation.blocked
                                pinned = persistedConversation.pinned
                                name = persistedConversation.name
                                blockingClient = persistedConversation.blockingClient
                                blockReason = persistedConversation.blockReason
                            }
                            lastMessage =
                                realm.where(MessageNew::class.java).sort("date", Sort.DESCENDING)
                                    .equalTo("threadId", id).findFirst()
                        }
                        realm.insertOrUpdate(conversation)
                    }
                }
            }

            val phoneNumberUtils = PhoneNumberUtils(context!!)
            // Sync recipients
            recipientCursor?.use {
                val contacts = realm.copyToRealmOrUpdate(getContacts())
                recipientCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        EventBus.getDefault()
                            .post(UpdateProgress(progress, max, getString(R.string.message)))
                        val recipient = cursorToRecipient.map(cursor).apply {
                            contact = contacts.firstOrNull { contact ->
                                contact.numbers.any {
                                    phoneNumberUtils.compare(
                                        address, it.address
                                    )
                                }
                            }
                        }
                        realm.insertOrUpdate(recipient)
                    }
                }
            }

            realm.insert(SyncLog())
            realm.commitTransaction()
            realm.close()

            EventBus.getDefault().post(UpdateProgressStatus(false, getString(R.string.synchronizing_messages)))

            Log.e("TAG", "syncMessages:End ${System.currentTimeMillis()}")
        }
    }

    private fun getContacts(): List<Contact> {
        val defaultNumberIds = Realm.getDefaultInstance().use { realm ->
            realm.where(PhoneNumber::class.java).equalTo("isDefault", true).findAll()
                .map { number -> number.id }
        }

        val phoneNumberUtils = PhoneNumberUtils(context!!)
        val cursorToContact = CursorToContactImpl(context!!)
        return cursorToContact.getContactsCursor()?.map { cursor -> cursorToContact.map(cursor) }
            ?.groupBy { contact -> contact.lookupKey }?.map { contacts ->
                // Sometimes, contacts providers on the phone will create duplicate phone number entries. This
                // commonly happens with Whatsapp. Let's try to detect these duplicate entries and filter them out
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contacts.value.flatMap { it.numbers }.forEach { number ->
                    number.isDefault = defaultNumberIds.any { id -> id == number.id }
                    val duplicate = uniqueNumbers.find { other ->
                        phoneNumberUtils.compare(number.address, other.address)
                    }

                    if (duplicate == null) {
                        uniqueNumbers += number
                    } else if (!duplicate.isDefault && number.isDefault) {
                        duplicate.isDefault = true
                    }
                }

                contacts.value.first().apply {
                    numbers.clear()
                    numbers.addAll(uniqueNumbers)
                }
            } ?: listOf()
    }

    private fun messageToBackupMessage(message: MessageNew): BackupMessage = BackupMessage(
        type = message.boxId,
        address = message.address,
        date = message.date,
        dateSent = message.dateSent,
        read = message.read,
        status = message.deliveryStatus,
        body = message.body,
        protocol = 0,
        serviceCenter = null,
        locked = message.locked,
        subId = message.subId
    )

    private fun performBackup(outputUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val moshi = Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()

                var messageCount = 0
                var backupMessages: List<BackupMessage> = emptyList()

                synchronized(this) {
                    // Map all the messages into our object we'll use for the Json mapping
                    Realm.getDefaultInstance().use { realm ->
                        // Get the messages from realm
                        val messages = realm.where(MessageNew::class.java).sort("date").findAll()
                            .createSnapshot()
                        messageCount = messages.size

                        // Map the messages to the new format
                        backupMessages = messages.mapIndexed { index, message ->
                            messageToBackupMessage(message).also {
                                EventBus.getDefault().post(
                                    UpdateProgress(
                                        index,
                                        messageCount,
                                        getString(R.string.message)
                                    )
                                )

                            }
                        }
                    }

                    // Convert the data to json
                    val adapter = moshi.adapter(Backup::class.java).indent("\t")
                    val backupData = Backup(messageCount, backupMessages)
                    val json = adapter.toJson(backupData)

                    // Open an output stream for the provided Uri
                    val outputStream = contentResolver.openOutputStream(outputUri)
                    outputStream.use { it?.write(json.toByteArray()) }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                EventBus.getDefault().post(UpdateProgressStatus(false, ""))
                stop()
            } finally {
                EventBus.getDefault().post(UpdateProgressStatus(false, ""))
                stop()
            }
        }
    }

}