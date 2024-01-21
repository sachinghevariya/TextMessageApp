//package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.os.Handler
//import android.os.Looper
//import android.provider.Telephony
//import android.telephony.SmsManager
//import android.util.Log
//import android.view.ContextThemeWrapper
//import android.view.View
//import android.widget.TimePicker
//import androidx.activity.viewModels
//import androidx.coordinatorlayout.widget.CoordinatorLayout
//import androidx.lifecycle.distinctUntilChanged
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.behavior.SwipeDismissBehavior
//import com.google.android.material.snackbar.Snackbar
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityConversationDetailBinding
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.MessageAdapter
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.addBlockedNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.countMessagesInThread
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteBlockedNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteMessage
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getConversations
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getMessages
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getNameAndPhotoFromPhoneNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadParticipants
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markMessageRead
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.scheduleMessage
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.sendMessageCompat
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ScheduleMessage
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.ThreadItem
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.IS_BLOCKED
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.IS_SCHEDULE
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_NUMBER
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.detailScreenThreadId
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.ReverseEndlessRecyclerViewScrollListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.copyToClipboard
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.ensureBackgroundThread
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.generateRandomId
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getAddresses
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getCurrentYearMonthDay
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getCustomDateTimeInMillis
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getNextDay
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isShortCode
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.removeDiacriticsIfNeeded
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showCustomDialog
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showKeyboard
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.value
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.MessageViewModel
//import plugin.adsdk.extras.NetworkChangeReceiver
//import plugin.adsdk.service.AppOpenManager
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
//@AndroidEntryPoint
//class ConversationDetailActivity : BaseActivity<ActivityConversationDetailBinding>() {
//
//    override fun getViewBinding() = ActivityConversationDetailBinding.inflate(layoutInflater)
//
//    private lateinit var messageAdapter: MessageAdapter
//    private val viewModel: MessageViewModel by viewModels()
//    private var threadId: Long = -1
//    private var threadTitle: String = ""
//    private var isBlocked: Boolean = false
//    private var isShortCode: Boolean = false
//    private var participants = ArrayList<SimpleContact>()
//    private var messages = ArrayList<Message>()
//    private var threadItems = ArrayList<ThreadItem>()
//    private var isNewConversation = true
//    private var isScheduledMessage = false
//    private var isAllMessageFetched = false
//    private var scheduleTime = -1L
//    private var formattedDateTime = ""
//    private val MIN_DATE_TIME_DIFF_SECS = 86400
//    private var conversation: Conversation? = null
//    private var selYear = 2023
//    private var selMonth = 11
//    private var selDay = 23
//
//    //    private var blockedThreadIds = emptyList<Long>()
//    private var isScrollToBottomCalled: Boolean = false
//    private var processedMessageIds = HashSet<Long>()
//
//    override fun initData() {
//        AppOpenManager.blockAppOpen(this)
//        binding.loader.visible()
//        getIntentData()
//        getConversation()
//        setupAdapter()
//        setUpToolBar()
//        initFlow()
//        manageScheduleView()
//        checkIfAllMessageFetched()
//    }
//
//    private fun manageScheduleView() {
//        if (isScheduledMessage) {
//            showScheduleDialog()
//        }
//    }
//
//    private fun showScheduleDialog() {
//
////        val dialog = DialogSchedule()
////        dialog.setCallback {
////            val (currentYear, currentMonth, currentDay) = getCurrentYearMonthDay()
////            val (currentNYear, currentNMonth, nextDay) = getNextDay(
////                currentYear, currentMonth, currentDay
////            )
////            showScheduleSendUi()
////            when (it) {
////                1 -> {
////                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_today)
////                    scheduleTime =
////                        getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 17, 0)
////                }
////
////                2 -> {
////                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.later_tonight)
////                    scheduleTime =
////                        getCustomDateTimeInMillis(currentYear, currentMonth, currentDay, 21, 0)
////                }
////
////                3 -> {
////                    binding.messageHolder.tvSchedulesMsg.text = getString(R.string.tomorrow)
////                    scheduleTime =
////                        getCustomDateTimeInMillis(currentNYear, currentNMonth, nextDay, 8, 0)
////                }
////
////                4 -> {
////                    showDatePicker()
////                }
////            }
////        }
////        showCustomDialog(this, dialog)
//    }
//
//    private fun showDatePicker() {
//        val today = Calendar.getInstance()
//
//        val datePickerDialog = DatePickerDialog(
//            ContextThemeWrapper(this, R.style.CustomDatePickerDialogTheme),
//            { _, year, month, dayOfMonth ->
//                selYear = year
//                selMonth = month
//                selDay = dayOfMonth
//                showTimePicker(year, month, dayOfMonth)
//            },
//            today.get(Calendar.YEAR),
//            today.get(Calendar.MONTH),
//            today.get(Calendar.DAY_OF_MONTH)
//        )
//
//        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
//        datePickerDialog.setOnCancelListener {
//            hideScheduleSendUi()
//        }
//        datePickerDialog.show()
//
//    }
//
//    private fun showTimePicker(year: Int, month: Int, dayOfMonth: Int) {
//        val today = Calendar.getInstance()
//
//        val timePickerDialog = TimePickerDialog(
//            ContextThemeWrapper(this, R.style.CustomTimePickerDialogTheme),
//            { _: TimePicker, hourOfDay: Int, minute: Int ->
//                val selectedDateTime = Calendar.getInstance().apply {
//                    set(year, month, dayOfMonth, hourOfDay, minute)
//                }
//                processSelectedDateTime(selectedDateTime)
//            },
//            today.get(Calendar.HOUR_OF_DAY),
//            today.get(Calendar.MINUTE),
//            true
//        )
//        timePickerDialog.setOnCancelListener {
//            showDatePicker()
//        }
//        timePickerDialog.show()
//    }
//
//    private fun processSelectedDateTime(selectedDateTime: Calendar) {
//        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy, hh:mm a", Locale.getDefault())
//        formattedDateTime = dateFormat.format(selectedDateTime.time)
//        scheduleTime = selectedDateTime.time.time
//        binding.messageHolder.tvSchedulesMsg.text = "$formattedDateTime"
//
//        if (System.currentTimeMillis() > selectedDateTime.time.time) {
//            showToast(getString(R.string.future_selection))
//            showTimePicker(selYear, selMonth, selDay)
//        }
//    }
//
//    private fun getIntentData() {
//        threadId = intent.getLongExtra(THREAD_ID, -1)
//        getConversation()
//        threadTitle = intent.getStringExtra(THREAD_TITLE)!!
//        isBlocked = intent.getBooleanExtra(IS_BLOCKED, false)
//        isScheduledMessage = intent.getBooleanExtra(IS_SCHEDULE, false)
//        CoroutineScope(Dispatchers.IO).launch {
//            if (!isBlocked && CommonClass.blockedThreadIds.contains(threadId)) {
//                isBlocked = true
//            }
//        }
//    }
//
//    private fun initFlow() {
//        manageMarquee()
//        lifecycleScope.launch {
//            viewModel.getThreadMessages(threadId).distinctUntilChanged().observe(this@ConversationDetailActivity) {
//                messages.clear()
//                messages.addAll(it)
//                if (messages.size > 0) {
//                    isNewConversation = false
//                }
//                CoroutineScope(Dispatchers.IO).launch {
//                    threadItems = getThreadItems()
//                    withContext(Dispatchers.Main){
//                        messageAdapter.submitList(threadItems)
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            scrollToBottom()
//                        }, 500)
//                        if (it.isEmpty()) {
//                            binding.ivScrollDown.gone()
//                        }
//                        binding.loader.gone()
//                    }
//                }
//            }
//        }
//
//        /*viewModel.getThreadMessages(threadId)
//            .observe(this@ConversationDetailActivity) { newMessages ->
//                // Identify new messages since the last update
//                val newMessagesList =
//                    newMessages.filter { !processedMessageIds.contains(it.id) } as ArrayList<Message>
//                // Add new messages to the messages list
//                messages.addAll(newMessagesList)
//
//                // Mark new messages as processed
//                processedMessageIds.addAll(newMessagesList.map { it.id })
//
//                // Process only the new messages to update threadItems
//                CoroutineScope(Dispatchers.IO).launch {
//                    val newThreadItems = getThreadItems(newMessagesList)
//                    withContext(Dispatchers.Main) {
//                        threadItems.addAll(newThreadItems)
//                        messageAdapter.submitList(threadItems)
//                    }
//
//                    // Scroll to bottom after the list is updated
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        scrollToBottom()
//                    }, 500)
//
//                    // Hide loader if newMessages is empty
//                    if (newMessages.isEmpty()) {
//                        binding.ivScrollDown.gone()
//                    }
//
//                    binding.loader.gone()
//                }
//            }*/
//        viewClick()
//        checkIfSenderIsShortcode()
//    }
//
//    private fun getThreadItems(newMessages: ArrayList<Message>): ArrayList<ThreadItem> {
//        val items = ArrayList<ThreadItem>()
//        if (isFinishing) {
//            return items
//        }
//        if (newMessages.isEmpty()) {
//            return items
//        }
//        newMessages.sortBy { it.date }
//
//        viewModel.markAsReadConversation(threadId)
//        var prevDateTime = 0
//
//        // Process only the new messages
//        newMessages.forEach { message ->
//            if (!message.read) {
//                markMessageRead(message.id, message.isMMS)
//                viewModel.markAsReadMessage(message.id)
//            }
//
//            if (message.date - prevDateTime > MIN_DATE_TIME_DIFF_SECS) {
//                items.add(ThreadItem.ThreadDateTime(message.date, "1"))
//                prevDateTime = message.date
//            }
//            items.add(message)
//
//            if (message.type == Telephony.Sms.MESSAGE_TYPE_FAILED) {
//                items.add(ThreadItem.ThreadError(message.id, message.body))
//            }
//
//            if (message.type == Telephony.Sms.MESSAGE_TYPE_OUTBOX) {
//                items.add(ThreadItem.ThreadSending(message.id))
//            }
//        }
//
//        return items
//    }
//
//    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
//        super.networkStateChanged(state)
//    }
//
//    private fun manageMarquee() {
//        binding.messageHolder.threadSelectSimNumber.isSelected = true
//        binding.messageHolder.threadCharacterCounter.isSelected = true
//    }
//
//    private fun checkIfSenderIsShortcode() {
//        if (isShortCode(threadTitle)) {
//            isShortCode = true
//            binding.messageHolder.root.gone()
//            showSwipeDismissAbleSnackBar()
//        } else {
//            isShortCode = false
//            binding.messageHolder.root.visible()
//        }
//        manageBlockView()
//    }
//
//    private fun manageBlockView() {
//        runOnUiThread {
//            if (isBlocked) {
//                binding.messageHolder.root.gone()
//                binding.viewBlocked.visible()
//                binding.tvBlockMsg.text = "${getString(R.string.block_info_msg)} '${threadTitle}'"
//            } else {
//                binding.viewBlocked.gone()
//                if (!isShortCode) binding.messageHolder.root.visible()
//            }
//            messageAdapter.setIsConversationBlocked(isBlocked)
//        }
//    }
//
//    private fun showSwipeDismissAbleSnackBar() {
//        val snackBar = Snackbar.make(
//            binding.root, getString(R.string.invalid_short_code), Snackbar.LENGTH_LONG
//        )
//        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
//        val behavior = SwipeDismissBehavior<View>()
//        behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
//        params.behavior = behavior
//        snackBar.view.layoutParams = params
//        snackBar.show()
//    }
//
//    private fun setUpToolBar() {
//        binding.threadToolbar.setNavigationIcon(R.drawable.ic_back)
//        binding.threadToolbar.title = threadTitle
//        binding.threadToolbar.setNavigationOnClickListener {
//            detailScreenThreadId = -1
//            finish()
//        }
//        detailScreenThreadId = threadId
//    }
//
//    private fun setupAdapter() {
//        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        binding.rvMessageList.layoutManager = layoutManager
//        messageAdapter =
//            MessageAdapter(this) { item: Message?, deleteEvent, copyEvent, blockEvent, starredEvent, selectedItems ->
//                if (item != null) {
//                    unStarredMessages(item)
//                }
//
//                if (deleteEvent) {
//                    val itemsCnt = selectedItems.size
//                    val items = resources.getQuantityString(
//                        R.plurals.delete_messages, itemsCnt, itemsCnt
//                    )
//                    val baseString = R.string.deletion_confirmation
//                    val question = String.format(resources.getString(baseString), items)
//                    ConfirmationDialog.newInstance(
//                        this,
//                        question,
//                        positive = getString(R.string.delete),
//                        dialogTitle = getString(R.string.delete_msg)
//                    ) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            deleteMessages(selectedItems)
//                            withContext(Dispatchers.Main) {
//                                messageAdapter.clearSelection()
//                            }
//                        }
//                    }
//                }
//
//                if (copyEvent) {
//                    copyMessages(selectedItems)
//                    messageAdapter.clearSelection()
//                }
//
//                if (blockEvent) {
//                    val baseString = R.string.block_confirmation
//                    val itemsCnt = selectedItems.size
//                    val items = resources.getQuantityString(
//                        R.plurals.delete_messages, itemsCnt, itemsCnt
//                    )
//                    val question = String.format(resources.getString(baseString), items)
//                    ConfirmationDialog.newInstance(
//                        this,
//                        question,
//                        positive = getString(R.string.ok),
//                        dialogTitle = getString(R.string.block_msg)
//                    ) {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            blockConversation(selectedItems)
//                            withContext(Dispatchers.Main) {
//                                messageAdapter.clearSelection()
//                                binding.messageHolder.root.gone()
//                            }
//                        }
//                    }
//                }
//
//                if (starredEvent) {
//                    starredMessages(selectedItems)
//                    messageAdapter.clearSelection()
//                }
//            }
//        binding.rvMessageList.adapter = messageAdapter
//
//        val scrollListener = object :
//            ReverseEndlessRecyclerViewScrollListener(layoutManager, object : OnScrollListener {
//                override fun onScrolledUp() {
//                    binding.ivScrollDown.visible()
//                }
//
//                override fun onScrolledDown() {}
//
//                override fun onReachedBottom() {
//                    binding.ivScrollDown.gone()
//                }
//
//                override fun onReachedTop() {}
//            }) {
//            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
//                checkIfAllMessageFetched()
//            }
//        }
//
//        /*val scrollListener = MyScrollListener(object : OnScrollListener {
//            override fun onScrolledUp() {
//                binding.ivScrollDown.visible()
//            }
//
//            override fun onScrolledDown() {}
//
//            override fun onReachedBottom() {
//                binding.ivScrollDown.gone()
//            }
//
//            override fun onReachedTop() {}
//        })*/
//        binding.rvMessageList.addOnScrollListener(scrollListener)
//        binding.messageHolder.threadTypeMessage.showKeyboard()
//        Handler(Looper.getMainLooper()).postDelayed({
//            scrollToBottom()
//        }, 500)
//    }
//
//    private fun starredMessages(selectedItems: List<Message>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                viewModel.starredMessageById(it.id)/*val index = findThreadItemIndexByMessageId(it.id)
//                if (index != -1) {
//                    val item = threadItems[index] as Message
//                    item.isStarred = true
//                    threadItems.removeAt(index)
//                    threadItems.add(index, item)
//                    withContext(Dispatchers.Main) {
//                        messageAdapter.submitList(threadItems)
//                    }
//                }*/
//            }
//        }
//    }
//
//    private fun findThreadItemIndexByMessageId(messageId: Long): Int {
//        val foundIndex = threadItems.indexOfFirst { threadItem ->
//            when (threadItem) {
//                is Message -> threadItem.id == messageId
//                // Add other ThreadItem types if needed
//                else -> false
//            }
//        }
//
//        return if (foundIndex != -1) foundIndex else -1
//    }
//
//    private fun unStarredMessages(item: Message) {
//        CoroutineScope(Dispatchers.IO).launch {
//            item.apply {
//                viewModel.unStarredMessageById(id)/* val index = findThreadItemIndexByMessageId(id)
//                 if (index != -1) {
//                     val item = threadItems[index] as Message
//                     item.isStarred = false
//                     threadItems.removeAt(index)
//                     threadItems.add(index, item)
//                     withContext(Dispatchers.Main) {
//                         messageAdapter.submitList(threadItems)
//                     }
//                 }*/
//            }
//        }
//    }
//
//    private fun blockConversation(selectedItems: ArrayList<Message>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//
//        val conversation = viewModel.getConversationByThreadId(selectedItems[0].threadId)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            conversation.apply {
//                addBlockedNumber(phoneNumber)
//                viewModel.addToBlockConversation(threadId)
//            }
//        }
//    }
//
//    private fun copyMessages(selectedItems: List<Message>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        val selectedText = selectedItems.reversed().joinToString("\n") { it.body }
//        copyToClipboard(selectedText)
//    }
//
//    private fun deleteMessages(selectedItems: List<Message>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//
//        if (messages.size == selectedItems.size) {
//            deleteConversation(threadId)
//            viewModel.deleteConversationByThreadId(threadId)
//            viewModel.deleteMessageByThreadId(threadId)
//            notificationManager.cancel(threadId.toInt())
//            runOnUiThread {
//                finish()
//            }
//        } else {
//            messages.removeAll(selectedItems.toSet())
//            val msg = messages.last()
//            CoroutineScope(Dispatchers.IO).launch {
//                selectedItems.forEach {
//                    deleteMessage(it.id, it.isMMS)
//                    viewModel.deleteSelectedMessage(it.id)
//                    notificationManager.cancel(it.threadId.toInt())
//                }
//                val conversation = viewModel.getConversationByThreadId(threadId)
//                if (conversation != null) {
//                    conversation.msgId = msg.id
//                    conversation.snippet = msg.body
//                    conversation.date = msg.date
//                    conversation.read = msg.read
//                    viewModel.insertOrUpdateConversation(conversation)
//                }
//            }
//        }
//    }
//
//    private fun viewClick() {
//        setupParticipants()
//        binding.messageHolder.threadSendMessage.setOnClickListener(1000L) {
//            sendMessage()
//        }
//        binding.threadToolbar.setOnClickListener(1000L) {
//            /*startActivity(
//                 Intent(this, ContactDetailActivity::class.java)
//                     .putExtra(THREAD_TITLE, threadTitle)
//                     .putExtra(THREAD_ID, threadId)
//                     .putExtra(
//                         CONTACT_LIST,
//                         messages.first().participants as ArrayList<SimpleContact>
//                     )
//             )*/
//        }
//        binding.ivScrollDown.setOnClickListener(1000L) {
//            scrollToBottom()
//            binding.ivScrollDown.gone()
//        }
//        binding.btnUnblock.setOnClickListener(1000L) {
//            unblockConversation(conversation)
//        }
//        binding.messageHolder.ivCloseSchedule.setOnClickListener(1000L) {
//            hideScheduleSendUi()
//        }
//        binding.messageHolder.containerSchedule.setOnClickListener(1000L) {
//            showDatePicker()
//        }
//    }
//
//    private fun sendScheduleMessage(text: String, subscriptionId: Int) {
//        try {
//            CoroutineScope(Dispatchers.IO).launch {
//                val messageId = generateRandomId()
//                val message = buildScheduledMessage(text, subscriptionId, messageId, scheduleTime)
//                if (isNewConversation) {
//                    val conversation = createNewConversation(message, threadId)
//                    if (conversation != null) {
//                        conversation.msgId = message.id
//                        conversation.snippet = message.body
//                        conversation.date = message.date
//                        conversation.isScheduled = true
//                        viewModel.insertOrUpdateConversation(conversation)
//                    }
//                } else {/* val conversation = viewModel.getConversationByThreadId(threadId)
//                     if (conversation != null) {
//                         conversation.msgId = message.id
//                         conversation.snippet = message.body
//                         conversation.date = message.date
//                         conversation.isScheduled = true
//                         viewModel.insertOrUpdateConversation(conversation)
//                     }*/
//                }
//
//                scheduleMessage(message.getMessage())
//                insertOrUpdateScheduleMessage(message)
//
//                runOnUiThread {
//                    clearCurrentMessage()
//                    hideScheduleSendUi()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun createNewConversation(
//        message: ScheduleMessage, threadId: Long = generateRandomId()
//    ): Conversation {
//        val nameAndPhoto = getNameAndPhotoFromPhoneNumber(message.senderPhoneNumber)
//        val phoneNumber = PhoneNumber(message.senderPhoneNumber, 0, "", message.senderPhoneNumber)
//        val participant = SimpleContact(
//            0,
//            0,
//            nameAndPhoto.first,
//            nameAndPhoto.second,
//            arrayListOf(phoneNumber),
//            ArrayList(),
//            ArrayList()
//        )
//        participants = arrayListOf(participant)
//        val senderName = nameAndPhoto.first
//        val photoUri = nameAndPhoto.second
//
//        return Conversation(
//            threadId = threadId,
//            snippet = message.body,
//            date = message.date,
//            read = true,
//            title = senderName,
//            photoUri = photoUri,
//            isGroupConversation = false,
//            phoneNumber = message.senderPhoneNumber,
//            categoryType = MessageType.OTHER,
//            msgId = message.id,
//            message = null,
//            isArchive = 0,
//            isPin = 0,
//            isBlocked = 0,
//            isScheduled = true
//        )
//    }
//
//    private fun showScheduleSendUi() {
//        binding.messageHolder.containerSchedule.visible()
//    }
//
//    private fun hideScheduleSendUi() {
//        isScheduledMessage = false
//        binding.messageHolder.containerSchedule.gone()
//    }
//
//    private fun buildScheduledMessage(
//        text: String, subscriptionId: Int, messageId: Long, scheduleTime: Long
//    ): ScheduleMessage {
//        return ScheduleMessage(
//            id = messageId,
//            body = text,
//            type = Telephony.Sms.MESSAGE_TYPE_QUEUED,
//            status = Telephony.Sms.STATUS_NONE,
//            participants = participants,
//            date = (scheduleTime / 1000).toInt(),
//            read = true,
//            threadId = threadId,
//            isMMS = false,
//            senderPhoneNumber = threadTitle,
//            senderName = threadTitle,
//            senderPhotoUri = "",
//            subscriptionId = subscriptionId,
//            categoryType = MessageType.OTHER,
//            isScheduled = true
//        )
//    }
//
//    private fun getConversation() {
//        CoroutineScope(Dispatchers.IO).launch {
//            conversation = viewModel.getConversationByThreadId(threadId)
//        }
//    }
//
//    private fun unblockConversation(selectedItem: Conversation?) {
//        if (selectedItem == null) {
//            val addresses = participants.getAddresses()
//            if (addresses.isNotEmpty()) {
//                val phoneNumber = addresses[0]
//                deleteBlockedNumber(phoneNumber)
//                this@ConversationDetailActivity.isBlocked = false
//                manageBlockView()
//            }
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItem.apply {
//                deleteBlockedNumber(phoneNumber)
//                viewModel.unblockConversation(threadId)
//                this@ConversationDetailActivity.isBlocked = false
//                manageBlockView()
//            }
//        }
//    }
//
//    private fun sendMessage() {
//        var text = binding.messageHolder.threadTypeMessage.value
//        if (text.isEmpty()) {
//            showToast(getString(R.string.empty_msg))
//            return
//        }
//        messageAdapter.clearSelection()
//        scrollToBottom()
//
//        text = removeDiacriticsIfNeeded(text)
//
//        val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
//
//        if (isScheduledMessage) {
//            sendScheduleMessage(text, subscriptionId)
//        } else {
//            sendNormalMessage(text, subscriptionId)
//        }
//
//    }
//
//    private fun setupParticipants() {
//        if (participants.isEmpty()) {
//            participants = if (messages.isEmpty()) {
//                val intentNumbers = getPhoneNumbersFromIntent()
//                val participants = getThreadParticipants(threadId, null)
//                fixParticipantNumbers(participants, intentNumbers)
//            } else {
//                messages.first().participants as ArrayList<SimpleContact>
//            }
//            if (participants.isEmpty()) {
//                val name = intent.getStringExtra(THREAD_TITLE) ?: ""
//                val number = intent.getStringExtra(THREAD_NUMBER)
//                if (number == null) {
//                    showToast(getString(R.string.unknown_error_occurred))
//                    finish()
//                    return
//                }
//
//                val phoneNumber = PhoneNumber(number, 0, "", number)
//                val contact = SimpleContact(
//                    0, 0, name, "", arrayListOf(phoneNumber), ArrayList(), ArrayList()
//                )
//                participants.add(contact)
//            }
//        }
//    }
//
//    private fun fixParticipantNumbers(
//        participants: ArrayList<SimpleContact>, properNumbers: ArrayList<String>
//    ): ArrayList<SimpleContact> {
//        for (number in properNumbers) {
//            for (participant in participants) {
//                participant.phoneNumbers = participant.phoneNumbers.map {
//                    val numberWithoutPlus = number.replace("+", "")
//                    if (numberWithoutPlus == it.normalizedNumber.trim()) {
//                        if (participant.name == it.normalizedNumber) {
//                            participant.name = number
//                        }
//                        PhoneNumber(number, 0, "", number)
//                    } else {
//                        PhoneNumber(it.normalizedNumber, 0, "", it.normalizedNumber)
//                    }
//                } as ArrayList<PhoneNumber>
//            }
//        }
//
//        return participants
//    }
//
//    private fun getPhoneNumbersFromIntent(): ArrayList<String> {
//        val numberFromIntent = intent.getStringExtra(THREAD_NUMBER)
//        val numbers = ArrayList<String>()
//
//        if (numberFromIntent != null) {
//            if (numberFromIntent.startsWith('[') && numberFromIntent.endsWith(']')) {
//                val type = object : TypeToken<List<String>>() {}.type
//                numbers.addAll(Gson().fromJson(numberFromIntent, type))
//            } else {
//                numbers.add(numberFromIntent)
//            }
//        }
//        return numbers
//    }
//
//    private fun sendNormalMessage(text: String, subscriptionId: Int) {
//        val addresses = participants.getAddresses()
//        val attachments = arrayListOf<Any>()
//
//        try {
//            val messagingUtils = MessagingUtils(this)
//            sendMessageCompat(text, addresses, subscriptionId, null, messagingUtils)
//            ensureBackgroundThread {
//                val messageIds = messages.map { it.id }
//                val messages = getMessages(
//                    threadId, limit = maxOf(1, attachments.size)
//                ).filter { it.id !in messageIds }
//
//
//                for (message in messages) {
//                    insertOrUpdateMessage(message)
//                }
//                if (isNewConversation) {
//                    val conversation = getConversations(threadId).firstOrNull()
//                    if (conversation != null) {
//                        viewModel.insertOrUpdateConversation(conversation)
//                    }
//                } else {
//                    val conversation = viewModel.getConversationByThreadId(threadId)
//                    if (conversation != null) {
//                        conversation.msgId = messages.first().id
//                        conversation.snippet = messages.first().body
//                        conversation.date = (System.currentTimeMillis() / 1000).toInt()
//                        conversation.read = messages.first().read
//                        viewModel.insertOrUpdateConversation(conversation)
//                    }
//                }
//            }
//            clearCurrentMessage()
//
//        } catch (e: Exception) {
//            showToast(e.message ?: "")
//        } catch (e: Error) {
//            showToast(e.localizedMessage ?: getString(R.string.unknown_error_occurred))
//        }
//    }
//
//    private fun insertOrUpdateMessage(message: Message) {
//        viewModel.insertOrUpdateMessages(message)
//    }
//
//    private fun insertOrUpdateScheduleMessage(message: ScheduleMessage) {
//        viewModel.insertOrUpdateScheduleMessage(message)
//    }
//
//    private fun clearCurrentMessage() {
//        binding.messageHolder.threadTypeMessage.setText("")
//    }
//
//    private fun scrollToBottom() {
//        val ll = binding.rvMessageList.layoutManager as LinearLayoutManager
//        ll.scrollToPositionWithOffset(messageAdapter.items.size - 1, 0)
//    }
//
//    private val bottomDataPosition: Int
//        get() = messageAdapter.items.size - 1
//
//    private fun manageMessage(): ArrayList<ThreadItem> {
////        val job = CoroutineScope(Dispatchers.IO).launch {
////            viewModel.markAsReadConversation(threadId)
////            messages.forEach {
////                if (!it.read) {
////                    markMessageRead(it.id, it.isMMS)
////                    viewModel.markAsReadMessage(it.id)
////                    viewModel.markAsReadConversation(threadId)
////                }
////            }
////        }
////        // Wait for the coroutine to complete
////        runBlocking {
////            job.join()
////        }
//        return getThreadItems()
//    }
//
//    private fun getThreadItems(): ArrayList<ThreadItem> {
//        val items = ArrayList<ThreadItem>()
//        if (isFinishing) {
//            return items
//        }
//
//        viewModel.markAsReadConversation(threadId)
//
//        messages.sortBy { it.date }
//        var prevDateTime = 0
//
//        val cnt = messages.size
//        for (i in 0 until cnt) {
//            val message = messages.getOrNull(i) ?: continue
//
//            if (!message.read) {
//                markMessageRead(message.id, message.isMMS)
//                viewModel.markAsReadMessage(message.id)
//            }
//            if (message.date - prevDateTime > MIN_DATE_TIME_DIFF_SECS) {
//                items.add(ThreadItem.ThreadDateTime(message.date, "1"))
//                prevDateTime = message.date
//            }
//            items.add(message)
//
//            if (message.type == Telephony.Sms.MESSAGE_TYPE_FAILED) {
//                items.add(ThreadItem.ThreadError(message.id, message.body))
//            }
//
//            if (message.type == Telephony.Sms.MESSAGE_TYPE_OUTBOX) {
//                items.add(ThreadItem.ThreadSending(message.id))
//            }
//
//        }
//        return items
//    }
//
//    override fun onBackPressed() {
//        detailScreenThreadId = -1
//        super.onBackPressed()
//    }
//
//    private fun checkIfAllMessageFetched() {
//        if (!isAllMessageFetched) {
//            CoroutineScope(Dispatchers.IO).launch {
//                val totalMsgCount = countMessagesInThread(threadId)
//                val localDbThreadMsgCount = viewModel.getStoredMessageCount(threadId)
//
//                if (totalMsgCount == localDbThreadMsgCount) {
//                    isAllMessageFetched = true
//                }
//                if (localDbThreadMsgCount < totalMsgCount) {
//                    runOnUiThread {
//                        binding.loader.visible()
//                    }
//                    var limit = localDbThreadMsgCount + 50
//                    if (limit >= totalMsgCount) {
//                        limit = totalMsgCount
//                    }
//                    viewModel.getMessageFromCursorByThreadId(
//                        threadId,-1, limit, localDbThreadMsgCount
//                    )
//                }
//            }
//        }
//    }
//
//}