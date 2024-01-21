package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.FrameLayout
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityFeedbackBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility

@AndroidEntryPoint
class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    override fun getViewBinding() = ActivityFeedbackBinding.inflate(layoutInflater)

    var mBody = ""

    override fun initData() {
        bannerAd()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.tvSend.isSelected = true
        setUpToolBar()
        binding.etFeedback.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                try {
                    if (s.toString().length >= 2) {
                        mBody = s.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })
        binding.btnSend.setOnClickListener {
            if (mBody.length > 2) {
                val email = Uri.encode("calculatorlockhideuappphoto@gmail.com")
                val subject = "Report for issue"
                val body = Uri.encode(mBody)
                val uri = "mailto:$email?subject=$subject&body=$body"
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.type = "text/plain"
                intent.data = Uri.parse(uri)
                startActivity(intent)
            } else {
                showToast(getString(R.string.feedback_toast))
            }
        }
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()

        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.etFeedback.showKeyboard()
        }, 300)
    }

    private fun setUpToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = "${getString(R.string.feedback)}"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

}