package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogScheduleOptionBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackSchedule
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class DialogScheduleOption : BaseDialog<DialogScheduleOptionBinding>() {

    override fun getViewBinding() = DialogScheduleOptionBinding.inflate(layoutInflater)

    override fun init() {
        binding.btnSendNow.setOnClickListener(1000L) {
            dismiss()
            EventBus.getDefault().post(CallbackSchedule(1))
        }
        binding.btnCopy.setOnClickListener(1000L) {
            dismiss()
            EventBus.getDefault().post(CallbackSchedule(2))
        }
        binding.btnDelete.setOnClickListener(1000L) {
            dismiss()
            EventBus.getDefault().post(CallbackSchedule(3))
        }
        binding.btnCancel.setOnClickListener(1000L) {
            dismiss()
        }
    }

}