package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.realm.RealmList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityNewConversationBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.ContactsAdapterNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.DialogSelectDestination
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackString
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateContacts
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.IS_SCHEDULE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.hideKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isShortCode
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showCustomDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.NewConversationViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager
import java.io.Serializable

@AndroidEntryPoint
class NewConversationActivity : BaseActivity<ActivityNewConversationBinding>() {

    override fun getViewBinding() = ActivityNewConversationBinding.inflate(layoutInflater)

    private lateinit var contactsAdapter: ContactsAdapterNew
    private var isDialKeyboard = true
    private var isScheduled = false
    var number = ""
    private val viewModel: NewConversationViewModel by viewModels()
    private var searchList = arrayListOf<Contact>()
    private var newNumberList = arrayListOf<Contact>()
    private var isAdShown = false
    private var contact: Contact? = null
    private val DIALOG_SHOWN_KEY = "dialog_shown"
    private val SEARCH_LIST_KEY = "search_list"

    override fun initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        bannerAd()
        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }
        AppOpenManager.blockAppOpen(this)
        binding.search.isSelected = true
        setUpToolBar()
        setupAdapter()
        viewClick()
        getIntentData()
        getUnmanagedContacts()
    }

    private fun getUnmanagedContacts() {
        getRealmThread { realm ->
            val mobileOnly = true
            val mobileLabel by lazy {
                ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    resources,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                    "Mobile"
                ).toString()
            }

            val query = realm.where(Contact::class.java)

//            if (mobileOnly) {
//                query = query.contains("numbers.type", mobileLabel)
//            }
            val list = query
                .findAllAsync().map { realm.copyFromRealm(it) }
            searchList.clear()
            searchList.addAll(list)
            contactsAdapter.updateData(list as ArrayList<Contact>)
        }
    }

    private fun getIntentData() {
        isScheduled = intent.getBooleanExtra(IS_SCHEDULE, false)
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()

        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
    }

    private fun viewClick() {
        manageKeyboard()
        binding.ivDial.setOnClickListener(300L) {
            manageKeyboard()
        }
        binding.ivKeyboard.setOnClickListener(300L) {
            manageKeyboard()
        }
        binding.cancel.setOnClickListener(300L) {
            number = ""
            binding.search.setText("")
            binding.cancel.gone()
        }
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().length > 1) {
                    binding.cancel.visible()
                    number = s.toString().trim()
                    searchContacts(number)
                } else {
                    binding.cancel.gone()
                    contactsAdapter.updateData(searchList)
                }
            }
        })
    }

    private fun manageKeyboard() {
        isDialKeyboard = !isDialKeyboard
        if (!isDialKeyboard) {
            binding.search.inputType = InputType.TYPE_CLASS_TEXT
            binding.ivDial.visible()
            binding.ivKeyboard.gone()
        } else {
            binding.search.inputType = InputType.TYPE_CLASS_PHONE
            binding.ivDial.gone()
            binding.ivKeyboard.visible()
        }

        binding.search.showKeyboard()

    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = "${getString(R.string.new_conversation)}"
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAdapter() {
        binding.rvContacts.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        contactsAdapter = ContactsAdapterNew(arrayListOf(), this, myPreferences) { item ->
            number = item.name
            onContactClick(item)
        }
        binding.rvContacts.adapter = contactsAdapter
    }


    private fun onContactClick(contact: Contact) {
        val phoneNumbers = arrayListOf<String>()
        contact.numbers.forEach {
            phoneNumbers.add(it.address)
        }
        if (phoneNumbers.size > 1) {
            /*val primaryNumber = contact.phoneNumbers.find { it.isPrimary }
            if (primaryNumber != null) {
                launchThreadActivity(primaryNumber.value, contact.name)
            } else {
                val items = ArrayList<RadioItem>()
                phoneNumbers.forEachIndexed { index, phoneNumber ->
                    val type = getPhoneNumberTypeText(phoneNumber.type, phoneNumber.label)
                    items.add(RadioItem(index, "${phoneNumber.normalizedNumber} ($type)", phoneNumber.normalizedNumber))
                }

                RadioGroupDialog(this, items) {
                    launchThreadActivity(it as String, contact.name)
                }
            }*/
            this.contact = contact
            val dialog = DialogSelectDestination.newInstance()
            val args = Bundle()
            args.putSerializable("KEY_LIST", phoneNumbers as? Serializable)
            dialog.arguments = args
            showCustomDialog(this, dialog)
        } else {
            if (searchList.contains(contact)) {
                launchThreadActivity(contact.numbers.first()?.address!!, contact.name)
                return
            }
            if (isShortCode(number)) {
                binding.search.setText("")
                showToast(getString(R.string.invalid_short_code))
                return
            }
            launchThreadActivity(contact.numbers.first()?.address!!, contact.name)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallbackString(event: CallbackString) {
        contact?.let {
            gotoNext(it, event.number)
        }
    }

    private fun gotoNext(contact: Contact, number: String) {
        if (searchList.contains(contact)) {
            launchThreadActivity(number, contact.name)
        } else if (isShortCode(number)) {
            binding.search.setText("")
            showToast(getString(R.string.invalid_short_code))
        } else {
            launchThreadActivity(number, contact.name)
        }
    }


    private fun launchThreadActivity(phoneNumber: String, name: String) {
        binding.search.hideKeyboard()
        val numbers = phoneNumber.split(";").toSet()
        val number = if (numbers.size == 1) phoneNumber else Gson().toJson(numbers)
        Log.e("TAG", "onContactClick: "+number)

        Intent(this, ConversationDetailActivityNew::class.java).apply {
            putExtra(THREAD_ID, getThreadId(numbers))
            putExtra(THREAD_TITLE, name)
            putExtra(THREAD_NUMBER, number)
            putExtra(IS_SCHEDULE, isScheduled)
            startActivity(this)
            if (isScheduled) {
                finish()
            }
        }
    }

    fun searchContacts(query: String) {
        val filteredList = searchList.filter { contact ->
            val nameMatches = contact.name.contains(query, ignoreCase = true)
            val phoneNumberMatches = contact.numbers.any { phoneNumber ->
                phoneNumber.address.contains(query, ignoreCase = true)
            }
            nameMatches || phoneNumberMatches
        }

        if (filteredList.isEmpty()) {
            if (query.length > 6) {
                if(query.all { it.isDigit() }){
                    addSingleEntry()
                }else{
                    contactsAdapter.updateData(arrayListOf())
                }
            } else {
                contactsAdapter.updateData(filteredList as ArrayList<Contact>)
            }
        } else {
            contactsAdapter.updateData(filteredList as ArrayList<Contact>)
        }
    }

    private fun addSingleEntry() {
        val simpleContact = Contact(numbers = RealmList(PhoneNumber(address = number)), name = number)
        newNumberList = arrayListOf()
        newNumberList.add(simpleContact)
        contactsAdapter.updateData(newNumberList)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateContacts(event: UpdateContacts){
        getUnmanagedContacts()
    }

}