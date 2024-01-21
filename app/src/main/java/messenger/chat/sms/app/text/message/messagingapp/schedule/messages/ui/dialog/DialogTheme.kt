package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.SharedPre

@AndroidEntryPoint
class DialogTheme : DialogFragment() {

    private lateinit var myPreferences: MyPreferences
    private var isThemeLight: Boolean = false
    private var isThemeDark: Boolean = false
    private var isThemeDefault: Boolean = false

    companion object {
        private const val THEME_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        private const val THEME_DARK = AppCompatDelegate.MODE_NIGHT_YES
        private const val THEME_SYSTEM_DEFAULT = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)
        dialog!!.setCancelable(false)
        return inflater.inflate(R.layout.dialog_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myPreferences = MyPreferences(requireContext())
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        val tvLight: ImageView = dialog!!.findViewById<ImageView>(R.id.img_light)
        val tvDark: ImageView = dialog!!.findViewById<ImageView>(R.id.img_dark)
        val tvDefault: ImageView = dialog!!.findViewById<ImageView>(R.id.img_default)
        val constLight: ConstraintLayout = dialog!!.findViewById(R.id.const_light)
        val constDark: ConstraintLayout = dialog!!.findViewById(R.id.const_dark)
        val constDefault: ConstraintLayout = dialog!!.findViewById(R.id.const_default)

        setTheme(tvLight, tvDark, tvDefault)
        constLight.setOnClickListener {
            isThemeLight = true
            tvLight.setImageResource(R.drawable.ic_check)
            tvDark.setImageResource(R.drawable.ic_lang_uncheck)
            tvDefault.setImageResource(R.drawable.ic_lang_uncheck)
        }
        constDark.setOnClickListener {
            isThemeDark = true
            tvLight.setImageResource(R.drawable.ic_lang_uncheck)
            tvDark.setImageResource(R.drawable.ic_check)
            tvDefault.setImageResource(R.drawable.ic_lang_uncheck)
        }
        constDefault.setOnClickListener {
            isThemeDefault = true
            tvLight.setImageResource(R.drawable.ic_lang_uncheck)
            tvDark.setImageResource(R.drawable.ic_lang_uncheck)
            tvDefault.setImageResource(R.drawable.ic_check)
        }
        val tvOk: AppCompatTextView = dialog!!.findViewById(R.id.tv_save)
        val tvCancel: AppCompatTextView = dialog!!.findViewById(R.id.tv_cancel)
        tvCancel.setOnClickListener {
            dismiss()
        }
        tvOk.setOnClickListener {
            dismiss()
            if (isThemeLight) {
                saveTheme(THEME_LIGHT)
                applyTheme(THEME_LIGHT)
            } else if (isThemeDark) {
                saveTheme(THEME_DARK)
                applyTheme(THEME_DARK)
            } else if (isThemeDefault) {
                saveTheme(THEME_SYSTEM_DEFAULT)
                applyTheme(THEME_SYSTEM_DEFAULT)
            }
        }
    }

    private fun setTheme(tvLight: ImageView, tvDark: ImageView, tvDefault: ImageView) {
        when (getSavedTheme()) {
            THEME_LIGHT -> {
                tvLight.setImageResource(R.drawable.ic_check)
                tvDark.setImageResource(R.drawable.ic_lang_uncheck)
                tvDefault.setImageResource(R.drawable.ic_lang_uncheck)
            }

            THEME_DARK -> {
                tvLight.setImageResource(R.drawable.ic_lang_uncheck)
                tvDark.setImageResource(R.drawable.ic_check)
                tvDefault.setImageResource(R.drawable.ic_lang_uncheck)
            }

            THEME_SYSTEM_DEFAULT -> {
                tvLight.setImageResource(R.drawable.ic_lang_uncheck)
                tvDark.setImageResource(R.drawable.ic_lang_uncheck)
                tvDefault.setImageResource(R.drawable.ic_check)
            }
        }
    }


    private fun saveTheme(theme: Int) {
        SharedPre.save(activity, "MyTheme", theme)
    }

    private fun getSavedTheme(): Int {
        return SharedPre.getInt(activity, "MyTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun applyTheme(theme: Int) {
        when (theme) {
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                dialog?.dismiss()
            }

            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                dialog?.dismiss()
            }

            THEME_SYSTEM_DEFAULT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                dialog?.dismiss()
            }
        }
    }
}