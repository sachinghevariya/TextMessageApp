package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ScheduledMessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers.ScheduledMessageReceiver
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.SCHEDULED_MESSAGE_ID

fun Context.getScheduleSendPendingIntent(message: ScheduledMessageNew): PendingIntent {
    val intent = Intent(this, ScheduledMessageReceiver::class.java)
//    intent.putExtra(THREAD_ID, message.threadId)
    intent.putExtra(SCHEDULED_MESSAGE_ID, message.id)

    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    return PendingIntent.getBroadcast(this, message.id.toInt(), intent, flags)
}

fun Context.scheduleMessage(message: ScheduledMessageNew) {
    val pendingIntent = getScheduleSendPendingIntent(message)
    val triggerAtMillis = message.date

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    AlarmManagerCompat.setExactAndAllowWhileIdle(
        alarmManager,
        AlarmManager.RTC_WAKEUP,
        triggerAtMillis,
        pendingIntent
    )
}

fun Context.cancelScheduleSendPendingIntent(messageId: Long) {
    val intent = Intent(this, ScheduledMessageReceiver::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    PendingIntent.getBroadcast(this, messageId.toInt(), intent, flags).cancel()
}
