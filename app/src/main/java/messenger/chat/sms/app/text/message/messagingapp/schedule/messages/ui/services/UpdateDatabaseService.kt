package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.FetchDataRepository
import javax.inject.Inject

@AndroidEntryPoint
class UpdateDatabaseService : Service() {

    @Inject
    lateinit var mRepository: FetchDataRepository

    @Inject
    lateinit var mRepositoryData: DataRepository

    private var serviceJob: Job? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val builder =
            NotificationCompat.Builder(this, "DataFetchServiceChannelDataFetchServiceChannel")
                .setSmallIcon(R.drawable.ic_messenger)
                .setContentTitle("Observe message")
                .setPriority(NotificationCompat.PRIORITY_LOW)

        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        startForeground(10101010, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "DataFetchService Channel"
            val descriptionText = "DataFetchService Channel Description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                "DataFetchServiceChannelDataFetchServiceChannel",
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start fetching data in a coroutine
//        val contentResolver = contentResolver
//        contentResolver.registerContentObserver(
//            Telephony.Sms.CONTENT_URI, true, MySMSObserver(mRepository.mContext)
//        )
        return START_STICKY
    }
}