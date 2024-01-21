package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TempActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import plugin.adsdk.extras.BaseLauncherActivity
import plugin.adsdk.service.AppOpenManager
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseLauncherActivity(R.layout.activity_splash) {

    @Inject
    lateinit var myPreferences: MyPreferences

    override fun init() {
        AppOpenManager.blockAppOpen(this)
        installSplashScreen()
        changeLanguage(MyPreferences(this).language!!)
    }

    fun Context.changeLanguage(languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            configuration.setLocale(locale)
        }
        configuration.setLayoutDirection(locale)
        return createConfigurationContext(configuration)
    }

    override fun destinationIntent(): Intent {
        return if (!myPreferences.languageShown) {
            Intent(
                this,
                SelectLanguageActivity::class.java
            ).putExtra("isFromSplash", true)
        } else {
            Intent(this, TempActivity::class.java)
        }
    }

    override fun permissions(): Array<String?> {
        return arrayOf()
    }

    override fun baseURL(): String {
        return BASE_URL
    }

    companion object {
        private const val BASE_URL = "https://ht.askforad.com/"
    }

}