package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import javax.inject.Inject

@AndroidEntryPoint
class DefaultSmsChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prefs: MyPreferences

    override fun onReceive(context: Context, intent: Intent) {


        if (intent.getBooleanExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false)) {
//            val pendingResult = goAsync()
//            syncMessages.execute(Unit) { pendingResult.finish() }
        }
    }

}