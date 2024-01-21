package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogConfirmBinding

class ConfirmationDialog() {
    private var dialog: Dialog? = null

    private lateinit var activity: Activity
    private lateinit var message: String
    private lateinit var positive: String
    private var cancelOnTouchOutside = false
    private lateinit var dialogTitle: String
    private lateinit var callback: () -> Unit

    companion object {
        fun newInstance(
            activity: Activity,
            message: String = "",
            positive: String = "",
            cancelOnTouchOutside: Boolean = true,
            dialogTitle: String = "",
            callback: () -> Unit
        ): ConfirmationDialog {
            return ConfirmationDialog(
                activity,
                message,
                positive,
                cancelOnTouchOutside,
                dialogTitle,
                callback
            )
        }
    }

    constructor(
        activity: Activity,
        message: String = "",
        positive: String = "",
        cancelOnTouchOutside: Boolean = true,
        dialogTitle: String = "",
        callback: () -> Unit
    ) : this() {
        this.activity = activity
        this.message = message
        this.positive = positive
        this.cancelOnTouchOutside = cancelOnTouchOutside
        this.dialogTitle = dialogTitle
        this.callback = callback
        val view = DialogConfirmBinding.inflate(activity.layoutInflater, null, false)
        view.message.text = message
        view.title.text = dialogTitle
        view.btnOkay.text = positive
        dialog = Dialog(activity)
        dialog?.setContentView(view.root)
        dialog?.setCancelable(cancelOnTouchOutside)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.btnCancel.setOnClickListener { dialog?.dismiss() }
        view.btnOkay.setOnClickListener { dialogConfirmed() }
        dialog?.show()
    }


    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }

}