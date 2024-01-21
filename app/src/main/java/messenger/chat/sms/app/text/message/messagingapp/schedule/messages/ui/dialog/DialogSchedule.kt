package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogScheduleBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Callback
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class DialogSchedule : BaseDialog<DialogScheduleBinding>() {


    override fun getViewBinding() = DialogScheduleBinding.inflate(layoutInflater)

    override fun init() {
        binding.btnToday.setOnClickListener(1000L) {
            EventBus.getDefault().post(Callback(1))
            dismiss()
        }
        binding.btnTonight.setOnClickListener(1000L) {
            EventBus.getDefault().post(Callback(2))
            dismiss()
        }
        binding.btnTomorrow.setOnClickListener(1000L) {
            EventBus.getDefault().post(Callback(3))
            dismiss()
        }
        binding.btnSelectDate.setOnClickListener(1000L) {
            EventBus.getDefault().post(Callback(4))
            dismiss()
        }
        binding.btnCancel.setOnClickListener(1000L) {
            dismiss()
        }
    }


}