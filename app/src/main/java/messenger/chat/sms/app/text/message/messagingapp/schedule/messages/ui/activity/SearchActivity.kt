package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Case
import io.realm.Realm
import io.realm.Sort
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivitySearchBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.PeopleAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.SearchAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateContacts
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ContactFilter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationFilter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberFilter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.RecipientFilter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.SearchResult
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.fadeIn
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.hideKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.onTextChangeListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.removeAccents
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    private var lastSearchedText = ""

    private lateinit var adapter: SearchAdapter
    private lateinit var peopleAdapter: PeopleAdapter

    override fun getViewBinding() = ActivitySearchBinding.inflate(layoutInflater)

    override fun initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        setUpToolBar()
        manageMarquee()
        setUpAdapter()
        setupPeopleAdapter()
        binding.search.onTextChangeListener { text ->
            if (text.isNotEmpty()) {
                if (binding.searchHolder.alpha < 1f) {
                    binding.searchHolder.fadeIn()
                }
            } else {
                fadeOutSearch()
            }
            searchTextChanged(text)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
//        getRealmThread { realm ->
//            val mobileOnly = true
//            val mobileLabel by lazy {
//                ContactsContract.CommonDataKinds.Phone.getTypeLabel(
//                    resources,
//                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
//                    "Mobile"
//                ).toString()
//            }
//
//            var query = realm.where(Contact::class.java)
//
//            if (mobileOnly) {
//                query = query.contains("numbers.type", mobileLabel)
//            }
//            val list = query
//                .findAllAsync().map { realm.copyFromRealm(it) }
//            peopleAdapter.submitList(list)
//        }


        getRealmThread { realm ->
            val list = realm.where(Contact::class.java)
                .findAllAsync()
                .map { realm.copyFromRealm(it) }


            peopleAdapter.submitList(list)
        }
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()
        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
    }

    private fun manageMarquee() {
        binding.search.isSelected = true
        binding.tvPeople.isSelected = true
    }

    private fun setUpToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = "${getString(R.string.search_conversation)}"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun fadeOutSearch() {
        binding.searchHolder.animate().alpha(0f).setDuration(150L).withEndAction {
            searchTextChanged("")
        }.start()
    }

    private fun searchTextChanged(text: String) {
        lastSearchedText = text
        if (text.length >= 2) {
            binding.searchResultsList.visible()
            val searchResults = searchConversations(text)
            adapter.submitList(searchResults)
        } else {
            binding.searchResultsList.gone()
        }
    }

    private fun searchConversations(query: CharSequence): List<SearchResult> {
        val realm = Realm.getDefaultInstance()
        val phoneNumberFilter = PhoneNumberFilter(
            PhoneNumberUtils(this)
        )
        val conversationFilter = ConversationFilter(
            RecipientFilter(
                ContactFilter(
                    phoneNumberFilter
                ),
                phoneNumberFilter
            )
        )

        val normalizedQuery = query.removeAccents()
        val conversations = realm.copyFromRealm(
            realm
                .where(ConversationNew::class.java)
                .notEqualTo("id", 0L)
                .isNotNull("lastMessage")
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("pinned", Sort.DESCENDING, "lastMessage.date", Sort.DESCENDING)
                .findAll()
        )

        val messagesByConversation = realm.copyFromRealm(
            realm
                .where(MessageNew::class.java)
                .beginGroup()
                .contains("body", normalizedQuery, Case.INSENSITIVE)
//                .or()
//                .contains("parts.text", normalizedQuery, Case.INSENSITIVE)
                .endGroup()
                .findAll()
        )
            .asSequence()
            .groupBy { message -> message.threadId }
            .filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
            .map { (threadId, messages) ->
                Pair(
                    conversations.first { it.id == threadId },
                    messages.size
                )
            }
            .map { (conversation, messages) ->
                SearchResult(
                    normalizedQuery,
                    conversation,
                    messages
                )
            }
            .sortedByDescending { result -> result.messages }
            .toList()

        realm.close()

        return conversations
            .filter { conversation -> conversationFilter.filter(conversation, normalizedQuery) }
            .map { conversation ->
                SearchResult(
                    normalizedQuery,
                    conversation,
                    0
                )
            } + messagesByConversation
    }

    private fun setUpAdapter() {
        binding.search.showKeyboard()
        adapter = SearchAdapter(this, myPreferences) {
            binding.search.hideKeyboard()
            Intent(this, ConversationDetailActivityNew::class.java).apply {
                putExtra(THREAD_ID, it.conversation.id)
                putExtra(THREAD_TITLE, it.conversation.getTitle())
                startActivity(this)
            }
        }
        binding.searchResultsList.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.searchResultsList.adapter = adapter
    }

    private fun setupPeopleAdapter() {
        binding.rvPeople.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        peopleAdapter = PeopleAdapter(this, myPreferences) { item ->
            onContactClick(item)
        }
        binding.rvPeople.adapter = peopleAdapter
    }

    private fun onContactClick(contact: Contact) {
        binding.search.hideKeyboard()
        val phoneNumbers = contact.numbers.first()?.address!!
        launchThreadActivity(phoneNumbers, contact.name)
    }

    private fun launchThreadActivity(phoneNumber: String, name: String) {
        val numbers = phoneNumber.split(";").toSet()
        val number = if (numbers.size == 1) phoneNumber else Gson().toJson(numbers)
        Intent(this, ConversationDetailActivityNew::class.java).apply {
            putExtra(THREAD_ID, getThreadId(numbers))
            putExtra(THREAD_TITLE, name)
            putExtra(THREAD_NUMBER, number)
            startActivity(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateContacts(event: UpdateContacts){
        getRealmThread { realm ->
            val list = realm.where(Contact::class.java)
                .findAllAsync()
                .map { realm.copyFromRealm(it) }


            peopleAdapter.submitList(list)
        }
    }

}