package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.content.contentValuesOf
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.MyApp
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.MyApp.Companion.isNewMessageArrived
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getNameAndPhotoFromPhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToConversationImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToRecipientImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TelephonyCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.tryOrNull
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.NotificationHelper
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getNotificationBitmap
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.refreshMessages
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var mAppDatabase: MessagesDatabase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var myPreferences: MyPreferences

    override fun onReceive(context: Context, intent: Intent) {
            val blockedThreadIds = context.getBlockedThreadIds()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
                var address = ""
                var body = ""
                var subject = ""
                var date = 0L
                var threadId = 0L
                var status = Telephony.Sms.STATUS_NONE
                val type = Telephony.Sms.MESSAGE_TYPE_INBOX
                val read = 0
                val subscriptionId = intent.getIntExtra("subscription", -1)
                CoroutineScope(Dispatchers.IO).launch {
                    messages.forEach {
                        address = it.displayOriginatingAddress ?: ""
                        subject = it.pseudoSubject
                        status = it.status
                        body += it.displayMessageBody
                        date = System.currentTimeMillis()
                    Log.e("TAG", "onReceive: SMS----------------- " + address)
                        threadId = context.getThreadId(setOf(address))
                    Log.e("TAG", "onReceive: SMS----------threadId------- " + threadId)

                        if (blockedThreadIds.contains(threadId)) {
                            return@forEach
                        }
                        val message = insertReceivedSms(
                            context,
                            subscriptionId,
                            address,
                            body,
                            threadId,
                            System.currentTimeMillis()
                        )

                        Log.e("TAG", "onReceive: ${message.toString()}")

                        try {
                            updateConversations(threadId)
                            getOrCreateConversation(context, listOf(address))
                            refreshMessages(true)
                        } catch (ignored: Exception) {
                        }
                        myPreferences.lastUpdateDbTime = message.date.toLong()
                        isNewMessageArrived = true
                        val nameAndPhoto = context.getNameAndPhotoFromPhoneNumber(address)
                        val senderName = nameAndPhoto.first
                        val photoUri = nameAndPhoto.second
                        val bitmap = context.getNotificationBitmap(photoUri)
                        if (CommonClass.detailScreenThreadId != threadId) {
                            notificationHelper.showMessageNotification(
                                message.contentId,
                                address,
                                body,
                                threadId,
                                bitmap,
                                senderName,
                                false,
                                myPreferences
                            )
                        }
                    }
                }
            }

    }

    private fun updateConversations(vararg threadIds: Long) {
        getRealmThread { realm ->
            threadIds.forEach { threadId ->
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

    private fun insertReceivedSms(
        context: Context,
        subId: Int,
        address: String,
        body: String,
        threadIdNew: Long,
        sentTime: Long
    ): MessageNew {

//        val messageIds = KeyManagerImpl()
        // Insert the message to Realm
        val message = MessageNew().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.subId = subId

            id = KeyManagerImpl.newInstance().newId()
            threadId = threadIdNew
            boxId = Telephony.Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            read = false
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: MessageNew? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        Log.e("TAG", "insertReceivedSms: "+message.toString() )
        // Insert the message to the native content provider
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE_SENT to sentTime,
            Telephony.Sms.DATE to sentTime
        )
        context.contentResolver.insert(
            Telephony.Sms.CONTENT_URI,
            values
        )?.lastPathSegment?.toLong()?.let { id ->
            // Update the contentId after the message has been inserted to the content provider
            realm.executeTransaction {
                managedMessage?.contentId = id
                message.contentId = id
            }
        }

        MyApp.threadId = threadIdNew

        realm.close()

        return message
    }
}
