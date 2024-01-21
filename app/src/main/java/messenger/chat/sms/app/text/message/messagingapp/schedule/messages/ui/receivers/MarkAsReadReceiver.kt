package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markMessageRead
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.anyOf
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.MARK_AS_READ
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.MESSAGE_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.refreshMessages
import javax.inject.Inject

@AndroidEntryPoint
class MarkAsReadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appDb: MessagesDatabase

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MARK_AS_READ -> {
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                val messageID = intent.getLongExtra(MESSAGE_ID, 0L)
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
                CoroutineScope(Dispatchers.IO).launch {
                    context.markMessageRead(messageID, false)
//                    refreshMessages()
//                    appDb.getMessagesDao().markAsReadMessage(messageID)
//                    appDb.getConversationsDao().markAsReadConversation(threadId)
                }
                context.notificationManager.cancel(threadId.toInt())
            }
        }
    }

}
