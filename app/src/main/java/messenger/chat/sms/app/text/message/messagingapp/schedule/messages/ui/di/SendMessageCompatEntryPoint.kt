package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SendMessageCompatEntryPoint {
    fun messagingUtils(): MessagingUtils
}