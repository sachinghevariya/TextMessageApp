package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Telephony
import android.util.Log
import androidx.core.content.contentValuesOf
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.scheduleMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.sendMessageCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToConversationImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToRecipientImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ScheduledMessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TelephonyCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.tryOrNull
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.SCHEDULED_MESSAGE_ID
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledMessageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appDb: MessagesDatabase
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            CoroutineScope(Dispatchers.IO).launch {
                getRealmThread { realm ->
                    val messagesList =
                        realm.where(ScheduledMessageNew::class.java)
                            .findAll().map {
                                realm.copyFromRealm(it)
                            }
                    messagesList.forEach {
                        context.scheduleMessage(it)
                    }
                }
                /* appDb.getMessagesDao().getScheduledMessages().forEach {
                     if (it != null) {
                         context.scheduleMessage(it)
                     }
                 }*/
            }
        } else {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakelock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "messenger:scheduled.message.receiver"
            )
            wakelock.acquire(3000)

            CoroutineScope(Dispatchers.IO).launch {
                handleIntent(context, intent)
            }
        }
    }

    private fun handleIntent(context: Context, intent: Intent) {
//        val threadId = intent.getLongExtra(THREAD_ID, 0L)
        val messageId = intent.getLongExtra(SCHEDULED_MESSAGE_ID, 0L)

        /*val isBlocked = appDb.getConversationsDao().checkIfThreadIdBlocked(threadId)
        if(isBlocked){
            appDb.getScheduleMessageDao().deleteMessagesById(messageId)
        }else{

        }*/

        getRealmThread { realm ->
            val messagesList =
                realm.where(ScheduledMessageNew::class.java).equalTo("id", messageId).findAll()
                    .map {
                        realm.copyFromRealm(it)
                    }

            messagesList.forEach { message ->
                val addresses = message.recipients
                try {
                    Handler(Looper.getMainLooper()).post {
                        val msg = insertSentSms(
                            context,
                            message.subId,
                            message.threadId,
                            message.recipients[0]!!,
                            message.body,
                            message.date
                        )
                        val messagingUtils = MessagingUtils(context)
                        context.sendMessageCompat(
                            message.body,
                            addresses,
                            message.subId,
                            msg.contentId,
                            messagingUtils
                        )
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        getOrCreateConversation(context, message.recipients)?.let {
                            updateConversations(message.threadId)
                        }
                        getRealmThread { realm ->
                            val msg = realm.where(ScheduledMessageNew::class.java)
                                .equalTo("id", messageId).findFirst()
                            realm.executeTransaction { msg?.deleteFromRealm() }
                        }
                    }, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: Error) {
                    e.printStackTrace()
                }
            }
        }

//        val messagesList = try {
//            appDb.getScheduleMessageDao().getScheduledMessageWithId(threadId, messageId)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return
//        }

//        messagesList.forEach { message ->
//            val addresses = (message.participants as ArrayList<SimpleContact>).getAddresses()
//            try {
//                Handler(Looper.getMainLooper()).post {
//                    val messagingUtils = MessagingUtils(context)
//                    context.sendMessageCompat(
//                        message.body,
//                        addresses,
//                        message.subscriptionId,
//                        null,
//                        messagingUtils
//                    )
//                }
//                CoroutineScope(Dispatchers.IO).launch {
//                    delay(1000)
//                    appDb.getScheduleMessageDao().deleteMessagesById(messageId)
//                    val messageSent = context.getMessages(threadId, limit = 1)
//                    appDb.getMessagesDao().insertOrUpdate(messageSent[0])
//                    val conversation = appDb.getConversationsDao().getConversationByThreadId(threadId)
//                    conversation.isScheduled = false
//                    conversation.msgId = messageSent[0].id
//                    conversation.date = messageSent[0].date
//                    conversation.snippet = messageSent[0].body
//                    conversation.read = messageSent[0].read
//                    MyApp.isNewMessageArrived = true
//                    appDb.getConversationsDao().updateConversation(conversation)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } catch (e: Error) {
//                e.printStackTrace()
//            }
//        }

    }

    private fun insertSentSms(
        context: Context,
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): MessageNew {
//        val messageIds = KeyManagerImpl()
        // Insert the message to Realm
        val message = MessageNew().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.subId = subId

            id = KeyManagerImpl.newInstance().newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: MessageNew? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE to System.currentTimeMillis(),
            Telephony.Sms.READ to true,
            Telephony.Sms.SEEN to true,
            Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            Telephony.Sms.THREAD_ID to threadId
        )

//        if (prefs.canUseSubId.get()) {
//            values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
//        }

        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
//        Log.e("TAG", "insertSentSms: ${uri}")
        uri?.lastPathSegment?.toLong()?.let { id ->
            realm.executeTransaction {
                managedMessage?.takeIf { it.isValid }?.contentId = id
                message.contentId = id
            }
        }
        realm.close()

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
//        if (threadId == 0L) {
//            uri?.let(syncRepository::syncMessage)
//        }

        return message
    }

    private fun updateConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            threadIds.forEach { threadId ->
                val conversation = realm
                    .where(ConversationNew::class.java)
                    .equalTo("id", threadId)
                    .findFirst() ?: return

                val message = realm
                    .where(MessageNew::class.java)
                    .equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING)
                    .findFirst()

                realm.executeTransaction {
                    conversation.lastMessage = message
                }
            }
        }
    }

    private fun getOrCreateConversation(
        context: Context,
        addresses: List<String>
    ): ConversationNew? {
        if (addresses.isEmpty()) {
            return null
        }

        return (getThreadId(context, addresses) ?: tryOrNull {
            TelephonyCompat.getOrCreateThreadId(
                context,
                addresses.toSet()
            )
        })
            ?.takeIf { threadId -> threadId != 0L }
            ?.let { threadId ->
                getConversation(threadId)
                    ?.let(Realm.getDefaultInstance()::copyFromRealm)
                    ?: getConversationFromCp(context, threadId)
            }
    }

    private fun getConversationFromCp(context: Context, threadId: Long): ConversationNew? {
        val cursorToConversation = CursorToConversationImpl(context)
        val cursorToRecipient = CursorToRecipientImpl(context)
        val phoneNumberUtils = PhoneNumberUtils(context)

        return cursorToConversation.getConversationsCursor()
            ?.map(cursorToConversation::map)
            ?.firstOrNull { it.id == threadId }
            ?.let { conversation ->
                val realm = Realm.getDefaultInstance()
                val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())
                val lastMessage = realm.where(MessageNew::class.java).equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING).findFirst()?.let(realm::copyFromRealm)

                val recipients = conversation.recipients
                    .map { recipient -> recipient.id }
                    .map { id -> cursorToRecipient.getRecipientCursor(id) }
                    .mapNotNull { recipientCursor ->
                        // Map the recipient cursor to a list of recipients
                        recipientCursor?.use {
                            recipientCursor.map {
                                cursorToRecipient.map(
                                    recipientCursor
                                )
                            }
                        }
                    }
                    .flatten()
                    .map { recipient ->
                        recipient.apply {
                            contact = contacts.firstOrNull {
                                it.numbers.any {
                                    phoneNumberUtils.compare(
                                        recipient.address,
                                        it.address
                                    )
                                }
                            }
                        }
                    }

                conversation.recipients.clear()
                conversation.recipients.addAll(recipients)
                conversation.lastMessage = lastMessage
                realm.executeTransaction { it.insertOrUpdate(conversation) }
                realm.close()

                conversation
            }
    }

    private fun getConversation(threadId: Long): ConversationNew? {
        return Realm.getDefaultInstance()
            .apply { refresh() }
            .where(ConversationNew::class.java)
            .equalTo("id", threadId)
            .findFirst()
    }

    fun getThreadId(context: Context, recipients: Collection<String>): Long? {
        val phoneNumberUtils = PhoneNumberUtils(context)
        return Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(ConversationNew::class.java)
                .findAll()
                .asSequence()
                .filter { conversation -> conversation.recipients.size == recipients.size }
                .find { conversation ->
                    conversation.recipients.map { it.address }.all { address ->
                        recipients.any { recipient -> phoneNumberUtils.compare(recipient, address) }
                    }
                }?.id
        }
    }
}
