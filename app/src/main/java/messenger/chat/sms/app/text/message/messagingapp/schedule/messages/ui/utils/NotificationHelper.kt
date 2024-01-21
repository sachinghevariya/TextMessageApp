package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.ConversationDetailActivityNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TempActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers.DirectReplyReceiver
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers.MarkAsReadReceiver
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.MARK_AS_READ
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.MESSAGE_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.NOTIFICATION_CHANNEL
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.REPLY
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val soundUri get() = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private val user = Person.Builder()
        .setName(context.getString(R.string.me))
        .build()

    init {
        maybeCreateChannel(context.getString(R.string.channel_received_sms))
        maybeCreateChannel(context.getString(R.string.message_not_sent_short))
    }

    @SuppressLint("NewApi")
    fun showMessageNotification(
        messageId: Long,
        address: String,
        body: String,
        threadId: Long,
        bitmap: Bitmap?,
        sender: String?,
        alertOnlyOnce: Boolean = false,
        myPreferences: MyPreferences
    ) {
        val notificationId = threadId.toInt()
        val contentIntent = Intent(context, ConversationDetailActivityNew::class.java).apply {
            putExtra(THREAD_ID, threadId)
            putExtra(THREAD_TITLE, sender)
        }
        val taskStackBuilder = TaskStackBuilder.create(context)
            .addParentStack(ConversationDetailActivityNew::class.java)
            .addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

//        val contentPI = PendingIntent.getActivity(
//            context,
//            notificationId,
//            contentIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )

        val markAsReadIntent = Intent(context, MarkAsReadReceiver::class.java).apply {
            action = MARK_AS_READ
            putExtra(THREAD_ID, threadId)
            putExtra(MESSAGE_ID, messageId)
        }
        val markAsReadPendingIntent =
            PendingIntent.getBroadcast(
                context,
                notificationId,
                markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

        var replyAction: NotificationCompat.Action? = null
        val isNoReplySms = isShortCode(address)
        if (isNougatPlus() && !isNoReplySms) {
            val replyLabel = context.getString(R.string.reply)
            val remoteInput = RemoteInput.Builder(REPLY)
                .setLabel(replyLabel)
                .build()

            val replyIntent = Intent(context, DirectReplyReceiver::class.java).apply {
                putExtra(THREAD_ID, threadId)
                putExtra(THREAD_NUMBER, address)
            }

            val replyPendingIntent =
                PendingIntent.getBroadcast(
                    context.applicationContext,
                    notificationId,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            replyAction = NotificationCompat.Action.Builder(
                R.drawable.ic_send_vector,
                replyLabel,
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()
        }

        val largeIcon = if (myPreferences.showProfilePhoto) {
            bitmap ?: if (sender != null) {
                getContactLetterIcon(context, sender)
            } else {
                null
            }
        } else {
            if (sender != null) {
                getContactLetterIcon(context, sender)
            } else {
                null
            }
        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL).apply {
            if (myPreferences.showNotificationMessage) {
                setLargeIcon(largeIcon)
                setStyle(
                    getMessagesStyle(
                        address,
                        body,
                        notificationId,
                        sender,
                        myPreferences.showNotificationGroup
                    )
                )
            }
            setSmallIcon(R.drawable.ic_messenger)
            setContentIntent(contentPI)
            priority = NotificationCompat.PRIORITY_MAX
            setDefaults(Notification.DEFAULT_LIGHTS)
            setCategory(Notification.CATEGORY_MESSAGE)
            setAutoCancel(true)
            setOnlyAlertOnce(alertOnlyOnce)
            setSound(soundUri, AudioManager.STREAM_NOTIFICATION)
            setGroup(notificationId.toString())
        }

        if (replyAction != null) {
            builder.addAction(replyAction)
        }

        builder.addAction(
            R.drawable.ic_check_vector,
            context.getString(R.string.mark_as_read),
            markAsReadPendingIntent
        ).setChannelId(NOTIFICATION_CHANNEL)

        notificationManager.notify(notificationId, builder.build())
    }

    private fun getMessagesStyle(
        address: String,
        body: String,
        notificationId: Int,
        name: String?,
        showNotificationMessage: Boolean
    ): NotificationCompat.MessagingStyle {
        val sender = if (name != null) {
            Person.Builder()
                .setName(name)
                .setKey(address)
                .build()
        } else {
            null
        }

        return if (showNotificationMessage) {
            NotificationCompat.MessagingStyle(user).also { style ->
                getOldMessages(notificationId).forEach {
                    style.addMessage(it)
                }
                val newMessage = NotificationCompat.MessagingStyle.Message(
                    body,
                    System.currentTimeMillis(),
                    sender
                )
                style.addMessage(newMessage)
            }
        } else {
            NotificationCompat.MessagingStyle(user).also { style ->
                val newMessage = NotificationCompat.MessagingStyle.Message(
                    body,
                    System.currentTimeMillis(),
                    sender
                )
                style.addMessage(newMessage)
            }
        }

    }

    private fun getOldMessages(notificationId: Int): List<NotificationCompat.MessagingStyle.Message> {
        if (!isNougatPlus()) {
            return emptyList()
        }
        val currentNotification =
            notificationManager.activeNotifications.find { it.id == notificationId }
        return if (currentNotification != null) {
            val activeStyle =
                NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
                    currentNotification.notification
                )
            activeStyle?.messages.orEmpty()
        } else {
            emptyList()
        }
    }

    private fun maybeCreateChannel(name: String) {
        if (isOreoPlus()) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .build()

            val id = NOTIFICATION_CHANNEL
            val importance = NotificationManager.IMPORTANCE_HIGH
            NotificationChannel(id, name, importance).apply {
                setBypassDnd(false)
                enableLights(true)
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    fun showSendingFailedNotification(
        title: String,
        message: MessageNew,
        threadId: Long,
        bitmap: Bitmap?,
        myPreferences: MyPreferences
    ) {
        val sender = title
        val notificationId = threadId.toInt()
        val intent = Intent(context, ConversationDetailActivityNew::class.java).apply {
            putExtra(THREAD_ID, threadId)
            putExtra(THREAD_TITLE, sender)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val summaryText = String.format(context.getString(R.string.message_sending_error), sender)
        /*val largeIcon = if (sender != null) {
            getContactLetterIcon(context, sender)
        } else {
            null
        }*/

        val largeIcon = if (myPreferences.showProfilePhoto) {
            bitmap ?: if (sender != null) {
                getContactLetterIcon(context, sender)
            } else {
                null
            }
        } else {
            if (sender != null) {
                getContactLetterIcon(context, sender)
            } else {
                null
            }
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setContentTitle(context.getString(R.string.message_not_sent_title))
            .setContentText(summaryText)
            .setSmallIcon(R.drawable.ic_messenger)
            .setLargeIcon(largeIcon)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
//            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setChannelId(NOTIFICATION_CHANNEL)

        notificationManager.notify(notificationId, builder.build())
    }

}