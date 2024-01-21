package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.os.Bundle
import android.widget.FrameLayout
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivitySwipeActionsBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.DialogSwipeAction
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackS
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSwipeAction
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showCustomDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class SwipeActionsActivity : BaseActivity<ActivitySwipeActionsBinding>() {

    override fun getViewBinding() = ActivitySwipeActionsBinding.inflate(layoutInflater)

    private var isRightAction = false
    private val DIALOG_SHOWN_KEY = "dialog_shown"

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putBoolean(DIALOG_SHOWN_KEY, isRightAction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            isRightAction = savedInstanceState.getBoolean(DIALOG_SHOWN_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        manageMarquee()
        setDefaultIcons(true)
        setDefaultIcons(false)
        setUpToolBar()
        viewClick()
    }

    private fun manageMarquee() {
        binding.tvSwipeLeft.isSelected = true
        binding.tvSwipeLeftActionTitle.isSelected = true
        binding.btnChangeLeftAction.isSelected = true
        binding.tvSwipeRight.isSelected = true
        binding.tvSwipeRightActionTitle.isSelected = true
        binding.btnChangeRightAction.isSelected = true
    }

    private fun viewClick() {
        binding.btnChangeRightAction.setOnClickListener(1000L) {
            isRightAction = true
            showActionDialog(true)
        }
        binding.btnChangeLeftAction.setOnClickListener(1000L) {
            isRightAction = false
            showActionDialog(false)
        }
    }

    private fun showActionDialog(isRightAction: Boolean) {
        val dialog = DialogSwipeAction()
        val args = Bundle()
        args.putBoolean("KEY", isRightAction)
        dialog.arguments = args
        showCustomDialog(this, dialog)

        /*showCustomDialog(this, DialogSwipeAction.newInstance(myPreferences, isRightAction) {
            Log.e("TAG", "showActionDialog: ")
            EventBus.getDefault().post(UpdateSwipeAction())
            if (isRightAction) {
                binding.rlRightActionContainer.visible()
                when (it) {
                    1 -> {
                        binding.tvSwipeRightActionTitle.text = getString(R.string.archive)
                        binding.ivRightAction.setImageResource(R.drawable.ic_archive)
                    }

                    2 -> {
                        binding.tvSwipeRightActionTitle.text = getString(R.string.delete)
                        binding.ivRightAction.setImageResource(R.drawable.ic_delete)
                    }

                    3 -> {
                        binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_read)
                        binding.ivRightAction.setImageResource(R.drawable.ic_mark_read)
                    }

                    4 -> {
                        binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_unread)
                        binding.ivRightAction.setImageResource(R.drawable.ic_mark_unread)
                    }

                    5 -> {
                        binding.tvSwipeRightActionTitle.text = getString(R.string.none)
                        binding.rlRightActionContainer.gone()
                        binding.ivRightAction.setImageResource(R.drawable.ic_block)
                    }
                }
            } else {
                binding.rlLeftActionContainer.visible()
                when (it) {
                    1 -> {
                        binding.tvSwipeLeftActionTitle.text = getString(R.string.archive)
                        binding.ivLeftAction.setImageResource(R.drawable.ic_archive)
                    }

                    2 -> {
                        binding.tvSwipeLeftActionTitle.text = getString(R.string.delete)
                        binding.ivLeftAction.setImageResource(R.drawable.ic_delete)
                    }

                    3 -> {
                        binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_read)
                        binding.ivLeftAction.setImageResource(R.drawable.ic_mark_read)
                    }

                    4 -> {
                        binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_unread)
                        binding.ivLeftAction.setImageResource(R.drawable.ic_mark_unread)
                    }

                    5 -> {
                        binding.tvSwipeLeftActionTitle.text = getString(R.string.none)
                        binding.rlLeftActionContainer.gone()
                        binding.ivLeftAction.setImageResource(R.drawable.ic_block)
                    }
                }
            }
        })*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallbackS(event: CallbackS) {
        EventBus.getDefault().post(UpdateSwipeAction())
        if (isRightAction) {
            binding.rlRightActionContainer.visible()
            when (event.int) {
                1 -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.archive)
                    binding.ivRightAction.setImageResource(R.drawable.ic_archive)
                }

                2 -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.delete)
                    binding.ivRightAction.setImageResource(R.drawable.ic_delete)
                }

                3 -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_read)
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_read)
                }

                4 -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_unread)
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_unread)
                }

                5 -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.none)
                    binding.rlRightActionContainer.gone()
                    binding.ivRightAction.setImageResource(R.drawable.ic_block)
                }
            }
        } else {
            binding.rlLeftActionContainer.visible()
            when (event.int) {
                1 -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.archive)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_archive)
                }

                2 -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.delete)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_delete)
                }

                3 -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_read)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_read)
                }

                4 -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_unread)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_unread)
                }

                5 -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.none)
                    binding.rlLeftActionContainer.gone()
                    binding.ivLeftAction.setImageResource(R.drawable.ic_block)
                }
            }
        }
    }


    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = getString(R.string.swipe_actions)
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setDefaultIcons(isRightAction: Boolean) {
        val defaultAction = if (isRightAction) {
            myPreferences.swipeRightActionLabel
        } else {
            myPreferences.swipeLeftActionLabel
        }

        if (isRightAction) {
            binding.rlRightActionContainer.visible()
            when (defaultAction) {
                "swipeArchive" -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.archive)
                    binding.ivRightAction.setImageResource(R.drawable.ic_archive)
                }

                "swipeDelete" -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.delete)
                    binding.ivRightAction.setImageResource(R.drawable.ic_delete)
                }

                "swipeMarkRead" -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_read)
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_read)
                }

                "swipeMarkUnRead" -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.mark_as_unread)
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_unread)
                }

                "swipeNone" -> {
                    binding.tvSwipeRightActionTitle.text = getString(R.string.none)
                    binding.rlRightActionContainer.gone()
                    binding.ivRightAction.setImageResource(R.drawable.ic_block)
                }
            }
        } else {
            binding.rlLeftActionContainer.visible()
            when (defaultAction) {
                "swipeArchive" -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.archive)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_archive)
                }

                "swipeDelete" -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.delete)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_delete)
                }

                "swipeMarkRead" -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_read)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_read)
                }

                "swipeMarkUnRead" -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.mark_as_unread)
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_unread)
                }

                "swipeNone" -> {
                    binding.tvSwipeLeftActionTitle.text = getString(R.string.none)
                    binding.rlLeftActionContainer.gone()
                    binding.ivLeftAction.setImageResource(R.drawable.ic_block)
                }
            }
        }

        /*when (defaultAction) {
            "swipeArchive" -> {
                if (isRightAction) {
                    binding.ivRightAction.setImageResource(R.drawable.ic_archive)
                } else {
                    binding.ivLeftAction.setImageResource(R.drawable.ic_archive)
                }
            }

            "swipeDelete" -> {
                if (isRightAction) {
                    binding.ivRightAction.setImageResource(R.drawable.ic_delete)
                } else {
                    binding.ivLeftAction.setImageResource(R.drawable.ic_delete)
                }
            }

            "swipeMarkRead" -> {
                if (isRightAction) {
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_read)
                } else {
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_read)
                }
            }

            "swipeMarkUnRead" -> {
                if (isRightAction) {
                    binding.ivRightAction.setImageResource(R.drawable.ic_mark_unread)
                } else {
                    binding.ivLeftAction.setImageResource(R.drawable.ic_mark_unread)
                }
            }

            "swipeNone" -> {
                if (isRightAction) {
                    binding.ivRightAction.setImageResource(R.drawable.ic_block)
                } else {
                    binding.ivLeftAction.setImageResource(R.drawable.ic_block)
                }
            }
        }*/
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}