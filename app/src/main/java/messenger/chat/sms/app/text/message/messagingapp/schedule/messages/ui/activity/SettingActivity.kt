package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.FrameLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.BuildConfig
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivitySettingBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.DialogTheme
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSetting
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    private var isDialogShown = false
    private val DIALOG_SHOWN_KEY = "dialog_shown"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putBoolean(DIALOG_SHOWN_KEY, isDialogShown)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            isDialogShown = savedInstanceState.getBoolean(DIALOG_SHOWN_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getViewBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initData() {
        bannerAd()
        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }
        setUpToolBar()
        viewClick()
        manageMarquee()
    }

    private fun viewClick() {
        binding.subVersionNumber.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME}"

        binding.switchProfilePhotos.isChecked = myPreferences.showProfilePhoto
        binding.switchNotificationGroup.isChecked = myPreferences.showNotificationGroup
        binding.switchNotificationMessage.isChecked = myPreferences.showNotificationMessage

        binding.btnLanguage.setOnClickListener(1000L) {
            startActivity(Intent(this@SettingActivity, SelectLanguageActivity::class.java))
        }
        binding.btnTheme.setOnClickListener(1000L) {
            isDialogShown = false
            openDialogTheme()
        }
        binding.btnArchive.setOnClickListener(1000L) {
            startActivity(Intent(this@SettingActivity, ArchivedActivity::class.java))
        }
        binding.btnRateUs.setOnClickListener(1000L) {
            AdsUtility.rateUs(this)
        }
        binding.btnShare.setOnClickListener(1000L) {
            AdsUtility.shareApp(this)
        }
        binding.btnPrivacy.setOnClickListener(1000L) {
            AdsUtility.privacyPolicy(this)
        }
        binding.btnFeedback.setOnClickListener(1000L) {
            showInterstitial(Intent(this, FeedbackActivity::class.java))
        }

        binding.switchProfilePhotos.setOnCheckedChangeListener { _, isChecked ->
            myPreferences.showProfilePhoto = isChecked
            EventBus.getDefault().post(UpdateSetting())
        }

        binding.switchNotificationGroup.setOnCheckedChangeListener { _, isChecked ->
            myPreferences.showNotificationGroup = isChecked
        }

        binding.switchNotificationMessage.setOnCheckedChangeListener { _, isChecked ->
            myPreferences.showNotificationMessage = isChecked
        }
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()
        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
    }

    private fun manageMarquee() {
        binding.subVersionNumber.isSelected = true
        binding.tvAbout.isSelected = true
        binding.tvPolicy.isSelected = true
        binding.tvShare.isSelected = true
        binding.tvRate.isSelected = true
        binding.tvFeedback.isSelected = true
        binding.tvShowMessage.isSelected = true
        binding.tvSubMessage.isSelected = true
        binding.tvNotification.isSelected = true
        binding.tvSubShowNotification.isSelected = true
        binding.tvShowNotification.isSelected = true
        binding.tvSubShowProfile.isSelected = true
        binding.tvShowProfile.isSelected = true
        binding.tvDisplay.isSelected = true
        binding.tvArchive.isSelected = true
        binding.tvLang.isSelected = true

    }

    private fun setUpToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = getString(R.string.settings)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun openDialogTheme() {
        if (!isDialogShown) {
            val dialogTheme = DialogTheme()
            dialogTheme.show(supportFragmentManager, "Theme")
            isDialogShown = true
        }


    }

    //Rate
    private fun rateUs(activity: BaseActivity<*>) {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)
                )
            )
        }
    }

    //Share
    private fun shareApp(activity: BaseActivity<*>) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name))
            val shareMessage = (("\nPlease try this application\n\n"
                    + "https://play.google.com/store/apps/details?id="
                    + activity.packageName) + "\n")
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            activity.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (ignored: Exception) {
        }
    }

    //Policy
    private fun privacyPolicy(activity: BaseActivity<*>) {
        try {
            val url: String =
                if (AdsUtility.config.privacyPolicyUrl != null && AdsUtility.config.privacyPolicyUrl?.isNotEmpty()!!) {
                    AdsUtility.config.privacyPolicyUrl
                } else {
                    "https://sites.google.com/view/calculator-lock-hide-app-photo/home"
                }

            val browserIntent: Intent = if (!TextUtils.isEmpty(url)) {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            } else {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://mapateltechnowebprivacypolicy.blogspot.com/2021/08/privacypolicy.html")
                )
            }
            activity.startActivity(browserIntent)
        } catch (ignored: Exception) {
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        backPressed()
    }

}