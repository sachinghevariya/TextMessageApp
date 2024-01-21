package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.SmsSender
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.NotificationHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun getSmsSender(@ApplicationContext context: Context): SmsSender {
        return SmsSender.getInstance(context)
    }

    @Singleton
    @Provides
    fun getMessagingUtils(@ApplicationContext context: Context): MessagingUtils {
        return MessagingUtils(context)
    }

    @Singleton
    @Provides
    fun getNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

}