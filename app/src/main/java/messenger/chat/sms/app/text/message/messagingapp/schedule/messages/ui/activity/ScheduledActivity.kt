package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityScheduledBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.DialogScheduleOption
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.cancelScheduleSendPendingIntent
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.sendMessageCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackSchedule
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToConversationImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToRecipientImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ScheduleAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ScheduledMessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TelephonyCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.anyOf
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.tryOrNull
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.copyToClipboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showCustomDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledActivity : BaseActivity<ActivityScheduledBinding>() {
    override fun getViewBinding() = ActivityScheduledBinding.inflate(layoutInflater)
    private lateinit var conversationAdapter: ScheduleAdapter

    //    private val viewModel: ConversationViewModel by viewModels()
    private var conversationList = arrayListOf<ConversationNew>()


    private lateinit var conversation: ConversationNew


    private val CONVERSATION_KEY = "conversation_key"

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        try {
//            outState.putSerializable(CONVERSATION_KEY, conversation)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        try {
//            conversation = savedInstanceState.getSerializable(CONVERSATION_KEY) as ConversationNew
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    override fun initData() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        bannerAd()
        AppOpenManager.blockAppOpen(this)
        manageMarquee()
        setUpToolBar()
        viewClick()
        setupAdapter()
    }

    override fun onResume() {
        super.onResume()
        observeLiveData()
    }

    private fun manageMarquee() {
        binding.tvChat.isSelected = true
    }

    private fun setupAdapter() {
        binding.rvScheduled.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        conversationAdapter = ScheduleAdapter(
            this,
            myPreferences,
            isFromSchedule = true,
            phoneNumberUtils = PhoneNumberUtils(this),
            actionCallback = {}
        ) { item, _, _, _, _, _, _, _, _, _ ->
            if (item != null) {
                conversation = item
                showScheduleOptionDialog()
            }
        }
        binding.rvScheduled.adapter = conversationAdapter
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
        binding.rvScheduled.addOnScrollListener(scrollListener)
    }

    private fun showScheduleOptionDialog() {
        val dialog = DialogScheduleOption()
        showCustomDialog(this, dialog)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallbackSchedule(event: CallbackSchedule) {
        when (event.int) {
            1 -> {
                sendNowMessage()
            }

            2 -> {
                copyMessages()
            }

            3 -> {
                deleteScheduleMessage()
            }
        }
    }


    private fun deleteScheduleMessage() {
        CoroutineScope(Dispatchers.IO).launch {
//            viewModel.deleteScheduleMessagesById(conversation.msgId)
            cancelScheduleSendPendingIntent(conversation.lastMessage?.id!!)
        }
        getRealmThread { realm ->
            val messagesList = realm.where(ScheduledMessageNew::class.java)
                .equalTo("id", conversation.lastMessage?.id).findFirst()
            realm.executeTransaction { messagesList?.deleteFromRealm() }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            observeLiveData()
        }, 200)
    }

    private fun sendNowMessage() {
//        val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
//        val addresses = listOf(conversation.lastMessage?.address!!)
        try {

            getRealmThread { realm ->
                val messagesList = realm.where(ScheduledMessageNew::class.java)
                    .equalTo("id", conversation.lastMessage?.id).findAll().map {
                        realm.copyFromRealm(it)
                    }

                messagesList.forEach { message ->
                    val addresses = message.recipients
                    try {
                        Handler(Looper.getMainLooper()).post {
                            val msg = insertSentSms(
                                this,
                                message.subId,
                                message.threadId,
                                message.recipients[0]!!,
                                message.body,
                                System.currentTimeMillis()
                            )
                            val messagingUtils = MessagingUtils(this)
                            sendMessageCompat(
                                message.body,
                                addresses,
                                message.subId,
                                msg.contentId,
                                messagingUtils
                            )
                        }

                        Handler(Looper.getMainLooper()).postDelayed({
                            getOrCreateConversation(this, message.recipients)?.let {
                                updateConversations(message.threadId)
                            }
                            deleteScheduleMessage()
                        }, 1000)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } catch (e: Error) {
                        e.printStackTrace()
                    }
                }
            }


            /*Handler(Looper.getMainLooper()).post {
                val messagingUtils = MessagingUtils(this)
                sendMessageCompat(
                    conversation.lastMessage?.body!!,
                    addresses,
                    subscriptionId,
                    null,
                    messagingUtils
                )
            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(1000)
                viewModel.deleteScheduleMessagesById(conversation.msgId)
                val messageSent = getMessages(conversation.threadId, limit = 1)
                if (messageSent != null && messageSent.isNotEmpty()) {
                    viewModel.updateMessages(messageSent[0])
                    val conversation = viewModel.getConversationByThreadId(conversation.threadId)
                    conversation.isScheduled = false
                    conversation.msgId = messageSent[0].id
                    conversation.snippet = messageSent[0].body
                    conversation.read = messageSent[0].read
                    conversation.date = messageSent[0].date
                    viewModel.updateConversation(conversation)
                }
            }*/

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    private fun insertSentSms(
        context: Context,
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): MessageNew {
//        val messageIds = KeyManagerImpl()
        // Insert the message to Realm
        val message = MessageNew().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.subId = subId

            id = KeyManagerImpl.newInstance().newId()
            boxId = Telephony.Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: MessageNew? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        // Insert the message to the native content provider
        val values = contentValuesOf(
            Telephony.Sms.ADDRESS to address,
            Telephony.Sms.BODY to body,
            Telephony.Sms.DATE to System.currentTimeMillis(),
            Telephony.Sms.READ to true,
            Telephony.Sms.SEEN to true,
            Telephony.Sms.TYPE to Telephony.Sms.MESSAGE_TYPE_OUTBOX,
            Telephony.Sms.THREAD_ID to threadId
        )

//        if (prefs.canUseSubId.get()) {
//            values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
//        }

        val uri = context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)

        // Update the contentId after the message has been inserted to the content provider
        // The message might have been deleted by now, so only proceed if it's valid
        //
        // We do this after inserting the message because it might be slow, and we want the message
        // to be inserted into Realm immediately. We don't need to do this after receiving one
//        Log.e("TAG", "insertSentSms: ${uri}")
        uri?.lastPathSegment?.toLong()?.let { id ->
            realm.executeTransaction {
                managedMessage?.takeIf { it.isValid }?.contentId = id
                message.contentId = id
            }
        }
        realm.close()

        // On some devices, we can't obtain a threadId until after the first message is sent in a
        // conversation. In this case, we need to update the message's threadId after it gets added
        // to the native ContentProvider
//        if (threadId == 0L) {
//            uri?.let(syncRepository::syncMessage)
//        }

        return message
    }

    private fun updateConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            threadIds.forEach { threadId ->
                val conversation = realm
                    .where(ConversationNew::class.java)
                    .equalTo("id", threadId)
                    .findFirst() ?: return

                val message = realm
                    .where(MessageNew::class.java)
                    .equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING)
                    .findFirst()

                realm.executeTransaction {
                    conversation.lastMessage = message
                }
            }
        }
    }

    private fun getOrCreateConversation(
        context: Context,
        addresses: List<String>
    ): ConversationNew? {
        if (addresses.isEmpty()) {
            return null
        }

        return (getThreadId(context, addresses) ?: tryOrNull {
            TelephonyCompat.getOrCreateThreadId(
                context,
                addresses.toSet()
            )
        })
            ?.takeIf { threadId -> threadId != 0L }
            ?.let { threadId ->
                getConversation(threadId)
                    ?.let(Realm.getDefaultInstance()::copyFromRealm)
                    ?: getConversationFromCp(context, threadId)
            }
    }

    private fun getConversationFromCp(context: Context, threadId: Long): ConversationNew? {
        val cursorToConversation = CursorToConversationImpl(context)
        val cursorToRecipient = CursorToRecipientImpl(context)
        val phoneNumberUtils = PhoneNumberUtils(context)

        return cursorToConversation.getConversationsCursor()
            ?.map(cursorToConversation::map)
            ?.firstOrNull { it.id == threadId }
            ?.let { conversation ->
                val realm = Realm.getDefaultInstance()
                val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())
                val lastMessage = realm.where(MessageNew::class.java).equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING).findFirst()?.let(realm::copyFromRealm)

                val recipients = conversation.recipients
                    .map { recipient -> recipient.id }
                    .map { id -> cursorToRecipient.getRecipientCursor(id) }
                    .mapNotNull { recipientCursor ->
                        // Map the recipient cursor to a list of recipients
                        recipientCursor?.use {
                            recipientCursor.map {
                                cursorToRecipient.map(
                                    recipientCursor
                                )
                            }
                        }
                    }
                    .flatten()
                    .map { recipient ->
                        recipient.apply {
                            contact = contacts.firstOrNull {
                                it.numbers.any {
                                    phoneNumberUtils.compare(
                                        recipient.address,
                                        it.address
                                    )
                                }
                            }
                        }
                    }

                conversation.recipients.clear()
                conversation.recipients.addAll(recipients)
                conversation.lastMessage = lastMessage
                realm.executeTransaction { it.insertOrUpdate(conversation) }
                realm.close()

                conversation
            }
    }

    private fun getConversation(threadId: Long): ConversationNew? {
        return Realm.getDefaultInstance()
            .apply { refresh() }
            .where(ConversationNew::class.java)
            .equalTo("id", threadId)
            .findFirst()
    }

    fun getThreadId(context: Context, recipients: Collection<String>): Long? {
        val phoneNumberUtils = PhoneNumberUtils(context)
        return Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(ConversationNew::class.java)
                .findAll()
                .asSequence()
                .filter { conversation -> conversation.recipients.size == recipients.size }
                .find { conversation ->
                    conversation.recipients.map { it.address }.all { address ->
                        recipients.any { recipient -> phoneNumberUtils.compare(recipient, address) }
                    }
                }?.id
        }
    }

    private fun copyMessages() {
        val selectedText = conversation.lastMessage?.body!!
        copyToClipboard(selectedText)
    }

    private fun observeLiveData() {
        /*lifecycleScope.launch {
            viewModel.getAllScheduleConversationsLiveData()
                .observe(this@ScheduledActivity) { conversations ->
                    val newList = arrayListOf<ConversationNew>()
                    if (AdsUtility.isNetworkConnected(this@ScheduledActivity)) {
                        if (conversations.isNotEmpty() && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
                            binding.tvNoData.gone()
//                            newList.add(0, adModel)
                            newList.addAll(conversations)
                            conversationAdapter.submitList(newList) {
                                binding.rvScheduled.smoothScrollToPosition(0)
                            }
                        } else {
                            if (conversations.isEmpty()) {
                                binding.tvNoData.visible()
                            } else {
                                binding.tvNoData.gone()
                            }
                            newList.addAll(conversations)
                            conversationAdapter.submitList(newList) { }
                        }
                    } else {
                        if (conversations.isNotEmpty()) {
                            binding.tvNoData.gone()
                            newList.addAll(conversations)
                            conversationAdapter.submitList(newList) {
                                binding.rvScheduled.smoothScrollToPosition(0)
                            }
                        } else {
                            binding.tvNoData.visible()
                            binding.ivScrollUp.gone()
                            conversationAdapter.submitList(arrayListOf()) { }
                        }
                    }
                }
        }*/
        getRealmThread { realm ->
            conversationList = arrayListOf()
            val messages = realm.where(ScheduledMessageNew::class.java)
                .sort("date", Sort.DESCENDING).findAll().map { realm.copyFromRealm(it) }

            Log.e("TAG", "observeLiveData: messages ${messages.size}")
            if (messages.isEmpty()) {
                conversationList = arrayListOf()
                binding.tvNoData.visible()
                conversationAdapter.submitList(arrayListOf())
            } else {
                messages.forEach { msg ->
                    val conversation = realm.where(ConversationNew::class.java)
                        .anyOf("id", listOf(msg.threadId).toLongArray())
                        .findFirst()?.let { it1 -> realm.copyFromRealm(it1) }

                    conversation?.let {
                        it.lastMessage = msg.getMessage()
                        conversationList.add(it)
                    }
                }
                if (conversationList.isEmpty()) {
                    binding.tvNoData.visible()
                } else {
                    binding.tvNoData.gone()
                    conversationAdapter.submitList(conversationList)
                }
            }

        }
    }

    private fun viewClick() {
        binding.addMessage.setOnClickListener(1000L) {
            startActivity(
                Intent(
                    this@ScheduledActivity,
                    NewConversationActivity::class.java
                ).putExtra(CommonClass.IS_SCHEDULE, true)
            )
        }
        binding.ivScrollUp.setOnClickListener(1000L) {
            scrollToTop()
            binding.ivScrollUp.gone()
        }
    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = getString(R.string.scheduled)
        binding.threadToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun scrollToTop() {
        val ll = binding.rvScheduled.layoutManager as LinearLayoutManager
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}