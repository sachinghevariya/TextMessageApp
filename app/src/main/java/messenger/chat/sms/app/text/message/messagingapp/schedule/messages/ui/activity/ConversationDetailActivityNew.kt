package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager
import android.widget.TimePicker
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.RealmList
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.MyApp
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityConversationDetailNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.MessageAdapterNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.DialogSchedule
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteBlockedNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.scheduleMessage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.sendMessageCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Callback
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.RefreshMessages
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToConversationImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToRecipientImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.KeyManagerImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ScheduledMessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.TelephonyCompat
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ThreadItemNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.anyOf
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.autoScrollToStart
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.getRealmThread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.tryOrNull
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.IS_BLOCKED
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.IS_SCHEDULE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.detailScreenThreadId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyRecyclerView
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.copyToClipboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.generateRandomId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getCurrentYearMonthDay
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getCustomDateTimeInMillis
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getNextDay
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isShortCode
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.removeDiacriticsIfNeeded
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showCustomDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showKeyboard
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.value
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AppOpenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ConversationDetailActivityNew : BaseActivity<ActivityConversationDetailNewBinding>() {

    override fun getViewBinding() = ActivityConversationDetailNewBinding.inflate(layoutInflater)

    private lateinit var messageAdapter: MessageAdapterNew

    //    private val viewModel: MessageViewModel by viewModels()
    private var threadId: Long = -1
    private var threadTitle: String = ""
    private var isBlocked: Boolean = false
    private var isShortCode: Boolean = false
    private var participants = emptyList<String>()
    private var messages = ArrayList<MessageNew>()
    private var threadItems = ArrayList<ThreadItemNew>()
    private var isNewConversation = true
    private var isScheduledMessage = false
    private var isAllMessageFetched = false
    private var scheduleTime = -1L
    private var formattedDateTime = ""
    private val MIN_DATE_TIME_DIFF_SECS = 86400
    private var conversation: ConversationNew? = null
    private var selYear = 2023
    private var selMonth = 11
    private var selDay = 23
    private var refreshedSinceSent = false
    private var allMessagesFetched = false
    private var loadingOlderMessages = false
    private var oldestMessageDate = -1L
    private var bus: EventBus? = null
    private var scrolledReachBottom = true
    private var lastVisiblePosition = -1
    private val SCHEDULED_KEY = "scheduled_key"
    private val DIALOG_SHOWN_KEY = "dialog_key"
    private var isDialogShown = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        try {
            outState.putBoolean(SCHEDULED_KEY, isScheduledMessage)
            outState.putSerializable(DIALOG_SHOWN_KEY, isDialogShown)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        try {
            isScheduledMessage = savedInstanceState.getBoolean(SCHEDULED_KEY)
            isDialogShown = savedInstanceState.getBoolean(DIALOG_SHOWN_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initData() {
        bus = EventBus.getDefault()
        if (!bus!!.isRegistered(this)) {
            bus!!.register(this)
        }
        AppOpenManager.blockAppOpen(this)
        getIntentData()
        getConversation()
        setUpToolBar()
        initFlow()
    }

    override fun onDestroy() {
        super.onDestroy()
        bus?.unregister(this)
    }

    private fun manageScheduleView() {
        if (isScheduledMessage) {
            showScheduleDialog()
        }
    }

    private fun showScheduleDialog() {
        /*val callback: (Int) -> Unit = {
            Log.e("TAG", "showActionDialog: ${it}")
            val (currentYear, currentMonth, currentDay) = getCurrentYearMonthDay()
            val (currentNYear, currentNMonth, nextDay) = getNextDay(
                currentYear, currentMonth, currentDay
            )
            showScheduleSendUi()
            when (it) {
                1 -> {
                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_today)
                    scheduleTime =
                        getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 17, 0)
                }

                2 -> {
                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_tonight)
                    scheduleTime =
                        getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 21, 0)
                }

                3 -> {
                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.tomorrow)
                    scheduleTime =
                        getCustomDateTimeInMillis(currentNYear, currentNMonth, nextDay, 8, 0)
                }

                4 -> {
                    showDatePicker()
                }
            }
        }*/
        if (!isDialogShown) {
            val dialog = DialogSchedule()
            showCustomDialog(this, dialog)
            isDialogShown = true
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCallback(event: Callback) {
        val (currentYear, currentMonth, currentDay) = getCurrentYearMonthDay()
        val (currentNYear, currentNMonth, nextDay) = getNextDay(
            currentYear, currentMonth, currentDay
        )
        showScheduleSendUi()
        when (event.int) {
            1 -> {
                binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_today)
                scheduleTime =
                    getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 17, 0)
            }

            2 -> {
                binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_tonight)
                scheduleTime =
                    getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 21, 0)
            }

            3 -> {
                binding.messageHolder.tvSchedulesMsg.text = getString(R.string.tomorrow)
                scheduleTime =
                    getCustomDateTimeInMillis(currentNYear, currentNMonth, nextDay, 8, 0)
            }

            4 -> {
                showDatePicker()
            }
        }
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.CustomDatePickerDialogTheme),
            { _, year, month, dayOfMonth ->
                selYear = year
                selMonth = month
                selDay = dayOfMonth
                showTimePicker(year, month, dayOfMonth)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.setCancelable(false)
        datePickerDialog.setOnCancelListener {
            hideScheduleSendUi()
        }
        datePickerDialog.show()

    }

    private fun showTimePicker(year: Int, month: Int, dayOfMonth: Int) {
        val today = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(
            ContextThemeWrapper(this, R.style.CustomTimePickerDialogTheme),
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                val selectedDateTime = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, hourOfDay, minute)
                }
                processSelectedDateTime(selectedDateTime)
            },
            today.get(Calendar.HOUR_OF_DAY),
            today.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.setOnCancelListener {
            showDatePicker()
        }
        timePickerDialog.setCancelable(false)
        timePickerDialog.show()
    }

    private fun processSelectedDateTime(selectedDateTime: Calendar) {
        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy, hh:mm a", Locale.getDefault())
        formattedDateTime = dateFormat.format(selectedDateTime.time)
        scheduleTime = selectedDateTime.time.time
        binding.messageHolder.tvSchedulesMsg.text = "$formattedDateTime"

        if (System.currentTimeMillis() > selectedDateTime.time.time) {
            showToast(getString(R.string.future_selection))
            showTimePicker(selYear, selMonth, selDay)
        }
    }

    private fun getIntentData() {
        try {
            threadId = intent.getLongExtra(THREAD_ID, -1)
            getConversation()
            threadTitle = intent.getStringExtra(THREAD_TITLE)!!
            isBlocked = intent.getBooleanExtra(IS_BLOCKED, false)
            isScheduledMessage = intent.getBooleanExtra(IS_SCHEDULE, false)
            CoroutineScope(Dispatchers.IO).launch {
                if (!isBlocked && CommonClass.blockedThreadIds.contains(threadId)) {
                    isBlocked = true
                }
            }
        } catch (e: Exception) {
            threadTitle = ""
            e.printStackTrace()
        }
    }

    private fun initFlow() {
        manageMarquee()
        setupCachedMessages {
            viewClick()
            checkIfSenderIsShortcode()
            manageScheduleView()
        }
    }

    private fun setupCachedMessages(callback: () -> Unit) {
        getRealmThread { realm ->
            messages = realm
                .where(MessageNew::class.java)
                .equalTo("threadId", threadId)
                .sort("date")
                .findAllAsync().map { realm.copyFromRealm(it) } as ArrayList<MessageNew>
//            Log.e("TAG", "setupCachedMessages: ${messages.size}")
            setupAdapter()
            runOnUiThread {
                if (messages.isEmpty()) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    binding.messageHolder.threadTypeMessage.requestFocus()
                }
                callback()
            }
        }
    }

    private fun observeLiveData() {
//        getRealmThread { realm ->
//            messages = realm
//                .where(MessageNew::class.java)
//                .equalTo("threadId", threadId)
//                .sort("date")
//                .findAllAsync().map { realm.copyFromRealm(it) } as ArrayList<MessageNew>
//            messageAdapter.submitList(messages)
//        }

//        refreshMessages(RefreshMessages())
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
    }

    private fun manageMarquee() {
        binding.messageHolder.threadSelectSimNumber.isSelected = true
        binding.messageHolder.threadCharacterCounter.isSelected = true
    }

    private fun checkIfSenderIsShortcode() {
        if (isShortCode(threadTitle)) {
            isShortCode = true
            binding.messageHolder.root.gone()
            showSwipeDismissAbleSnackBar()
        } else {
            isShortCode = false
            binding.messageHolder.root.visible()
            if (!isScheduledMessage) {
                binding.messageHolder.threadTypeMessage.showKeyboard()
            }
        }
        manageBlockView()
    }

    private fun manageBlockView() {
        runOnUiThread {
            if (isBlocked) {
                binding.messageHolder.root.gone()
                binding.viewBlocked.visible()
                binding.tvBlockMsg.text = "${getString(R.string.block_info_msg)} '${threadTitle}'"
            } else {
                binding.viewBlocked.gone()
                if (!isShortCode) binding.messageHolder.root.visible()
            }
            messageAdapter.setIsConversationBlocked(isBlocked)
        }
    }

    private fun showSwipeDismissAbleSnackBar() {
        val snackBar = Snackbar.make(
            binding.root, getString(R.string.invalid_short_code), Snackbar.LENGTH_LONG
        )
        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = SwipeDismissBehavior<View>()
        behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
        params.behavior = behavior
        snackBar.view.layoutParams = params
        snackBar.show()
    }

    private fun setUpToolBar() {
        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.threadToolbar.title = threadTitle
        binding.threadToolbar.setNavigationOnClickListener {
            detailScreenThreadId = -1
            finish()
        }
        detailScreenThreadId = threadId
    }

    private fun setupAdapter() {
        runOnUiThread {
            getOrCreateThreadAdapter().apply {
                getRealmThread { realm ->
                    val list = realm
                        .where(MessageNew::class.java)
                        .equalTo("threadId", threadId)
                        .sort("date")
                        .findAllAsync()
                    markAsReadConversation()

                    messages = list.map { realm.copyFromRealm(it) } as ArrayList<MessageNew>
                    messageAdapter.updateData(list)

                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun refreshMessages(event: RefreshMessages) {
        refreshedSinceSent = true
        if (MyApp.threadId == threadId) {
            markAsReadConversation()
            if (event.isScrollBottom) {
                try {
                    runOnUiThread {
                        if (!scrolledReachBottom) binding.ivScrollDown.visible()
                        if (messageAdapter?.actionMode != null) {
                            messageAdapter.clearSelection()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun markAsReadConversation() {
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("threadId", listOf(threadId).toLongArray())
                .beginGroup()
                .equalTo("read", false)
                .or()
                .equalTo("seen", false)
                .endGroup()
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
        }
    }

    private fun getOrCreateThreadAdapter(): MessageAdapterNew {
        var curAdapter = binding.rvMessageList.adapter
        if (curAdapter == null) {
            messageAdapter =
                MessageAdapterNew(this) { item: MessageNew?, deleteEvent, copyEvent, blockEvent, starredEvent, selectedItems ->
                    if (item != null) {
                        Log.e("TAG", "unStarredMessages: ")
                        unStarredMessages(item)
                    }

                    if (deleteEvent) {
                        val itemsCnt = selectedItems.size
                        val items = resources.getQuantityString(
                            R.plurals.delete_messages, itemsCnt, itemsCnt
                        )
                        val baseString = R.string.deletion_confirmation
                        val question = String.format(resources.getString(baseString), items)
                        ConfirmationDialog.newInstance(
                            this,
                            question,
                            positive = getString(R.string.delete),
                            dialogTitle = getString(R.string.delete_msg)
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                deleteMessages(selectedItems)
                                withContext(Dispatchers.Main) {
                                    messageAdapter.clearSelection()
                                }
                            }
                        }
                    }

                    if (copyEvent) {
                        copyMessages(selectedItems)
                        messageAdapter.clearSelection()
                    }

                    if (blockEvent) {
                        val baseString = R.string.block_confirmation
                        val itemsCnt = selectedItems.size
                        val items = resources.getQuantityString(
                            R.plurals.delete_messages, itemsCnt, itemsCnt
                        )
                        val question = String.format(resources.getString(baseString), items)
                        ConfirmationDialog.newInstance(
                            this,
                            question,
                            positive = getString(R.string.ok),
                            dialogTitle = getString(R.string.block_msg)
                        ) {
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    messageAdapter.clearSelection()
                                    binding.messageHolder.root.gone()
                                }
                            }
                        }
                    }

                    if (starredEvent) {
                        starredMessages(selectedItems)
                        messageAdapter.clearSelection()
                    }
                }
            curAdapter = messageAdapter
            binding.rvMessageList.adapter = messageAdapter

            messageAdapter.autoScrollToStart(binding.rvMessageList)

            binding.rvMessageList.endlessScrollListener =
                object : MyRecyclerView.EndlessScrollListener {
                    override fun updateBottom(isBottom: Boolean) {
                        val layoutManager =
                            binding.rvMessageList.layoutManager as LinearLayoutManager
                        lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                        scrolledReachBottom = isBottom
                        if (isBottom) {
                            binding.ivScrollDown.gone()
                        }
                    }

                    override fun updateTop() {
                    }
                }
        }
        return curAdapter as MessageAdapterNew
    }

    private fun starredMessages(selectedItems: List<MessageNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        val list = arrayListOf<Long>()
        selectedItems.forEach {
            list.add(it.id)
        }
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("id", list.toLongArray())
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.isStarred = true
                }
            }
        }
        observeLiveData()
    }

    private fun unStarredMessages(item: MessageNew) {
        val list = arrayListOf<Long>()
        list.add(item.id)
        getRealmThread { realm ->
            val messages = realm.where(MessageNew::class.java)
                .anyOf("id", list.toLongArray())
                .findAll()
            realm.executeTransaction {
                messages.forEach { message ->
                    message.isStarred = false
                }
            }
        }
        observeLiveData()
    }

    private fun copyMessages(selectedItems: List<MessageNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        val selectedText = selectedItems.reversed().joinToString("\n") { it.body }
        copyToClipboard(selectedText)
    }

    private fun deleteMessages(selectedItems: List<MessageNew>) {
        getRealmThread { realm ->
            if (selectedItems.isEmpty()) {
                return@getRealmThread
            }

            notificationManager.cancel(threadId.toInt())

            val messageIds = arrayListOf<Long>()
            selectedItems.forEach {
                messageIds.add(it.id)
            }

            if (messages.size == selectedItems.size) {
                getRealmThread { realm ->
                    val messages = realm.where(MessageNew::class.java)
                        .anyOf("id", messageIds.toLongArray())
                        .findAll()

                    val uris = messages.map { it.getUri() }

                    realm.executeTransaction { messages.deleteAllFromRealm() }

                    uris.forEach { uri -> contentResolver.delete(uri, null, null) }

                    val conversation = realm.where(ConversationNew::class.java)
                        .anyOf("id", listOf(threadId).toLongArray())
                        .findAll()

                    realm.executeTransaction { conversation.deleteAllFromRealm() }
                }
                deleteConversation(threadId)
//            viewModel.deleteConversationByThreadId(threadId)
//            viewModel.deleteMessageByThreadId(threadId)
                notificationManager.cancel(threadId.toInt())
                runOnUiThread {
                    finish()
//                bus?.post(RefreshMessages())
                }
            } else {
                messages.removeAll(selectedItems.toSet())
                val messages = realm.where(MessageNew::class.java)
                    .anyOf("id", messageIds.toLongArray())
                    .findAll()
                val uris = messages.map { it.getUri() }
                realm.executeTransaction { messages.deleteAllFromRealm() }
                uris.forEach { uri -> contentResolver.delete(uri, null, null) }

                val conversation = realm.where(ConversationNew::class.java)
                    .anyOf("id", listOf(threadId).toLongArray())
                    .findAll()

                realm.executeTransaction {
                    conversation.forEach {
                        it.lastMessage =
                            realm.where(MessageNew::class.java)
                                .sort("date", Sort.DESCENDING)
                                .equalTo("threadId", threadId).findFirst()
                    }
                }

            }
        }
        observeLiveData()
    }

    private fun viewClick() {
        setupParticipants()
        binding.messageHolder.threadSendMessage.setOnClickListener(1000L) {
            sendMessage()
        }
        binding.threadToolbar.setOnClickListener(1000L) {
            /*startActivity(
                 Intent(this, ContactDetailActivity::class.java)
                     .putExtra(THREAD_TITLE, threadTitle)
                     .putExtra(THREAD_ID, threadId)
                     .putExtra(
                         CONTACT_LIST,
                         messages.first().participants as ArrayList<SimpleContact>
                     )
             )*/
        }
        binding.ivScrollDown.setOnClickListener(1000L) {
            scrollToBottom()
            binding.ivScrollDown.gone()
        }
        binding.btnUnblock.setOnClickListener(1000L) {
            unblockConversation(conversation)
        }
        binding.messageHolder.ivCloseSchedule.setOnClickListener(1000L) {
            hideScheduleSendUi()
        }
        binding.messageHolder.containerSchedule.setOnClickListener(1000L) {
            showDatePicker()
        }
    }

    private fun sendScheduleMessage(text: String, subscriptionId: Int) {
        try {
            refreshedSinceSent = false
            try {
                getOrCreateConversation(participants)?.let {
                    updateConversations(threadId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            CoroutineScope(Dispatchers.IO).launch {
                val messageId = generateRandomId()
                val message = buildScheduledMessage(text, subscriptionId, messageId, scheduleTime)

                scheduleMessage(message)
                Handler(Looper.getMainLooper()).postDelayed({
                    updateConversations(threadId)
                    getOrCreateConversation(participants)
                }, 300)
                observeLiveData()
                runOnUiThread {
                    clearCurrentMessage()
                    hideScheduleSendUi()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showScheduleSendUi() {
        binding.messageHolder.containerSchedule.visible()
    }

    private fun hideScheduleSendUi() {
        isScheduledMessage = false
        binding.messageHolder.containerSchedule.gone()
    }

    private fun buildScheduledMessage(
        text: String, subscriptionId: Int, messageId: Long, scheduleTime: Long
    ): ScheduledMessageNew {
        var message: ScheduledMessageNew? = null
        Realm.getDefaultInstance().use { realm ->
            val id = (realm.where(ScheduledMessageNew::class.java).max("id")?.toLong() ?: -1) + 1
            val recipientsRealmList = RealmList(*participants.toTypedArray())
            val attachmentsRealmList = RealmList<String>()

            message = ScheduledMessageNew(
                id,
                threadId,
                scheduleTime,
                subscriptionId,
                recipientsRealmList,
                false,
                text,
                attachmentsRealmList
            )

            realm.executeTransaction { realm.insertOrUpdate(message) }
        }
        return message!!
    }

    private fun getConversation() {
        CoroutineScope(Dispatchers.IO).launch {
            if (threadId != null && threadId != 0L) {
                getRealmThread { realm ->
                    conversation = realm.where(ConversationNew::class.java)
                        .equalTo("id", threadId)
                        .findFirst()?.let {
                            realm.copyFromRealm(
                                it
                            )
                        }
                }
            }
        }
    }

    private fun unblockConversation(selectedItem: ConversationNew?) {
        if (selectedItem == null) {
            if (participants.isNotEmpty()) {
                val phoneNumber = participants[0]
                deleteBlockedNumber(phoneNumber)
                this@ConversationDetailActivityNew.isBlocked = false
                manageBlockView()
            } else {
                participants = getPhoneNumbersFromIntent()
                deleteBlockedNumber(participants[0])
                this@ConversationDetailActivityNew.isBlocked = false
                manageBlockView()
            }
            return
        }
        selectedItem.apply {
            lastMessage?.address?.let { it1 -> deleteBlockedNumber(it1) }
            getRealmThread { realm ->
                val conversations = realm.where(ConversationNew::class.java)
                    .anyOf("id", listOf(threadId).toLongArray())
                    .equalTo("blocked", true)
                    .findAll()

                realm.executeTransaction {
                    conversations.forEach { conversation ->
                        conversation.blocked = false
                    }
                }
                this@ConversationDetailActivityNew.isBlocked = false
                manageBlockView()
            }
        }
    }

    private fun sendMessage() {
        var text = binding.messageHolder.threadTypeMessage.value
        if (text.isEmpty()) {
            showToast(getString(R.string.empty_msg))
            return
        }
        messageAdapter.clearSelection()
        scrollToBottom()

        text = removeDiacriticsIfNeeded(text)

        val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()

        if (participants.isEmpty()) {
            participants = getPhoneNumbersFromIntent()
        }

        if (isScheduledMessage) {
            sendScheduleMessage(text, subscriptionId)
        } else {
            sendNormalMessage(text, subscriptionId)
        }

    }

    private fun setupParticipants() {
        if (participants.isEmpty()) {
            conversation?.let {
                participants = when (conversation?.recipients?.isNotEmpty()!!) {
                    true -> conversation?.recipients!!.map { it.address }
                    false -> emptyList()
                }
            }
        }
    }

    private fun getPhoneNumbersFromIntent(): ArrayList<String> {
        val numberFromIntent = intent.getStringExtra(THREAD_NUMBER)
        val numbers = ArrayList<String>()

        if (numberFromIntent != null) {
            if (numberFromIntent.startsWith('[') && numberFromIntent.endsWith(']')) {
                val type = object : TypeToken<List<String>>() {}.type
                numbers.addAll(Gson().fromJson(numberFromIntent, type))
            } else {
                numbers.add(numberFromIntent)
            }
        }
        return numbers
    }

    private fun sendNormalMessage(text: String, subscriptionId: Int) {
        if (participants.isEmpty()) {
            participants = getPhoneNumbersFromIntent()
        }
        try {
            refreshedSinceSent = false
            val messagingUtils = MessagingUtils(this)
            val message = insertSentSms(
                subscriptionId,
                threadId,
                participants.first(),
                text,
                System.currentTimeMillis()
            )
            Handler(Looper.getMainLooper()).postDelayed({
                sendMessageCompat(
                    text,
                    participants,
                    subscriptionId,
                    message.contentId,
                    messagingUtils
                )
                updateConversations(threadId)
                getOrCreateConversation(participants)
            }, 300)
            observeLiveData()
            clearCurrentMessage()
        } catch (e: Exception) {
            showToast(e.message ?: "")
        } catch (e: Error) {
            showToast(e.localizedMessage ?: getString(R.string.unknown_error_occurred))
        }
    }

    private fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long,
    ): MessageNew {
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

        val uri = contentResolver.insert(Telephony.Sms.CONTENT_URI, values)


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
        getRealmThread { realm ->
            realm.refresh()

            threadIds.forEach { threadId ->
                val conversation = realm
                    .where(ConversationNew::class.java)
                    .equalTo("id", threadId)
                    .findFirst() ?: return@getRealmThread

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

    private fun getOrCreateConversation(addresses: List<String>): ConversationNew? {
        if (addresses.isEmpty()) {
            return null
        }

        return (getThreadId(addresses) ?: tryOrNull {
            TelephonyCompat.getOrCreateThreadId(
                this,
                addresses.toSet()
            )
        })
            ?.takeIf { threadId -> threadId != 0L }
            ?.let { threadId ->
                getConversation(threadId)
                    ?.let(Realm.getDefaultInstance()::copyFromRealm)
                    ?: getConversationFromCp(threadId)
            }
    }

    private fun getConversationFromCp(threadId: Long): ConversationNew? {
        val cursorToConversation = CursorToConversationImpl(this@ConversationDetailActivityNew)
        val cursorToRecipient = CursorToRecipientImpl(this@ConversationDetailActivityNew)
        val phoneNumberUtils = PhoneNumberUtils(this@ConversationDetailActivityNew)

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

    fun getThreadId(recipients: Collection<String>): Long? {
        val phoneNumberUtils = PhoneNumberUtils(this)
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

    private fun clearCurrentMessage() {
        binding.messageHolder.threadTypeMessage.setText("")
    }

    private fun scrollToBottom() {
        val ll = binding.rvMessageList.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(messageAdapter.data?.size!! - 1, 0)
    }

    override fun onBackPressed() {
        detailScreenThreadId = -1
        super.onBackPressed()
    }

}