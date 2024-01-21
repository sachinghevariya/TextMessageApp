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
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityArchivedBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.ArchivedAdapterNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.updateConversationArchivedStatus
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ConversationViewModel
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class ArchivedActivity : BaseActivity<ActivityArchivedBinding>() {

    private val viewModel: ConversationViewModel by viewModels()
    override fun getViewBinding() = ActivityArchivedBinding.inflate(layoutInflater)
    private lateinit var archivedAdapter: ArchivedAdapterNew
//    private var isAdShown = false
//    private val adModel = ConversationNew.getAdObject()

    override fun initData() {
        bannerAd()
        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }
        AppOpenManager.blockAppOpen(this)
        binding.tvNoData.isSelected = true
        setUpToolBar()
        setUpAdapter()
        setUpClickEvent()
        getRealmThread { realm ->
            val list = realm
                .where(ConversationNew::class.java)
                .notEqualTo("id", 0L)
                .equalTo("archived", true)
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .beginGroup()
                .isNotNull("lastMessage")
                .or()
                .isNotEmpty("draft")
                .endGroup().sort(
                    arrayOf("pinned", "draft", "lastMessage.date"),
                    arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
                ).findAllAsync()
            archivedAdapter.updateData(list)
        }
    }

    private fun setUpClickEvent() {
        binding.ivScrollUp.setOnClickListener(1000L) {
            scrollToTop()
            binding.ivScrollUp.gone()
        }
    }

    private fun scrollToTop() {
        val ll = binding.rvArchived.layoutManager as LinearLayoutManager
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

    private fun setUpToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = "${getString(R.string.archives)}"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setUpAdapter() {
        binding.rvArchived.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        archivedAdapter =
            ArchivedAdapterNew(
                this,
                myPreferences,
                phoneNumberUtils = PhoneNumberUtils(this)
            ) { item, deleteEvent, unArchiveEvent, selectedItems ->
                if (item != null) {
                    startActivity(
                        Intent(this, ConversationDetailActivityNew::class.java)
                            .putExtra(CommonClass.THREAD_ID, item.id)
                            .putExtra(CommonClass.THREAD_TITLE, item.getTitle())
                    )
                } else if (deleteEvent) {
                    val itemsCnt = selectedItems.size
                    val items = resources.getQuantityString(
                        R.plurals.delete_conversations,
                        itemsCnt,
                        itemsCnt
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
                                archivedAdapter.clearSelection()
                            }
                        }*/
                        deleteConversations(selectedItems)
                        archivedAdapter.clearSelection()
                    }
                } else if (unArchiveEvent) {
                    val baseString = R.string.unArchive_confirmation
                    val itemsCnt = selectedItems.size
                    val items = resources.getQuantityString(
                        R.plurals.delete_conversations,
                        itemsCnt,
                        itemsCnt
                    )
                    val question = String.format(resources.getString(baseString), items)
                    ConfirmationDialog.newInstance(
                        this,
                        question,
                        positive = getString(R.string.ok),
                        dialogTitle = getString(R.string.unArchive_msg)
                    ) {
                        /*CoroutineScope(Dispatchers.IO).launch {
                            unArchiveConversations(selectedItems)
                            withContext(Dispatchers.Main) {
                                archivedAdapter.clearSelection()
                            }
                        }*/
                        unArchiveConversations(selectedItems)
                        archivedAdapter.clearSelection()
                    }
                }
            }
        binding.rvArchived.adapter = archivedAdapter
        archivedAdapter.emptyView = binding.tvNoData

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
        binding.rvArchived.addOnScrollListener(scrollListener)
    }

    private fun unArchiveConversations(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            selectedItems.forEach {
                updateConversationArchivedStatus(it.threadId, true)
                viewModel.moveToUnArchive(it.threadId)
                notificationManager.cancel(it.threadId.toInt())
            }
        }*/
        selectedItems.forEach {
            updateConversationArchivedStatus(it.id, true)
            viewModel.moveToUnArchive(it.id)
            notificationManager.cancel(it.id.toInt())
        }
    }

    private fun deleteConversations(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            selectedItems.forEach {
                deleteConversation(it.threadId)
                viewModel.deleteConversationByThreadId(it.threadId)
                viewModel.deleteMessageByThreadId(it.threadId)
                notificationManager.cancel(it.threadId.toInt())
            }
        }*/
        selectedItems.forEach {
            deleteConversation(it.id)
            viewModel.deleteConversationByThreadId(it.id)
//                viewModel.deleteMessageByThreadId(it.threadId)
            notificationManager.cancel(it.id.toInt())
        }
    }

    override fun onBackPressed() {
        backPressed()
    }

}