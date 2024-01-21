package messenger.chat.sms.app.text.message.messagingapp.schedule.messages

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.localehelper.LocaleHelper
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.localehelper.LocaleHelperApplicationDelegate
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.RealmConfiguration
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.SharedPre
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager
import plugin.adsdk.service.api.ListModel
import plugin.adsdk.service.utils.PurchaseHandler

@HiltAndroidApp
class MyApp : Application() {

    private val localeAppDelegate = LocaleHelperApplicationDelegate()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localeAppDelegate.attachBaseContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeAppDelegate.onConfigurationChanged(this)
    }

    override fun getApplicationContext(): Context =
        LocaleHelper.onAttach(super.getApplicationContext())

    override fun onCreate() {
        super.onCreate()
        initRealm()
        getConfig()
        val testDeviceIds = listOf("EF7B9A70EBF476FB4E4507F7ED7FAA92")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(configuration)

        AppOpenManager.init(this)
        PurchaseHandler.init(this)

        val savedTheme =
            SharedPre.getInt(this, "MyTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }

    private fun getConfig() {
        if (AdsUtility.config != null) {
            return
        }
        AdsUtility.config = ListModel()
//        if (PurchaseHandler.hasPurchased(this)) {
//            AdsUtility.config.migrateToNoAds()
//        }
        AdsUtility.config.packageName =
            "messenger.chat.sms.app.text.message.messagingapp.schedule.messages"
        AdsUtility.config.initialAppOpen = true
        AdsUtility.config.blockInitialAppOpen = true
        AdsUtility.config.activityCount = 0
        AdsUtility.config.adOnBack = true
        AdsUtility.config.privacyPolicyUrl =
            "https://sites.google.com/view/calculator-lock-hide-app-photo/home"
        AdsUtility.config.preloadNative = false
        AdsUtility.config.preloadBanner = true
        AdsUtility.config.preloadInterstitial = true
        AdsUtility.config.base64InAppKey = ""
    }

    private fun initRealm() {
        Realm.init(this)
        val realmConfiguration =
            RealmConfiguration.Builder().name("realDb.realm").allowWritesOnUiThread(true).build()
        try {
            Realm.getInstance(realmConfiguration)
            Realm.setDefaultConfiguration(realmConfiguration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var isNewMessageArrived = false
        var threadId = 0L
    }

}