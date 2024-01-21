package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<B : ViewBinding> : DialogFragment() {

    protected lateinit var binding: B

    abstract fun getViewBinding(): B
    abstract fun init()

    override fun onCreateDialog(@Nullable savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
//        val window = dialog.window
//        window?.setWindowAnimations(R.style.DialogAnimation)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Make the dialog background transparent
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Calculate the dialog width as 80% of the screen width
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val dialogWidth = (screenWidth * 0.85).toInt()

            // Set the dialog width and height
            setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

            // You can also adjust the gravity if needed, for example:
            setGravity(Gravity.CENTER)
        }

        isCancelable = false
    }

}