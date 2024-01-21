package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Sms
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getNameAndPhotoFromPhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.NotificationHelper
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.ensureBackgroundThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getNotificationBitmap
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.refreshMessages
import javax.inject.Inject

@AndroidEntryPoint
class SmsStatusSentReceiver : SendStatusReceiver() {

    @Inject
    lateinit var messagingUtils: MessagingUtils

    @Inject
    lateinit var appDb: MessagesDatabase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun updateAndroidDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        val messageUri: Uri? = intent.data
        val resultCode = resultCode
        val messagingUtils = messagingUtils

        val type = if (resultCode == Activity.RESULT_OK) {
            Sms.MESSAGE_TYPE_SENT
        } else {
            Sms.MESSAGE_TYPE_FAILED
        }
        messagingUtils.updateSmsMessageSendingStatus(messageUri, type)
        messagingUtils.maybeShowErrorToast(
            resultCode = resultCode,
            errorCode = intent.getIntExtra(EXTRA_ERROR_CODE, NO_ERROR_CODE)
        )
    }

    override fun updateAppDatabase(context: Context, intent: Intent, receiverResultCode: Int) {
        val messageUri = intent.data
        if (messageUri != null) {
            val messageId = messageUri.lastPathSegment?.toLong() ?: 0L
            ensureBackgroundThread {
                val type = if (receiverResultCode == Activity.RESULT_OK) {
                    Sms.MESSAGE_TYPE_SENT
                } else {
                    showSendingFailedNotification(context, messageId)
                    Sms.MESSAGE_TYPE_FAILED
                }
                updateRealmMessageStatus(messageId, type)
//                appDb.getMessagesDao().updateType(messageId, type)
//                refreshMessages()
            }
        }
    }

    private fun updateRealmMessageStatus(id: Long, status: Int) {
        getRealmThread { realm ->
            realm.refresh()
            val message = realm.where(MessageNew::class.java).equalTo("contentId", id).findFirst()
            message?.let {
                realm.executeTransaction {
                    message.boxId = status
                }
            }
        }
    }

    private fun showSendingFailedNotification(context: Context, messageId: Long) {
        Handler(Looper.getMainLooper()).post {
            /* if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                 return@post
             }*/
            val myPreferences = MyPreferences(context)
            CoroutineScope(Dispatchers.IO).launch {
                getRealmThread { realm ->
                    val message =
                        realm.where(MessageNew::class.java).equalTo("contentId", messageId)
                            .findFirst().let { realm.copyFromRealm(it) }




//                    val message = appDb.getMessagesDao().getMessageById(messageId)

                    if (message?.threadId != null) {
                        val nameAndPhoto =
                            context.getNameAndPhotoFromPhoneNumber(message?.address!!)
                        val threadId = message.threadId
                        val bitmap = context.getNotificationBitmap(nameAndPhoto.second)
                        notificationHelper.showSendingFailedNotification(
                            nameAndPhoto.first,
                            message,
                            threadId,
                            bitmap,
                            myPreferences
                        )
                    }
                }

            }
        }
    }
}
