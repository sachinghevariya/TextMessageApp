package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityBackupRestoreBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgress
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateProgressStatus
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services.RestoreBackupService
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.services.RestoreBackupService.Companion.isProgressEnable
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BackupRestoreActivity : BaseActivity<ActivityBackupRestoreBinding>() {
    override fun getViewBinding() = ActivityBackupRestoreBinding.inflate(layoutInflater)

    override fun initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }
        manageMarquee()
        setUpToolBar()
        viewClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun manageMarquee() {
        binding.tvTitle.isSelected = true
        binding.tvSubTitle.isSelected = true
        binding.tvBackup.isSelected = true
        binding.btnBackup.isSelected = true
        binding.btnRestore.isSelected = true
    }

    private fun viewClick() {
        binding.btnBackup.setOnClickListener(1000L) {
            Log.e("TAG", "viewClick: " + isProgressEnable)
            if (!isProgressEnable) {
                saveDocument.launch(getDatabaseBackupName())
            } else {
                Toast.makeText(this, getString(R.string.restore_back_msg), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnRestore.setOnClickListener(1000L) {
            val messageImportFileTypes = listOf("application/json")
            if (!isProgressEnable) {
                getContent.launch(messageImportFileTypes.toTypedArray())
            } else {
                Toast.makeText(this, getString(R.string.restore_back_msg), Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarTitle.text = getString(R.string.backup_restore)
        binding.toolbarTitle.isSelected = true
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getDatabaseBackupName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH)
        val currentDateTime = dateFormat.format(Date())
        return "backup_message_$currentDateTime"
    }

    private val saveDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument(CommonClass.MESSAGE_FILE_TYPE)) { uri ->
            if (uri != null) {
                startProgress(true)
                binding.tvSubTitle.text = "0/0 ${getString(R.string.message)}"
                binding.tvTitle.text = getString(R.string.backing_up_messages)
                RestoreBackupService.start(this, uri.toString(), 1)
//                viewModel.exportMessages(uri) { count, totalCount ->
//                    binding.tvSubTitle.text = "$count/$totalCount ${getString(R.string.message)}"
//                    if (count >= totalCount) {
//                        isBackingUp = false
//                        startProgress(false)
//                    }
//                }
            }
        }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                startProgress(true)
                binding.tvSubTitle.text = getString(R.string.restore)
                binding.tvSubTitle.text = "0/0 ${getString(R.string.restore)}"
                binding.tvTitle.text = getString(R.string.restore_message)
                RestoreBackupService.start(this, uri.toString(), 2)

//                viewModel.importMessages(uri) { count, totalCount ->
//                    runOnUiThread {
//                        binding.conversationsPb.progress = count
//                        binding.tvSubTitle.text =
//                            "$count/$totalCount ${getString(R.string.restore)}"
//                        if (count >= totalCount) {
//                            isRestoring = false
//                            viewModel.checkIfLastSync(true, isFromRestore = true) {}
//                            startProgress(false)
//                            EventBus.getDefault().post(UpdateConversations())
//                        }
//                    }
//                }
            } else {
            }
        }

    private fun startProgress(isShow: Boolean) {
        runOnUiThread {
            if (isShow) {
                if (binding.containerProgressView.visibility != View.VISIBLE) {
                    binding.containerProgressView.visible()
                    binding.conversationsPb.show()
                }
            } else {
                binding.containerProgressView.gone()
                binding.conversationsPb.hide()
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
    }

    override fun onBackPressed() {
        backPressed()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateProgressStatus(event: UpdateProgressStatus) {
        startProgress(event.isShow)
        binding.tvTitle.text = event.status
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateProgress(event: UpdateProgress) {
        startProgress(true)
        binding.tvSubTitle.text =
            "${event.count}/${event.total} ${event.status}"
    }


}