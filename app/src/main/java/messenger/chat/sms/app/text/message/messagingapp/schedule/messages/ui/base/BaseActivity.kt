package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.viewbinding.ViewBinding
import com.google.android.material.internal.ContextUtils
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.localehelper.LocaleHelper
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.localehelper.LocaleHelperActivityDelegate
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.localehelper.LocaleHelperActivityDelegateImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import org.greenrobot.eventbus.EventBus
import plugin.adsdk.service.BaseActivity
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity<B : ViewBinding> : BaseActivity() {

    lateinit var binding: B

    @Inject
    lateinit var myPreferences: MyPreferences
    abstract fun getViewBinding(): B

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        var newUiOptions = decorView.systemUiVisibility
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = newUiOptions
        if (SDK_INT >= Build.VERSION_CODES.N) {
            window.statusBarColor = Window.DECOR_CAPTION_SHADE_AUTO
        }
        super.onCreate(savedInstanceState)
//        localeDelegate.initialise(this,this)
        localeDelegate.onCreate(this)
        binding = getViewBinding()
        setContentView(binding.root)
        initData()
        setupNetworkChangeReceiver()
    }

    /* @Subscribe(threadMode = ThreadMode.MAIN)
     fun onUpdateLanguage(event: UpdateLanguage) {
         if(this is SelectLanguageActivity){

         }else {
             Log.e("TAG", "onUpdateLanguage:  Base")
             updateLocale(event.locale)
         }
     }

     override fun onStart() {
         super.onStart()
         if (!EventBus.getDefault().isRegistered(this)) {
             EventBus.getDefault().register(this)
         }
     }*/

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    val localeDelegate: LocaleHelperActivityDelegate = LocaleHelperActivityDelegateImpl()

    override fun getDelegate() = localeDelegate.getAppCompatDelegate(super.getDelegate())

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localeDelegate.attachBaseContext(newBase))
    }

    override fun onResume() {
        super.onResume()
        localeDelegate.onResumed(this)
    }

    override fun onPause() {
        super.onPause()
        localeDelegate.onPaused()
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val context = super.createConfigurationContext(overrideConfiguration)
        return LocaleHelper.onAttach(context)
    }

    override fun getApplicationContext(): Context =
        localeDelegate.getApplicationContext(super.getApplicationContext())

    open fun updateLocale(locale: Locale) {
        localeDelegate.setLocale(this, locale)
    }

    var appOpenBlockLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    private val REQUEST_CODE_PERMISSION = 3256

    open fun permissionsNotification(): Array<String>? {
        val p: Array<String> = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission33Notification
        } else {
            arrayOf()
        }
        return p
    }

    fun permissionsStorage(): Array<String> {
        val p: Array<String> = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission33
        } else {
            permissionAll
        }
        return p
    }

    private var permissionQ = arrayOf<String>(
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )


    private var permissionAll = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private var permission33 = arrayOf<String>(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private var permission33Notification = arrayOf<String>(
        Manifest.permission.POST_NOTIFICATIONS
    )

    open fun checkRunTimePermission(
        vararg permissionArrays: String?,
        listener: () -> Unit
    ) {
        if (hasPermissions(*permissionArrays)) {
            listener.invoke()
        } else {
            Dexter.withContext(this).withPermissions(*permissionArrays)
                .withListener(CompositeMultiplePermissionsListener(object :
                    MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            listener.invoke()
                        } else {
                            showPermissionSnack()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        list: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        permissionToken: PermissionToken?
                    ) {
                        permissionToken?.continuePermissionRequest()
                    }

                })).onSameThread().check()
        }
    }

    /*    open fun hasPermissions(vararg permissions: String?): Boolean {
            if (permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            this, permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }*/

    open fun showPermissionSnack() {
        val message = "All those permissions are needed for working all functionality"
        val snackbar = Snackbar.make(
            findViewById<View>(android.R.id.content),
            message,
            BaseTransientBottomBar.LENGTH_INDEFINITE
        ).setAction("Settings") { _: View? -> openSettingsDialog() }
        snackbar.setActionTextColor(Color.RED)
        val sbView = snackbar.view
        val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.YELLOW)
        snackbar.show()
    }

    open fun openSettingsDialog() {
        val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
        val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
        val EXTRA_SYSTEM_ALERT_WINDOW = "permission_settings"
        val bundle = Bundle()
        bundle.putString(EXTRA_FRAGMENT_ARG_KEY, EXTRA_SYSTEM_ALERT_WINDOW)
        val uri = Uri.fromParts("package", packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri)
            .putExtra(EXTRA_FRAGMENT_ARG_KEY, EXTRA_SYSTEM_ALERT_WINDOW)
            .putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
        startActivityForResult(intent, REQUEST_CODE_PERMISSION)
    }

}