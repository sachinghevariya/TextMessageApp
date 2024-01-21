package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityContactDetailBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.CONTACT_LIST
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ContactViewModel

@AndroidEntryPoint
class ContactDetailActivity : BaseActivity<ActivityContactDetailBinding>() {

    private val viewModel: ContactViewModel by viewModels()
    private var threadId: Long = -1
    private var threadTitle: String = ""
    private var contactList: List<SimpleContact> = arrayListOf()

    override fun getViewBinding() = ActivityContactDetailBinding.inflate(layoutInflater)

    override fun initData() {
        getIntentData()
        setUpToolBar()
        val contact = contactList[0]
        loadContactImage(
            this@ContactDetailActivity,
            contact.photoUri,
            binding.ivPhoto,
            contact.name,
            null,
            myPreferences.showProfilePhoto
        )
        binding.threadTitle.text = contact.name
    }

    private fun getIntentData() {
        threadId = intent.getLongExtra(THREAD_ID, -1)
        threadTitle = intent.getStringExtra(THREAD_TITLE)!!
        contactList = intent.getSerializableExtra(CONTACT_LIST) as List<SimpleContact>

    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = threadTitle
        binding.threadToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

}