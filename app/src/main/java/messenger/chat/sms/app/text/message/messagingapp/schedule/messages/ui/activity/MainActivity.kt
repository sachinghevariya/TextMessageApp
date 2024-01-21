//package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity
//
//import android.Manifest
//import android.app.Activity
//import android.app.ActivityManager
//import android.app.role.RoleManager
//import android.content.Context
//import android.content.Intent
//import android.graphics.Canvas
//import android.graphics.Color
//import android.os.Handler
//import android.os.Looper
//import android.provider.ContactsContract
//import android.provider.Telephony
//import android.view.View
//import android.view.inputmethod.InputMethodManager
//import android.widget.FrameLayout
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.coordinatorlayout.widget.CoordinatorLayout
//import androidx.core.content.ContextCompat
//import androidx.core.content.res.ResourcesCompat
//import androidx.core.view.GravityCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.chad.library.adapter4.dragswipe.QuickDragAndSwipe
//import com.chad.library.adapter4.dragswipe.QuickDragAndSwipe.SwipedView
//import com.chad.library.adapter4.dragswipe.listener.OnItemSwipeListener
//import com.google.android.material.behavior.SwipeDismissBehavior
//import com.google.android.material.snackbar.BaseTransientBottomBar
//import com.google.android.material.snackbar.Snackbar
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.MyApp
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityMainBinding
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.ConversationAdapterNew
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.addBlockedNumber
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markThreadMessagesRead
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markThreadMessagesUnread
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.updateConversationArchivedStatus
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateConversations
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSetting
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSwipeAction
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers.MyContactObserver
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_ID
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_TITLE
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isQPlus
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
//import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ConversationViewModel
//import org.greenrobot.eventbus.EventBus
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import plugin.adsdk.extras.NetworkChangeReceiver
//import plugin.adsdk.service.AdsUtility
//import plugin.adsdk.service.AppOpenManager
//
//@AndroidEntryPoint
//class MainActivity : BaseActivity<ActivityMainBinding>() {
//    private var backPressedTime: Long = 0
//    private val doubleBackToExit = 2000
//    private val viewModel: ConversationViewModel by viewModels()
//    lateinit var toggle: ActionBarDrawerToggle
//    private lateinit var conversationAdapter: ConversationAdapterNew
//    private var isAdShown = false
//    private var bindingAdapterPosition = -1
//    private val adModel = Conversation.getAdObject()
//    private val quickDragAndSwipe = QuickDragAndSwipe()
//        .setSwipeMoveFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
//
//    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)
//
//    override fun initData() {
//        bannerAd()
//        checkRunTimePermission(
//            Manifest.permission.READ_CONTACTS
//        ) {
//            checkAppDefault {
//                checkRunTimePermission(
//                    Manifest.permission.READ_SMS,
//                    Manifest.permission.SEND_SMS
//                ) {
//                    initFlow(false)
//                }
//            }
//        }
//        AppOpenManager.blockAppOpen(this)
//        hideView(false)
//        checkIfLastSync()
//        setUpAdapter()
//        setSupportActionBar(binding.toolBar)
//        setupDrawer()
//        manageMarquee()
//        viewClick()
//    }
//
//    private fun checkAppDefault(callback: () -> Unit) {
//        if (isQPlus()) {
//            val roleManager = getSystemService(RoleManager::class.java)
//            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
//                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
//                    callback()
//                } else {
//                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
//                    makeDefaultAppLauncher.launch(intent)
//                }
//            } else {
//                finish()
//            }
//        } else {
//            if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
//                callback()
//            } else {
//                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
//                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
//                makeDefaultAppLauncher.launch(intent)
//            }
//        }
//    }
//
//    private fun checkIfLastSync() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val listSyncDates = viewModel.getSyncData()
//            if (listSyncDates.isNotEmpty()) {
//                withContext(Dispatchers.Main) {
//                    initFlow(false)
//                }
//            }
//        }
//    }
//
//    private fun initFlow(isSyncDb: Boolean) {
//        CoroutineScope(Dispatchers.IO).launch {
//            CommonClass.blockedThreadIds = getBlockedThreadIds()
//        }
//        observerContactUpdate()
//        observeLiveData(isSyncDb)
//        observeConversationData()
//    }
//
//    private fun observeLiveData(isSyncDb: Boolean) {
//        lifecycleScope.launch {
//            viewModel.checkIfLastSync(isSyncDb) {
//                runOnUiThread {
//                    if (it) {
//                        hideView(false)
//                    } else {
//                        hideView(true)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun observeConversationData() {
//        lifecycleScope.launch {
//            viewModel.mRepository.dataContainer.progressLiveData.observe(this@MainActivity) {
//                if (it == 0) {
//                    hideView(false)
//                }
//                binding.conversationsPb.progress = it
//                if (it >= 100) {
//                    runOnUiThread {
//                        isAdShown = AdsUtility.isNetworkConnected(this@MainActivity)
//                        hideView(true)
//                    }
//                }
//            }
//
//            viewModel.getAllConversationsLiveData()
//                .observe(this@MainActivity) { conversations ->
//                    if (::conversationAdapter.isInitialized) {
//                        val newList = arrayListOf<Conversation>()
//                        if (conversations.isNotEmpty() && AdsUtility.isNetworkConnected(this@MainActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
//                            newList.add(0, adModel)
//                        }
//                        newList.addAll(conversations)
//                        if (MyApp.isNewMessageArrived) {
//                            MyApp.isNewMessageArrived = false
//                            conversationAdapter.submitList(newList)
//                            /*try {
//                                binding.rvConversations.smoothScrollToPosition(0)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }*/
//                        } else {
//                            if (newList.isEmpty()) {
//                                binding.ivScrollUp.gone()
//                            }
//                            conversationAdapter.submitList(newList) {}
//                        }
//                    }
//                }
//        }
//    }
//
//    private fun setupDrawer() {
//        toggle = object : ActionBarDrawerToggle(
//            this,
//            binding.drawerLayout,
//            binding.toolBar,
//            R.string.navigation_drawer_open,
//            R.string.navigation_drawer_close
//        ) {
//            override fun onDrawerClosed(drawerView: View) {
//                // Triggered once the drawer closes
//                super.onDrawerClosed(drawerView)
//                try {
//                    val inputMethodManager =
//                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
//                } catch (e: Exception) {
//                    e.stackTrace
//                }
//            }
//
//            override fun onDrawerOpened(drawerView: View) {
//                // Triggered once the drawer opens
//                super.onDrawerOpened(drawerView)
//                try {
//                    val inputMethodManager =
//                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
//                } catch (e: Exception) {
//                    e.stackTrace
//                }
//            }
//        }
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.isDrawerIndicatorEnabled = false
//        toggle.toolbarNavigationClickListener =
//            View.OnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
//        actionBar?.setDisplayHomeAsUpEnabled(true)
//        actionBar?.setHomeButtonEnabled(true)
//        toggle.syncState()
////        val typedValue = TypedValue()
////        theme.resolveAttribute(R.attr.iconColor, typedValue, true)
////        val iconColor = typedValue.data
//        val iconColor = ContextCompat.getColor(this, R.color.iconColor)
//        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu, theme)
//        drawable?.setTint(iconColor)
//        toggle.setHomeAsUpIndicator(drawable)
//    }
//
//    private fun closeDrawer() {
//        binding.drawerLayout.closeDrawer(GravityCompat.START)
//    }
//
//    private fun setUpAdapter() {
//        binding.rvConversations.layoutManager =
//            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        /*conversationAdapter = ConversationAdapter(
//            this, myPreferences,
//        ) { item, deleteEvent, archiveEvent, pinEvent, unPinEvent, markAsReadEvent, markAsUnReadEvent, blockEvent, _, selectedItems ->
//            if (item != null) {
//                startActivity(
//                    Intent(this, ConversationDetailActivity::class.java)
//                        .putExtra(THREAD_ID, item.threadId)
//                        .putExtra(THREAD_TITLE, item.title)
//                )
//            } else if (deleteEvent) {
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val baseString = R.string.deletion_confirmation
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog(
//                    this,
//                    question,
//                    positive = getString(R.string.delete),
//                    dialogTitle = getString(R.string.delete_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        deleteConversations(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            } else if (archiveEvent) {
//                val baseString = R.string.archive_confirmation
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog(
//                    this,
//                    question,
//                    positive = getString(R.string.ok),
//                    dialogTitle = getString(R.string.archive_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        selectedItems.forEach {
//                            bindingAdapterPosition =
//                                conversationAdapter.items.indexOf(it)
//                            if (bindingAdapterPosition == 0) {
//                                return@forEach
//                            }
//                        }
//                        archiveConversations(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            } else if (pinEvent) {
//                pinConversation(true, selectedItems)
//            } else if (unPinEvent) {
//                pinConversation(false, selectedItems)
//            } else if (markAsReadEvent) {
//                markAsRead(selectedItems)
//            } else if (markAsUnReadEvent) {
//                markAsUnread(selectedItems)
//            } else if (blockEvent) {
//                val baseString = R.string.block_confirmation
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog(
//                    this,
//                    question,
//                    positive = getString(R.string.ok),
//                    dialogTitle = getString(R.string.block_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        blockConversation(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            }
//        }*/
//
//        conversationAdapter = ConversationAdapterNew(
//            this, myPreferences, actionCallback = {
//                setupSwipeActions()
//            }
//        ) { item, deleteEvent, archiveEvent, pinEvent, unPinEvent, markAsReadEvent, markAsUnReadEvent, blockEvent, _, selectedItems ->
//            if (item != null) {
//                startActivity(
//                    Intent(this, ConversationDetailActivityNew::class.java)
//                        .putExtra(THREAD_ID, item.threadId)
//                        .putExtra(THREAD_TITLE, item.title)
//                )
//            } else if (deleteEvent) {
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val baseString = R.string.deletion_confirmation
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog.newInstance(
//                    this,
//                    question,
//                    positive = getString(R.string.delete),
//                    dialogTitle = getString(R.string.delete_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        deleteConversations(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            } else if (archiveEvent) {
//                val baseString = R.string.archive_confirmation
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog.newInstance(
//                    this,
//                    question,
//                    positive = getString(R.string.ok),
//                    dialogTitle = getString(R.string.archive_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        selectedItems.forEach {
//                            bindingAdapterPosition =
//                                conversationAdapter.items.indexOf(it)
//                            if (bindingAdapterPosition == 0) {
//                                return@forEach
//                            }
//                        }
//                        archiveConversations(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            } else if (pinEvent) {
//                pinConversation(true, selectedItems)
//            } else if (unPinEvent) {
//                pinConversation(false, selectedItems)
//            } else if (markAsReadEvent) {
//                markAsRead(selectedItems)
//            } else if (markAsUnReadEvent) {
//                markAsUnread(selectedItems)
//            } else if (blockEvent) {
//                val baseString = R.string.block_confirmation
//                val itemsCnt = selectedItems.size
//                val items = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val question = String.format(resources.getString(baseString), items)
//                ConfirmationDialog.newInstance(
//                    this,
//                    question,
//                    positive = getString(R.string.ok),
//                    dialogTitle = getString(R.string.block_msg)
//                ) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        blockConversation(selectedItems)
//                        withContext(Dispatchers.Main) {
//                            conversationAdapter.clearSelection()
//                        }
//                    }
//                }
//            }
//        }
//
//        setupSwipeActions()
//
//        binding.rvConversations.adapter = conversationAdapter
//
//        val scrollListener = MyScrollListener(object : OnScrollListener {
//            override fun onScrolledUp() {}
//
//            override fun onScrolledDown() {
//                binding.ivScrollUp.visible()
//            }
//
//            override fun onReachedBottom() {}
//
//            override fun onReachedTop() {
//                binding.ivScrollUp.gone()
//            }
//        })
//        binding.rvConversations.addOnScrollListener(scrollListener)
//
//    }
//
//    private fun setupSwipeActions() {
//        var swipe = true
//
//        val swipeListener: OnItemSwipeListener = object : OnItemSwipeListener {
//            override fun onItemSwipeStart(
//                viewHolder: RecyclerView.ViewHolder?,
//                bindingAdapterPosition: Int
//            ) {
//            }
//
//            override fun onItemSwipeEnd(
//                viewHolder: RecyclerView.ViewHolder,
//                bindingAdapterPosition: Int
//            ) {
//            }
//
//            override fun onItemSwiped(
//                viewHolder: RecyclerView.ViewHolder,
//                direction: Int,
//                bindingAdapterPosition: Int
//            ) {
//                this@MainActivity.bindingAdapterPosition = bindingAdapterPosition
//                val item = conversationAdapter.items[bindingAdapterPosition]
//                manageSwipeAction(direction, listOf(item), bindingAdapterPosition)
//            }
//
//            override fun onItemSwipeMoving(
//                canvas: Canvas,
//                viewHolder: RecyclerView.ViewHolder,
//                dX: Float,
//                dY: Float,
//                isCurrentlyActive: Boolean
//            ) {
//            }
//        }
//
//        val leftSwipe = myPreferences.swipeLeftActionLabel != "swipeNone"
//        val rightSwipe = myPreferences.swipeRightActionLabel != "swipeNone"
//
//        if (conversationAdapter.isSelectionMode) {
//            swipe = false
//        } else {
//            swipe = true
//            if (!leftSwipe && !rightSwipe) {
//                swipe = false
//            }
//        }
//
//        quickDragAndSwipe.attachToRecyclerView(binding.rvConversations)
//            .setDataCallback(conversationAdapter)
//            .setItemSwipeListener(swipeListener)
//            .setItemViewSwipeEnabled(swipe)
//            .setItemViewLeftSwipeEnabled(leftSwipe)
//            .setItemViewRightSwipeEnabled(rightSwipe)
//            .setSwipeView(createSwipedView())
//    }
//
//    private fun getSwipeActionDefaultIcons(isRightAction: Boolean): Int {
//        val defaultAction = if (isRightAction) {
//            myPreferences.swipeRightActionLabel
//        } else {
//            myPreferences.swipeLeftActionLabel
//        }
//
//        when (defaultAction) {
//            "swipeArchive" -> {
//                return R.drawable.ic_archive
//            }
//
//            "swipeDelete" -> {
//                return R.drawable.ic_delete
//            }
//
//            "swipeMarkRead" -> {
//                return R.drawable.ic_mark_read
//            }
//
//            "swipeMarkUnRead" -> {
//                return R.drawable.ic_mark_unread
//            }
//
//            "swipeNone" -> {
//                return R.drawable.ic_block
//            }
//        }
//        return R.drawable.ic_archive
//    }
//
//    private fun createSwipedView(): SwipedView {
//
//        val mSwipedView = SwipedView()
//
//        mSwipedView.setBackrounds(
//            intArrayOf(
//                R.color.colorAccent,
//                R.color.colorAccent
//            )
//        )
//        mSwipedView.setIcons(
//            intArrayOf(
//                getSwipeActionDefaultIcons(true),
//                getSwipeActionDefaultIcons(false)
//            )
//        )
//        mSwipedView.setTexts(
//            arrayOf<String?>(
//                "",
//                ""
//            )
//        )
//        mSwipedView.setTextColorView(
//            Color.WHITE
//        )
//        mSwipedView.setTextSizeView(
//            15
//        )
//
//        return mSwipedView
//    }
//
//    private fun manageSwipeAction(
//        direction: Int,
//        items: List<Conversation>,
//        bindingAdapterPosition: Int
//    ) {
//        val defaultAction = if (direction == ItemTouchHelper.RIGHT) {
//            myPreferences.swipeRightActionLabel
//        } else {
//            myPreferences.swipeLeftActionLabel
//        }
//
//        when (defaultAction) {
//            "swipeArchive" -> {
//                archiveConversations(items)
//            }
//
//            "swipeDelete" -> {
//                /*val itemsCnt = items.size
//                val itemsN = resources.getQuantityString(
//                    R.plurals.delete_conversations, itemsCnt, itemsCnt
//                )
//                val baseString = R.string.deletion_confirmation
//                val question = String.format(resources.getString(baseString), itemsN)
//                ConfirmationDialog(
//                    this@MainActivity,
//                    question,
//                    positive = getString(R.string.delete),
//                    dialogTitle = getString(R.string.delete_msg)
//                ) {
//
//                }*/
//                CoroutineScope(Dispatchers.IO).launch {
//                    withContext(Dispatchers.Main) {
//                        showSnackBar(items, true)
//                    }
//                    deleteConversations(items)
//                    withContext(Dispatchers.Main) {
//                        conversationAdapter.clearSelection()
//                    }
//                }
//            }
//
//            "swipeMarkRead" -> {
//                markAsRead(items)
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (bindingAdapterPosition == 0) {
//                        scrollToTop()
//                    }
//                }, 300)
//            }
//
//            "swipeMarkUnRead" -> {
//                markAsUnread(items)
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (bindingAdapterPosition == 0) {
//                        scrollToTop()
//                    }
//                }, 300)
//            }
//
//            "swipeNone" -> {
//
//            }
//        }
//    }
//
//    private fun blockConversation(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                addBlockedNumber(it.phoneNumber)
//                viewModel.addToBlockConversation(it.threadId)
//            }
//        }
//
//    }
//
//    private fun markAsRead(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                markThreadMessagesRead(it.threadId)
//                viewModel.markAsReadConversation(it.threadId)
//                viewModel.markAllMessageThreadRead(it.threadId)
//            }
//        }
//        conversationAdapter.clearSelection()
//    }
//
//    private fun markAsUnread(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                markThreadMessagesUnread(it.threadId)
//                viewModel.markAsUnReadConversation(it.threadId)
//                viewModel.markAsUnReadMessage(it.msgId)
//            }
//        }
//        conversationAdapter.clearSelection()
//    }
//
//    private fun pinConversation(pin: Boolean, selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                if (pin) {
//                    viewModel.pinConversation(it.threadId)
//                } else {
//                    viewModel.unPinConversation(it.threadId)
//                }
//            }
//        }
//
//    }
//
//    private fun archiveConversations(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                updateConversationArchivedStatus(it.threadId, true)
//                viewModel.moveToArchive(it.threadId)
//                notificationManager.cancel(it.threadId.toInt())
//            }
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            showSnackBar(selectedItems, false)
//            conversationAdapter.clearSelection()
//        }
//    }
//
//    private fun unArchiveConversations(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                updateConversationArchivedStatus(it.threadId, true)
//                viewModel.moveToUnArchive(it.threadId)
//                notificationManager.cancel(it.threadId.toInt())
//            }
//        }
//    }
//
//    private fun showSnackBar(selectedItems: List<Conversation>, isDeleteEvent: Boolean) {
//        binding.swipeAbleView.visible()
//        var msg = ""
//        msg = if (isDeleteEvent) {
//            resources.getQuantityString(
//                R.plurals.delete_conversations, selectedItems.size, selectedItems.size
//            ) + " " + getString(R.string.delete)
//        } else {
//            resources.getQuantityString(
//                R.plurals.delete_conversations, selectedItems.size, selectedItems.size
//            ) + " " + getString(R.string.archives)
//        }
//
//        val snackBar = Snackbar.make(
//            binding.swipeAbleView, msg, Snackbar.LENGTH_LONG
//        )
//
//        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
//        val behavior = SwipeDismissBehavior<View>()
//        behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
//        params.behavior = behavior
//        snackBar.view.layoutParams = params
//        snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
//            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
//                if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_MANUAL) {
//                    binding.swipeAbleView.gone()
//                }
//            }
//        })
//
//        if (isDeleteEvent) {
//
//        } else {
//            snackBar.setAction(getString(R.string.undo)) {
//                snackBar.dismiss()
//                unArchiveConversations(selectedItems)
//                Handler(Looper.getMainLooper()).postDelayed({
//                    if (bindingAdapterPosition == 0) {
//                        scrollToTop()
//                    }
//                }, 300)
//            }
//        }
//
//        snackBar.show()
//    }
//
//    private fun deleteConversations(selectedItems: List<Conversation>) {
//        if (selectedItems.isEmpty()) {
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                deleteConversation(it.threadId)
//                viewModel.deleteConversationByThreadId(it.threadId)
//                viewModel.deleteMessageByThreadId(it.threadId)
//                notificationManager.cancel(it.threadId.toInt())
//            }
//        }
//    }
//
//    private fun viewClick() {
//        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//        binding.All.setOnClickListener(1000L) {
//            viewModel.updateCategoryFilter(MessageType.ALL)
//        }
//        binding.Otp.setOnClickListener(1000L) {
//            viewModel.updateCategoryFilter(MessageType.OTP)
//        }
//        binding.Offers.setOnClickListener(1000L) {
//            viewModel.updateCategoryFilter(MessageType.OFFER)
//        }
//        binding.Personal.setOnClickListener(1000L) {
//            viewModel.updateCategoryFilter(MessageType.OTHER)
//        }
//        binding.Transaction.setOnClickListener(1000L) {
//            viewModel.updateCategoryFilter(MessageType.TRANSACTION)
//        }
//        binding.conversationsFab.setOnClickListener(1000L) {
//            checkRunTimePermission(
//                Manifest.permission.READ_CONTACTS
//            ) {
//                startActivity(Intent(this@MainActivity, NewConversationActivity::class.java))
//            }
//        }
//        binding.btnSearch.setOnClickListener(1000L) {
//            checkRunTimePermission(
//                Manifest.permission.READ_CONTACTS
//            ) {
//                startActivity(Intent(this, SearchActivity::class.java))
//            }
//        }
//        binding.settingsButton.setOnClickListener(1000L) {
//            startActivity(Intent(this, SettingActivity::class.java))
//        }
//        binding.ivScrollUp.setOnClickListener(1000L) {
//            scrollToTop()
//            binding.ivScrollUp.gone()
//        }
//
//
//        //----------------------------DrawerItemClick-----------------------------
//        binding.btnUnreadMessage.setOnClickListener(1000L) {
//            closeDrawer()
//        }
//        binding.btnPrivateBox.setOnClickListener(1000L) {
//            closeDrawer()
//        }
//        binding.btnArchive.setOnClickListener(1000L) {
//            closeDrawer()
//            startActivity(Intent(this@MainActivity, ArchivedActivity::class.java))
//        }
//        binding.btnSchedule.setOnClickListener(1000L) {
//            closeDrawer()
//            startActivity(Intent(this, ScheduledActivity::class.java))
//        }
//        binding.btnStared.setOnClickListener(1000L) {
//            closeDrawer()
//            startActivity(Intent(this, StarredActivity::class.java))
//        }
//        binding.btnBlock.setOnClickListener(1000L) {
//            closeDrawer()
//            startActivity(Intent(this, BlockListActivity::class.java))
//        }
//        binding.btnBackup.setOnClickListener(1000L) {
//            startActivity(Intent(this@MainActivity, BackupRestoreActivity::class.java))
//            closeDrawer()
//        }
//        binding.btnSwipeAction.setOnClickListener(1000L) {
//            startActivity(Intent(this@MainActivity, SwipeActionsActivity::class.java))
//            closeDrawer()
//        }
//        binding.btnPrivacy.setOnClickListener(1000L) {
//            closeDrawer()
//            AdsUtility.privacyPolicy(this)
//        }
//        binding.btnShare.setOnClickListener(1000L) {
//            closeDrawer()
//            AdsUtility.shareApp(this)
//        }
//        binding.btnRateUs.setOnClickListener(1000L) {
//            closeDrawer()
//            AdsUtility.rateUs(this)
//        }
//    }
//
//    private fun scrollToTop() {
//        val ll = binding.rvConversations.layoutManager as LinearLayoutManager
//        ll.scrollToPositionWithOffset(0, 0)
//    }
//
//    private fun manageMarquee() {
//        binding.search.isSelected = true
//        binding.tvChat.isSelected = true
//        binding.tvHeader.isSelected = true
//        binding.tvUnreadMsg.isSelected = true
//        binding.tvPrivateBox.isSelected = true
//        binding.tvArchive.isSelected = true
//        binding.tvSchedule.isSelected = true
//        binding.tvBlock.isSelected = true
//        binding.tvStarred.isSelected = true
//        binding.tvBackup.isSelected = true
//        binding.tvSwipeAction.isSelected = true
//        binding.tvMoreOption.isSelected = true
//        binding.tvPrivacy.isSelected = true
//        binding.tvInvite.isSelected = true
//        binding.tvRateUs.isSelected = true
//    }
//
//    private fun observerContactUpdate() {
//        checkRunTimePermission(
//            Manifest.permission.READ_CONTACTS
//        ) {
//            val contentResolver = contentResolver
//            contentResolver.registerContentObserver(
//                ContactsContract.Contacts.CONTENT_URI, true, MyContactObserver(this, viewModel)
//            )
//        }
//    }
//
//    private fun hideView(isVisible: Boolean) {
//        if (isVisible) {
//            binding.searchView.gone()
//            binding.btnSearch.visible()
//            binding.rvConversations.visible()
//            binding.conversationsFab.visible()
//            binding.settingsButton.visible()
////            if (AdsUtility.config.adMob.bannerAd.isNotEmpty()) showAd()
//            binding.containerProgress.gone()
//        } else {
//            binding.searchView.gone()
//            binding.containerProgress.visible()
////            binding.btnSearch.gone()
////            binding.rvConversations.gone()
////            binding.conversationsFab.gone()
////            binding.settingsButton.gone()
////            hideAd()
//        }
//
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onUpdateSetting(event: UpdateSetting) {
//        conversationAdapter.notifyDataSetChanged()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onUpdateConversations(event: UpdateConversations) {
//        observeConversationData()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onUpdateSwipeAction(event: UpdateSwipeAction) {
//        setupSwipeActions()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        EventBus.getDefault().unregister(this)
//    }
//
//    private val makeDefaultAppLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                myPreferences.setDefaultApp = true
//                if (myPreferences.lastUpdateDbTime == 0L) {
//                    initFlow(true)
//                } else {
//                    initFlow(false)
//                }
//            } else {
//                finish()
//            }
//        }
//
//    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
//        super.networkStateChanged(state)
//        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
//            showAd()
//        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
//            hideAd()
//        }
//    }
//
//    private fun hideAd() {
//        runOnUiThread {
//            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
//        }
//    }
//
//    private fun showAd() {
//        runOnUiThread {
//            if (AdsUtility.isNetworkConnected(this@MainActivity)) binding.root.findViewById<FrameLayout>(
//                R.id.banner_ad_container
//            ).visible()
//        }
//    }
//
//    override fun onBackPressed() {
//        if (backPressedTime + doubleBackToExit > System.currentTimeMillis()) {
//            super.onBackPressed() // If the time interval has passed, exit the app
//        } else {
//            showToast(getString(R.string.exit_app))
//        }
//
//        backPressedTime = System.currentTimeMillis()
//    }
//
//}