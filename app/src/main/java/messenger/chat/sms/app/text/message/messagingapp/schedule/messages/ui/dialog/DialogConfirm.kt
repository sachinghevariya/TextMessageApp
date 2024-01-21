package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogConfirmBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseDialog

@AndroidEntryPoint
class DialogConfirm : BaseDialog<DialogConfirmBinding>() {
    private lateinit var message: String
    private lateinit var positive: String
    private var cancelOnTouchOutside = false
    private lateinit var dialogTitle: String
    private lateinit var callback: () -> Unit
    fun setCallback(
        message: String,
        positive: String,
        cancelOnTouchOutside: Boolean,
        dialogTitle: String,
        callback: () -> Unit
    ) {
        this.message = message
        this.positive = positive
        this.cancelOnTouchOutside = cancelOnTouchOutside
        this.dialogTitle = dialogTitle
        this.callback = callback
    }

    override fun getViewBinding() = DialogConfirmBinding.inflate(layoutInflater)

    override fun init() {
        binding.message.text = message
        binding.title.text = dialogTitle
        binding.btnOkay.text = positive
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnOkay.setOnClickListener {
            dismiss()
            callback.invoke()
        }
    }


}