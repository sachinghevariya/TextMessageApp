package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun getMessagesDatabase(@ApplicationContext context: Context): MessagesDatabase {
        return MessagesDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun getMyPreferences(@ApplicationContext context: Context): MyPreferences {
        return MyPreferences.getPreferences(context)!!
    }
}