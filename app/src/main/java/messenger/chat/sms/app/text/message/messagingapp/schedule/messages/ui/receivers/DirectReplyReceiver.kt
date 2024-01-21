package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.RemoteInput
import androidx.core.content.contentValuesOf
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markThreadMessagesRead
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.sendMessageCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.REPLY
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.NotificationHelper
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getNotificationBitmap
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.refreshMessages
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.removeDiacriticsIfNeeded
import javax.inject.Inject

@AndroidEntryPoint
class DirectReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appDatabase: MessagesDatabase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var myPreferences: MyPreferences

    private var context: Context? = null

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        val address = intent.getStringExtra(THREAD_NUMBER)
        val threadId = intent.getLongExtra(THREAD_ID, 0L)
        var body =
            RemoteInput.getResultsFromIntent(intent)?.getCharSequence(REPLY)?.toString()
                ?: return

        body = context.removeDiacriticsIfNeeded(body)

        if (address != null) {
            val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
            val messagingUtils = MessagingUtils(context)

            CoroutineScope(Dispatchers.IO).launch {
                var messageId = 0L
                try {
                    val message = insertSentSms(
                        subscriptionId,
                        threadId,
                        address,
                        body,
                        System.currentTimeMillis()
                    )

                    context.sendMessageCompat(
                        body,
                        listOf(address),
                        subscriptionId,
                        message.contentId,
                        messagingUtils
                    )
//                    val message = context.getMessages(threadId, limit = 1).lastOrNull()
                    if (message != null) {
//                        appDatabase.getMessagesDao().insertOrUpdate(message)
                        messageId = message.contentId
//                        refreshMessages()
                        updateConversations(threadId, messageId, body, address)
                        /*try {
                            var conversation: Conversation? = null
                            conversation = appDatabase.getConversationsDao()
                                .getConversationByThreadId(threadId)
                            if (conversation != null) {
                                conversation.msgId = messageId
                                conversation.date = (System.currentTimeMillis() / 1000).toInt()
                                conversation.snippet = message.body
                                conversation.read = message.read
                                appDatabase.getConversationsDao().updateConversation(conversation)
                            } else {
                                conversation = context.getConversations(threadId).firstOrNull()
                                    ?: return@launch
                                conversation.msgId = messageId
                                conversation.date = (System.currentTimeMillis() / 1000).toInt()
                                conversation.snippet = message.body
                                conversation.read = message.read
                                appDatabase.getConversationsDao().insertOrUpdate(conversation)
                            }
                        } catch (ignored: Exception) {}
*/

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                context.markThreadMessagesRead(threadId)
                appDatabase.getConversationsDao().markAsReadConversation(threadId)
            }
        }
    }

    private fun updateConversations(threadId: Long, msgId: Long, body: String, address: String) {
        getRealmThread { realm ->
            realm.refresh()

            val conversation = realm
                .where(ConversationNew::class.java)
                .equalTo("id", threadId)
                .findFirst() ?: return@getRealmThread

            val message = realm
                .where(MessageNew::class.java)
                .equalTo("threadId", threadId)
                .sort("date", Sort.DESCENDING)
                .findFirst()
            realm.executeTransaction {
                conversation.lastMessage = message
            }


            val phoneNumberUtils = PhoneNumberUtils(context!!)
            val lastMessage = conversation.lastMessage
            val recipient = when {
                conversation.recipients.size == 1 || lastMessage == null -> conversation.recipients.firstOrNull()
                else -> conversation.recipients.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, lastMessage.address)
                }
            }

            var photoUri = ""
            photoUri = if (recipient?.contact?.photoUri == null) {
                ""
            } else {
                recipient.contact?.photoUri!!
            }
//                        val photoUri = message.senderPhotoUri
            val bitmap = context?.getNotificationBitmap(photoUri)
            Handler(Looper.getMainLooper()).post {
                notificationHelper.showMessageNotification(
                    msgId,
                    address,
                    body,
                    threadId,
                    bitmap,
                    sender = null,
                    alertOnlyOnce = true,
                    myPreferences
                )
            }

        }
    }

    private fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): MessageNew {
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


        val uri = context?.contentResolver?.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
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

}
