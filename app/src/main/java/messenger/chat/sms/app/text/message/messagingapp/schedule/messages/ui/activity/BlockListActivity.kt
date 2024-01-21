package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityBlockListBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteBlockedNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationAdapterNewNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizePhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ConversationViewModel
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class BlockListActivity : BaseActivity<ActivityBlockListBinding>() {

    private lateinit var conversationAdapter: ConversationAdapterNewNew
    private val viewModel: ConversationViewModel by viewModels()
    private val adModel = Conversation.getAdObject()

    override fun getViewBinding() = ActivityBlockListBinding.inflate(layoutInflater)

    override fun initData() {
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }
        setUpToolBar()
        setupAdapter()
        observeLiveData()
        setUpClickEvent()
    }

    private fun setUpClickEvent() {
        binding.ivScrollUp.setOnClickListener(1000L) {
            scrollToTop()
            binding.ivScrollUp.gone()
        }
    }

    private fun observeLiveData() {
//        lifecycleScope.launch {
//            viewModel.getAllBlockedConversationsLiveData()
//                .observe(this@BlockListActivity) { conversations ->
//                    val newList = arrayListOf<Conversation>()
//                    if (AdsUtility.isNetworkConnected(this@BlockListActivity)) {
//                        if (conversations.isNotEmpty() && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
//                            binding.tvNoData.gone()
////                            newList.add(0, adModel)
//                            newList.addAll(conversations)
//                            conversationAdapter.submitList(newList) {
//                                binding.rvConversations.smoothScrollToPosition(0)
//                            }
//                        } else {
//                            if (conversations.isEmpty()) {
//                                binding.tvNoData.visible()
//                            } else {
//                                binding.tvNoData.gone()
//                            }
//                            newList.addAll(conversations)
//                            conversationAdapter.submitList(newList) { }
//                        }
//                    } else {
//                        if (conversations.isNotEmpty()) {
//                            binding.tvNoData.gone()
//                            newList.addAll(conversations)
//                            conversationAdapter.submitList(newList) {
//                                binding.rvConversations.smoothScrollToPosition(0)
//                            }
//                        } else {
//                            binding.tvNoData.visible()
//                            binding.ivScrollUp.gone()
//                            conversationAdapter.submitList(arrayListOf()) { }
//                        }
//                    }
//                }
//        }

        getRealmThread { realm ->
            val list = realm
                .where(ConversationNew::class.java)
                .notEqualTo("id", 0L)
                .equalTo("archived", false)
                .equalTo("blocked", true)
                .isNotEmpty("recipients")
                .beginGroup()
                .isNotNull("lastMessage")
                .or()
                .isNotEmpty("draft")
                .endGroup().sort(
                    arrayOf("pinned", "draft", "lastMessage.date"),
                    arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
                ).findAllAsync()
            conversationAdapter.updateData(list)
        }
    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = getString(R.string.block_list)
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAdapter() {
        binding.rvConversations.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        conversationAdapter = ConversationAdapterNewNew(
            this,
            myPreferences,
            true,
            phoneNumberUtils = PhoneNumberUtils(this),
            actionCallback = {}
        ) { item, deleteEvent, _, _, _, _, _, _, unblockEvent, selectedItems ->
            if (item != null) {
                startActivity(
                    Intent(this, ConversationDetailActivityNew::class.java)
                        .putExtra(CommonClass.THREAD_ID, item.id)
                        .putExtra(CommonClass.THREAD_TITLE, item.getTitle())
                        .putExtra(CommonClass.IS_BLOCKED, true)
                )
            } else if (deleteEvent) {
                val itemsCnt = selectedItems.size
                val items = resources.getQuantityString(
                    R.plurals.delete_conversations, itemsCnt, itemsCnt
                )
                val baseString = R.string.deletion_confirmation
                val question = String.format(resources.getString(baseString), items)
                ConfirmationDialog.newInstance(
                    this,
                    question,
                    positive = getString(R.string.delete),
                    dialogTitle = getString(R.string.delete_msg)
                ) {
                    /*CoroutineScope(Dispatchers.IO).launch {
                        deleteConversations(selectedItems)
                        withContext(Dispatchers.Main) {
                            conversationAdapter.clearSelection()
                        }
                    }*/
                    deleteConversations(selectedItems)
                    conversationAdapter.clearSelection()
                }
            } else if (unblockEvent) {
                /*CoroutineScope(Dispatchers.IO).launch {
                    unblockConversation(selectedItems)
                    withContext(Dispatchers.Main) {
                        conversationAdapter.clearSelection()
                    }
                }*/
                unblockConversation(selectedItems)
                conversationAdapter.clearSelection()
            }
        }
        binding.rvConversations.adapter = conversationAdapter
        conversationAdapter.emptyView = binding.tvNoData
        val scrollListener = MyScrollListener(object : OnScrollListener {
            override fun onScrolledUp() {}

            override fun onScrolledDown() {
                binding.ivScrollUp.visible()
            }

            override fun onReachedBottom() {}

            override fun onReachedTop() {
                binding.ivScrollUp.gone()
            }
        })
        binding.rvConversations.addOnScrollListener(scrollListener)

    }

    private fun deleteConversations(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            selectedItems.forEach {
                deleteConversation(it.id)
                viewModel.deleteConversationByThreadId(it.id)
                viewModel.deleteMessageByThreadId(it.id)
                notificationManager.cancel(it.id.toInt())
            }
        }*/
        selectedItems.forEach {
            deleteConversation(it.id)
            viewModel.deleteConversationByThreadId(it.id)
            notificationManager.cancel(it.id.toInt())
        }
    }

    private fun unblockConversation(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        /* CoroutineScope(Dispatchers.IO).launch {
             selectedItems.forEach {
                 it.lastMessage?.address?.let { it1 -> deleteBlockedNumber(it1) }
                 viewModel.unblockConversation(it.id)
             }
         }*/
        selectedItems.forEach {
            it.lastMessage?.address?.let { it1 -> deleteBlockedNumber(it1) }
            viewModel.unblockConversation(it.id)
        }
    }

    private fun scrollToTop() {
        val ll = binding.rvConversations.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(0, 0)
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).visible()

        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
        }
    }

    override fun onBackPressed() {
        backPressed()
    }

}