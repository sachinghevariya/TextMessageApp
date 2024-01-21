package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogSwipeActionBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackS
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class DialogSwipeAction : BaseDialog<DialogSwipeActionBinding>() {

    private lateinit var myPreferences: MyPreferences
    private var isRightAction: Boolean = false

    override fun getViewBinding() = DialogSwipeActionBinding.inflate(layoutInflater)

    private fun manageNotInitVariables() {
        if (!::myPreferences.isInitialized) {
            myPreferences = MyPreferences(requireContext())
        }
    }

    override fun init() {
        isRightAction = arguments?.getBoolean("KEY", false) ?: false

        binding.switchArchive.isEnabled = false
        binding.switchDelete.isEnabled = false
        binding.switchMarkRead.isEnabled = false
        binding.switchMarkUnRead.isEnabled = false
        binding.switchNone.isEnabled = false
        manageNotInitVariables()
        setDefaultSelectedAction(isRightAction)

        binding.btnArchive.setOnClickListener(500L) {
            deSelectAll()
            binding.switchArchive.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeArchive"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeArchive"
            }
            EventBus.getDefault().post(CallbackS(1))
            dismiss()
        }
        binding.btnDelete.setOnClickListener(500L) {
            deSelectAll()
            binding.switchDelete.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeDelete"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeDelete"
            }
            EventBus.getDefault().post(CallbackS(2))
            dismiss()
        }
        binding.btnMarkRead.setOnClickListener(500L) {
            deSelectAll()
            binding.switchMarkRead.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeMarkRead"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeMarkRead"
            }
            EventBus.getDefault().post(CallbackS(3))
            dismiss()
        }
        binding.btnMarkUnRead.setOnClickListener(500L) {
            deSelectAll()
            binding.switchMarkUnRead.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeMarkUnRead"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeMarkUnRead"
            }
            EventBus.getDefault().post(CallbackS(4))
            dismiss()
        }
        binding.btnNone.setOnClickListener(500L) {
            deSelectAll()
            binding.switchNone.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeNone"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeNone"
            }
            EventBus.getDefault().post(CallbackS(5))
            dismiss()
        }
        binding.btnCancel.setOnClickListener(500L) {
            dismiss()
        }


        binding.btnViewArchive.setOnClickListener(500L) {
            deSelectAll()
            binding.switchArchive.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeArchive"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeArchive"
            }
            EventBus.getDefault().post(CallbackS(1))
            dismiss()
        }
        binding.btnViewDelete.setOnClickListener(500L) {
            deSelectAll()
            binding.switchDelete.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeDelete"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeDelete"
            }
            EventBus.getDefault().post(CallbackS(2))
            dismiss()
        }
        binding.btnViewMarkRead.setOnClickListener(500L) {
            deSelectAll()
            binding.switchMarkRead.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeMarkRead"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeMarkRead"
            }
            EventBus.getDefault().post(CallbackS(3))
            dismiss()
        }
        binding.btnViewMarkUnRead.setOnClickListener(500L) {
            deSelectAll()
            binding.switchMarkUnRead.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeMarkUnRead"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeMarkUnRead"
            }
            EventBus.getDefault().post(CallbackS(4))
            dismiss()
        }
        binding.btnViewNone.setOnClickListener(500L) {
            deSelectAll()
            binding.switchNone.isChecked = true
            if (isRightAction) {
                myPreferences.swipeRightActionLabel = "swipeNone"
            } else {
                myPreferences.swipeLeftActionLabel = "swipeNone"
            }
            EventBus.getDefault().post(CallbackS(5))
            dismiss()
        }


        /*binding.switchArchive.setOnCheckedChangeListener { _, _ ->
            deSelectAll()
            binding.switchArchive.isChecked = true
            if (isRightAction) {
                myPreferences?.swipeRightActionLabel = "swipeArchive"
            } else {
                myPreferences?.swipeLeftActionLabel = "swipeArchive"
            }
            callback.invoke(1)
            dismiss()
        }

        binding.switchDelete.setOnCheckedChangeListener { _, _ ->
            deSelectAll()
            binding.switchDelete.isChecked = true
            if (isRightAction) {
                myPreferences?.swipeRightActionLabel = "swipeDelete"
            } else {
                myPreferences?.swipeLeftActionLabel = "swipeDelete"
            }
            callback.invoke(2)
            dismiss()
        }

        binding.switchMarkRead.setOnCheckedChangeListener { _, _ ->
            deSelectAll()
            binding.switchMarkRead.isChecked = true
            if (isRightAction) {
                myPreferences?.swipeRightActionLabel = "swipeMarkRead"
            } else {
                myPreferences?.swipeLeftActionLabel = "swipeMarkRead"
            }
            callback.invoke(3)
            dismiss()
        }

        binding.switchMarkUnRead.setOnCheckedChangeListener { _, _ ->
            deSelectAll()
            binding.switchMarkUnRead.isChecked = true
            if (isRightAction) {
                myPreferences?.swipeRightActionLabel = "swipeMarkUnRead"
            } else {
                myPreferences?.swipeLeftActionLabel = "swipeMarkUnRead"
            }
            callback.invoke(4)
            dismiss()
        }

        binding.switchNone.setOnCheckedChangeListener { _, _ ->
            deSelectAll()
            binding.switchNone.isChecked = true
            if (isRightAction) {
                myPreferences?.swipeRightActionLabel = "swipeNone"
            } else {
                myPreferences?.swipeLeftActionLabel = "swipeNone"
            }
            callback.invoke(5)
            dismiss()
        }*/


    }

    private fun setDefaultSelectedAction(isRightAction: Boolean) {
        deSelectAll()
        val defaultAction = if (isRightAction) {
            binding.tvTitle.text = getString(R.string.swipe_right)
            myPreferences.swipeRightActionLabel
        } else {
            binding.tvTitle.text = getString(R.string.swipe_left)
            myPreferences.swipeLeftActionLabel
        }

        when (defaultAction) {
            "swipeArchive" -> {
                binding.switchArchive.isChecked = true
            }

            "swipeDelete" -> {
                binding.switchDelete.isChecked = true
            }

            "swipeMarkRead" -> {
                binding.switchMarkRead.isChecked = true
            }

            "swipeMarkUnRead" -> {
                binding.switchMarkUnRead.isChecked = true
            }

            "swipeNone" -> {
                binding.switchNone.isChecked = true
            }
        }
    }

    private fun deSelectAll() {
        binding.switchArchive.isChecked = false
        binding.switchDelete.isChecked = false
        binding.switchMarkRead.isChecked = false
        binding.switchMarkUnRead.isChecked = false
        binding.switchNone.isChecked = false
    }

}