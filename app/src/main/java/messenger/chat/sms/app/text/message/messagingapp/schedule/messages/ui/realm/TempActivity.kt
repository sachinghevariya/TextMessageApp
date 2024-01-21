package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.dragswipe.QuickDragAndSwipe
import com.chad.library.adapter4.dragswipe.listener.OnItemSwipeListener
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ActivityTempBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.ArchivedActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.BackupRestoreActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.BlockListActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.ConversationDetailActivityNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.NewConversationActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.ScheduledActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.SearchActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.SettingActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.StarredActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.activity.SwipeActionsActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseActivity
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.database.MessagesDatabase
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog.ConfirmationDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.addBlockedNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.deleteConversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getBlockedThreadIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markThreadMessagesRead
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.markThreadMessagesUnread
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.updateConversationArchivedStatus
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateConversations
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSetting
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateSwipeAction
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers.MyContactObserver
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.DB_NAME
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.OnScrollListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isQPlus
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.notificationManager
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ConversationViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import plugin.adsdk.extras.NetworkChangeReceiver
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.AppOpenManager

@AndroidEntryPoint
class TempActivity : BaseActivity<ActivityTempBinding>() {

    private val makeDefaultAppLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                manageSetDefaultView(true)
                myPreferences.setDefaultApp = true
                /*if (myPreferences.lastUpdateDbTime == 0L) {
                    initFlow(true)
                } else {
                    initFlow(false)
                }*/
                initFlow(true)
                observerContactUpdate()
            } else {
                manageSetDefaultView(false)
//                finish()
            }
        }

    private var backPressedTime: Long = 0
    private val doubleBackToExit = 2000
    private val viewModel: ConversationViewModel by viewModels()
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var conversationAdapter: ConversationAdapterNewNew
    private var bindingAdapterPosition = -1
    private var isAdShown = false

    private val quickDragAndSwipe =
        QuickDragAndSwipe().setSwipeMoveFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

    override fun getViewBinding() = ActivityTempBinding.inflate(layoutInflater)

    override fun initData() {
//        bannerAd()
//        nativeAdSmall()
        binding.containerProgress.gone()

        AppOpenManager.blockAppOpen(this)
        hideView(false)
        checkIfLastSync()
        setUpAdapter()
        setSupportActionBar(binding.toolBar)
        setupDrawer()
        manageMarquee()
        viewClick()
        loadData()
    }

    private fun checkIfLastSync() {
        CoroutineScope(Dispatchers.IO).launch {
            initFlow(false)
        }
    }

    private fun manageSetDefaultView(isEnable: Boolean) {
        if (isEnable) {
            binding.btnSearch.isEnabled = true
            toggle.toolbarNavigationClickListener =
                View.OnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
            binding.containerPermission.gone()
            binding.containerView.visible()
        } else {
            binding.btnSearch.isEnabled = false
            toggle.toolbarNavigationClickListener = null
            binding.containerPermission.visible()
            binding.containerView.gone()
        }
    }

    private fun checkAppDefault(callback: () -> Unit) {
        if (isQPlus()) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    manageSetDefaultView(true)
                    callback()
                } else {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    makeDefaultAppLauncher.launch(intent)
                }
            } else {
                manageSetDefaultView(false)
//                finish()
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(this) == packageName) {
                manageSetDefaultView(true)
                callback()
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                makeDefaultAppLauncher.launch(intent)
            }
        }
    }

    private fun initFlow(syncDb: Boolean) {
        getRealmThread { realm ->
            val list = realm
                .where(ConversationNew::class.java)
                .notEqualTo("id", 0L)
                .equalTo("archived", false)
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
            conversationAdapter.updateData(list)
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.containerProgress.gone()
        }

        CoroutineScope(Dispatchers.IO).launch {
            CommonClass.blockedThreadIds = getBlockedThreadIds()
        }

        if (syncDb) {
            syncMessages()
        }

    }

    private suspend fun checkIfRoomDataExist() {
        withContext(Dispatchers.IO) {
            try {
                val roomDatabaseExists: Boolean = getDatabasePath(DB_NAME).exists()

                if (roomDatabaseExists) {
                    val list =
                        MessagesDatabase.getInstance(this@TempActivity).getStarredMessageDao()
                            .getStarredMessagesLocal()

                    val conversationArchived =
                        MessagesDatabase.getInstance(this@TempActivity).getConversationsDao()
                            .getAllArchivedConversationsData()

                    val conversationIdList = arrayListOf<Long>()
                    conversationArchived.forEach {
                        conversationIdList.add(it.threadId)
                    }

                    val msgIdList = arrayListOf<Long>()
                    list.forEach {
                        msgIdList.add(it.id)
                    }

                    getRealmThread { realm ->
                        val messages =
                            realm.where(MessageNew::class.java)
                                .anyOf("contentId", msgIdList.toLongArray())
                                .sort("date", Sort.DESCENDING).findAll()

                        realm.executeTransaction {
                            messages.forEach { msg ->
                                msg.isStarred = true
                            }
                        }

                        val conversationArchivedList = realm.where(ConversationNew::class.java)
                            .anyOf("id", conversationIdList.toLongArray()).findAllAsync()

                        realm.executeTransaction {
                            conversationArchivedList.forEach { conversation ->
                                conversation.archived = true
                            }
                        }
                    }

                    deleteDatabase(DB_NAME)
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*   private fun checkIfRoomDataExist() {
           CoroutineScope(Dispatchers.IO).launch {
               try {
                   val roomDatabaseExists: Boolean = getDatabasePath(DB_NAME).exists()
                   if (roomDatabaseExists) {
                       val list =
                           MessagesDatabase.getInstance(this@TempActivity).getStarredMessageDao()
                               .getStarredMessagesLocal()
                       val conversationArchived =
                           MessagesDatabase.getInstance(this@TempActivity).getConversationsDao()
                               .getAllArchivedConversationsData()
                       Log.e("TAG", "checkIfRoomDataExist: ")

                       val conversationIdList = arrayListOf<Long>()
                       conversationArchived.forEach {
                           conversationIdList.add(it.threadId)
                       }
                       val msgIdList = arrayListOf<Long>()
                       list.forEach {
                           msgIdList.add(it.id)
                       }

                       getRealmThread { realm ->
                           val messages =
                               realm.where(MessageNew::class.java)
                                   .anyOf("contentId", msgIdList.toLongArray())
                                   .sort("date", Sort.DESCENDING).findAll()
                           realm.executeTransaction {
                               messages.forEach { msg ->
                                   msg.isStarred = true
                               }
                           }

                           val conversationArchivedList = realm.where(ConversationNew::class.java)
                               .anyOf("id", conversationIdList.toLongArray()).findAllAsync()
                           realm.executeTransaction {
                               conversationArchivedList.forEach { conversation ->
                                   conversation.archived = true
                               }
                           }

                       }

                       deleteDatabase(DB_NAME)
                   }
                   getRealmThread { realm ->
                       val list = realm.where(ConversationNew::class.java)
                           .anyOf("id", CommonClass.blockedThreadIds.toLongArray()).findAllAsync()

                       realm.executeTransaction {
                           list.forEach { conversation ->
                               conversation.blocked = true
                           }
                       }
                   }
               } catch (e: Exception) {
                   e.printStackTrace()
               }
           }
       }*/

    private fun syncMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.containerProgress.visible()
            }
            Log.e("TAG", "syncMessages:Start ${System.currentTimeMillis()}")
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            val persistedData = realm.copyFromRealm(
                realm.where(ConversationNew::class.java).beginGroup().equalTo("archived", true).or()
                    .equalTo("blocked", true).or().equalTo("pinned", true).or().isNotEmpty("name")
                    .or().isNotNull("blockingClient").or().isNotEmpty("blockReason").endGroup()
                    .findAll()
            ).associateBy { conversation -> conversation.id }.toMutableMap()


            realm.delete(Contact::class.java)
            realm.delete(ConversationNew::class.java)
            realm.delete(MessageNew::class.java)
            realm.delete(Recipient::class.java)
            realm.deleteAll()

            val cursorToMessage =
                CursorToMessageImpl(this@TempActivity, KeyManagerImpl.newInstance())
            val cursorToConversation = CursorToConversationImpl(this@TempActivity)
            val cursorToRecipient = CursorToRecipientImpl(this@TempActivity)

            val messageCursor = cursorToMessage.getMessagesCursor()
            Log.e("TAG", "messageCursor-count: ${messageCursor?.count}")
            val conversationCursor = cursorToConversation.getConversationsCursor()
            val recipientCursor = cursorToRecipient.getRecipientCursor()

            val max = (messageCursor?.count ?: 0) + (conversationCursor?.count
                ?: 0) + (recipientCursor?.count ?: 0)

            withContext(Dispatchers.Main) {
                binding.conversationsPb.max = max
            }

            var progress = 0


            messageCursor?.use {
                val messageColumns = CursorToMessage.MessageColumns(messageCursor)
                messageCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.conversationsPb.progress = progress
                        }
                        val message = cursorToMessage.map(Pair(cursor, messageColumns))
                        realm.insertOrUpdate(message)
                    }
                }
            }


            conversationCursor?.use {
                conversationCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.conversationsPb.progress = progress
                        }
                        val conversation = cursorToConversation.map(cursor).apply {
                            persistedData[id]?.let { persistedConversation ->
                                archived = persistedConversation.archived
                                blocked = persistedConversation.blocked
                                pinned = persistedConversation.pinned
                                name = persistedConversation.name
                                blockingClient = persistedConversation.blockingClient
                                blockReason = persistedConversation.blockReason
                            }
                            lastMessage =
                                realm.where(MessageNew::class.java).sort("date", Sort.DESCENDING)
                                    .equalTo("threadId", id).findFirst()
                        }
                        realm.insertOrUpdate(conversation)
                    }
                }
            }

            val phoneNumberUtils = PhoneNumberUtils(this@TempActivity)
            // Sync recipients
            recipientCursor?.use {
                val contacts = realm.copyToRealmOrUpdate(getContacts())
                recipientCursor.forEach { cursor ->
                    tryOrNull {
                        progress++
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.conversationsPb.progress = progress
                        }
                        val recipient = cursorToRecipient.map(cursor).apply {
                            contact = contacts.firstOrNull { contact ->
                                contact.numbers.any {
                                    phoneNumberUtils.compare(
                                        address, it.address
                                    )
                                }
                            }
                        }
                        realm.insertOrUpdate(recipient)
                    }
                }
            }

            realm.insert(SyncLog())
            realm.commitTransaction()
            realm.close()

            CoroutineScope(Dispatchers.Main).launch {
                binding.containerProgress.gone()
            }
            Log.e("TAG", "syncMessages:End ${System.currentTimeMillis()}")

            try {
                getRealmThread { realm ->
                    val list = realm.where(ConversationNew::class.java)
                        .anyOf("id", CommonClass.blockedThreadIds.toLongArray()).findAllAsync()

                    realm.executeTransaction {
                        list.forEach { conversation ->
                            conversation.blocked = true
                        }
                    }
                }
            } catch (e: Exception) {
            }
            CoroutineScope(Dispatchers.IO).launch {
                checkIfRoomDataExist()
            }

//            addAdModelInDb()
        }
    }

    private fun getContacts(): List<Contact> {
        val defaultNumberIds = Realm.getDefaultInstance().use { realm ->
            realm.where(PhoneNumber::class.java).equalTo("isDefault", true).findAll()
                .map { number -> number.id }
        }

        val phoneNumberUtils = PhoneNumberUtils(this)
        val cursorToContact = CursorToContactImpl(this)
        return cursorToContact.getContactsCursor()?.map { cursor -> cursorToContact.map(cursor) }
            ?.groupBy { contact -> contact.lookupKey }?.map { contacts ->
                // Sometimes, contacts providers on the phone will create duplicate phone number entries. This
                // commonly happens with Whatsapp. Let's try to detect these duplicate entries and filter them out
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contacts.value.flatMap { it.numbers }.forEach { number ->
                    number.isDefault = defaultNumberIds.any { id -> id == number.id }
                    val duplicate = uniqueNumbers.find { other ->
                        phoneNumberUtils.compare(number.address, other.address)
                    }

                    if (duplicate == null) {
                        uniqueNumbers += number
                    } else if (!duplicate.isDefault && number.isDefault) {
                        duplicate.isDefault = true
                    }
                }

                contacts.value.first().apply {
                    numbers.clear()
                    numbers.addAll(uniqueNumbers)
                }
            } ?: listOf()
    }

    private fun setupDrawer() {
        toggle = object : ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                // Triggered once the drawer closes
                super.onDrawerClosed(drawerView)
                try {
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                } catch (e: Exception) {
                    e.stackTrace
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                // Triggered once the drawer opens
                super.onDrawerOpened(drawerView)
                try {
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                } catch (e: Exception) {
                    e.stackTrace
                }
            }
        }
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = false
        toggle.toolbarNavigationClickListener =
            View.OnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        toggle.syncState()
//        val typedValue = TypedValue()
//        theme.resolveAttribute(R.attr.iconColor, typedValue, true)
//        val iconColor = typedValue.data
        val iconColor = ContextCompat.getColor(this, R.color.iconColor)
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu, theme)
        drawable?.setTint(iconColor)
        toggle.setHomeAsUpIndicator(drawable)
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun setUpAdapter() {
        binding.rvConversations.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        conversationAdapter = ConversationAdapterNewNew(
            this,
            myPreferences,
            phoneNumberUtils = PhoneNumberUtils(this),
            actionCallback = {
                setupSwipeActions()
            }) { item, deleteEvent, archiveEvent, pinEvent, unPinEvent, markAsReadEvent, markAsUnReadEvent, blockEvent, _, selectedItems ->
            if (item != null) {
                startActivity(
                    Intent(
                        this,
                        ConversationDetailActivityNew::class.java
                    ).putExtra(CommonClass.THREAD_ID, item.id)
                        .putExtra(CommonClass.THREAD_TITLE, item.getTitle())
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
                    deleteConversations(selectedItems)
                    conversationAdapter.clearSelection()
                }
            } else if (archiveEvent) {
                val baseString = R.string.archive_confirmation
                val itemsCnt = selectedItems.size
                val items = resources.getQuantityString(
                    R.plurals.delete_conversations, itemsCnt, itemsCnt
                )
                val question = String.format(resources.getString(baseString), items)
                ConfirmationDialog.newInstance(
                    this,
                    question,
                    positive = getString(R.string.ok),
                    dialogTitle = getString(R.string.archive_msg)
                ) {
                    selectedItems.forEach {
                        bindingAdapterPosition = conversationAdapter.data?.indexOf(it)!!
                        if (bindingAdapterPosition == 0) {
                            return@forEach
                        }
                    }
                    archiveConversations(selectedItems)
                    conversationAdapter.clearSelection()
                    /*CoroutineScope(Dispatchers.IO).launch {
                        selectedItems.forEach {
                            bindingAdapterPosition = conversationAdapter.data?.indexOf(it)!!
                            if (bindingAdapterPosition == 0) {
                                return@forEach
                            }
                        }
                        withContext(Dispatchers.Main) {
                            archiveConversations(selectedItems)
                            conversationAdapter.clearSelection()
                        }
                    }*/
                }
            } else if (pinEvent) {
                pinConversation(true, selectedItems)
            } else if (unPinEvent) {
                pinConversation(false, selectedItems)
            } else if (markAsReadEvent) {
                markAsRead(selectedItems)
            } else if (markAsUnReadEvent) {
                markAsUnread(selectedItems)
            } else if (blockEvent) {
                val baseString = R.string.block_confirmation
                val itemsCnt = selectedItems.size
                val items = resources.getQuantityString(
                    R.plurals.delete_conversations, itemsCnt, itemsCnt
                )
                val question = String.format(resources.getString(baseString), items)
                ConfirmationDialog.newInstance(
                    this,
                    question,
                    positive = getString(R.string.ok),
                    dialogTitle = getString(R.string.block_msg)
                ) {
                    blockConversation(selectedItems)
                    conversationAdapter.clearSelection()
                    /* CoroutineScope(Dispatchers.IO).launch {
                         withContext(Dispatchers.Main) {
                             blockConversation(selectedItems)
                             conversationAdapter.clearSelection()
                         }
                     }*/
                }
            }
        }
        conversationAdapter.autoScrollToStart(binding.rvConversations)
        setupSwipeActions()

        binding.rvConversations.adapter = conversationAdapter

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

    private fun setupSwipeActions() {
        var swipe = true

        val swipeListener: OnItemSwipeListener = object : OnItemSwipeListener {
            override fun onItemSwipeStart(
                viewHolder: RecyclerView.ViewHolder?, bindingAdapterPosition: Int
            ) {
            }

            override fun onItemSwipeEnd(
                viewHolder: RecyclerView.ViewHolder, bindingAdapterPosition: Int
            ) {
            }

            override fun onItemSwiped(
                viewHolder: RecyclerView.ViewHolder, direction: Int, bindingAdapterPosition: Int
            ) {
                this@TempActivity.bindingAdapterPosition = bindingAdapterPosition
                val item = conversationAdapter.data?.get(bindingAdapterPosition)!!
                manageSwipeAction(direction, listOf(item), bindingAdapterPosition)
            }

            override fun onItemSwipeMoving(
                canvas: Canvas,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                isCurrentlyActive: Boolean
            ) {
            }
        }

        val leftSwipe = myPreferences.swipeLeftActionLabel != "swipeNone"
        val rightSwipe = myPreferences.swipeRightActionLabel != "swipeNone"

        if (conversationAdapter.isSelectionMode) {
            swipe = false
        } else {
            swipe = true
            if (!leftSwipe && !rightSwipe) {
                swipe = false
            }
        }

        quickDragAndSwipe.attachToRecyclerView(binding.rvConversations)
            .setDataCallback(conversationAdapter)
            .setItemSwipeListener(swipeListener)
            .setItemViewSwipeEnabled(swipe)
            .setItemViewLeftSwipeEnabled(leftSwipe)
            .setItemViewRightSwipeEnabled(rightSwipe)
            .setSwipeView(createSwipedView())
    }

    private fun getSwipeActionDefaultIcons(isRightAction: Boolean): Int {
        val defaultAction = if (isRightAction) {
            myPreferences.swipeRightActionLabel
        } else {
            myPreferences.swipeLeftActionLabel
        }

        when (defaultAction) {
            "swipeArchive" -> {
                return R.drawable.ic_archive
            }

            "swipeDelete" -> {
                return R.drawable.ic_delete
            }

            "swipeMarkRead" -> {
                return R.drawable.ic_mark_read
            }

            "swipeMarkUnRead" -> {
                return R.drawable.ic_mark_unread
            }

            "swipeNone" -> {
                return R.drawable.ic_block
            }
        }
        return R.drawable.ic_archive
    }

    private fun createSwipedView(): QuickDragAndSwipe.SwipedView {

        val mSwipedView = QuickDragAndSwipe.SwipedView()

        mSwipedView.setBackrounds(
            intArrayOf(
                R.color.colorAccent, R.color.colorAccent
            )
        )
        mSwipedView.setIcons(
            intArrayOf(
                getSwipeActionDefaultIcons(true), getSwipeActionDefaultIcons(false)
            )
        )
        mSwipedView.setTexts(
            arrayOf<String?>(
                "", ""
            )
        )
        mSwipedView.setTextColorView(
            Color.WHITE
        )
        mSwipedView.setTextSizeView(
            15
        )

        return mSwipedView
    }

    private fun manageSwipeAction(
        direction: Int, items: List<ConversationNew>, bindingAdapterPosition: Int
    ) {
        val defaultAction = if (direction == ItemTouchHelper.RIGHT) {
            myPreferences.swipeRightActionLabel
        } else {
            myPreferences.swipeLeftActionLabel
        }

        when (defaultAction) {
            "swipeArchive" -> {
                archiveConversations(items)
            }

            "swipeDelete" -> {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        showSnackBar(items, true)
                    }
                    withContext(Dispatchers.Main) {
                        deleteConversations(items)
                        conversationAdapter.clearSelection()
                    }
                }
            }

            "swipeMarkRead" -> {
                markAsRead(items)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (bindingAdapterPosition == 0) {
                        scrollToTop()
                    }
                }, 300)
            }

            "swipeMarkUnRead" -> {
                markAsUnread(items)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (bindingAdapterPosition == 0) {
                        scrollToTop()
                    }
                }, 300)
            }

            "swipeNone" -> {

            }
        }
    }

    private fun blockConversation(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                addBlockedNumber(it.lastMessage?.address!!)
//                viewModel.addToBlockConversation(it.id)
//            }
//        }
        selectedItems.forEach {
            addBlockedNumber(it.lastMessage?.address!!)
            viewModel.addToBlockConversation(it.id)
        }

    }

    private fun markAsRead(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        selectedItems.forEach {
            viewModel.markAllMessageThreadRead(it.id)
            markThreadMessagesRead(it.id)
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                markThreadMessagesRead(it.id)
//                viewModel.markAsReadConversation(it.id)
//                viewModel.markAllMessageThreadRead(it.id)
//            }
//        }
        conversationAdapter.clearSelection()
    }

    private fun markAsUnread(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        selectedItems.forEach {
            viewModel.markAsUnReadMessage(it.id)
            markThreadMessagesUnread(it.id)
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                markThreadMessagesUnread(it.id)
//                viewModel.markAsUnReadConversation(it.id)
////                viewModel.markAsUnReadMessage(it.msgId)
//            }
//        }
        conversationAdapter.clearSelection()
    }

    private fun pinConversation(pin: Boolean, selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
        selectedItems.forEach {
            if (pin) {
                viewModel.pinConversation(it.id)
            } else {
                viewModel.unPinConversation(it.id)
            }
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                if (pin) {
//                    viewModel.pinConversation(it.id)
//                } else {
//                    viewModel.unPinConversation(it.id)
//                }
//            }
//        }

    }

    private fun archiveConversations(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                updateConversationArchivedStatus(it.id, true)
//                viewModel.moveToArchive(it.id)
//                notificationManager.cancel(it.id.toInt())
//            }
//        }
        selectedItems.forEach {
            updateConversationArchivedStatus(it.id, true)
            viewModel.moveToArchive(it.id)
            notificationManager.cancel(it.id.toInt())
        }
        CoroutineScope(Dispatchers.Main).launch {
            showSnackBar(selectedItems, false)
            conversationAdapter.clearSelection()
        }
    }

    private fun unArchiveConversations(selectedItems: List<ConversationNew>) {
        if (selectedItems.isEmpty()) {
            return
        }
//        CoroutineScope(Dispatchers.IO).launch {
//            selectedItems.forEach {
//                updateConversationArchivedStatus(it.id, true)
//                viewModel.moveToUnArchive(it.id)
//                notificationManager.cancel(it.id.toInt())
//            }
//        }
        selectedItems.forEach {
            updateConversationArchivedStatus(it.id, false)
            viewModel.moveToUnArchive(it.id)
            notificationManager.cancel(it.id.toInt())
        }
    }

    private fun showSnackBar(selectedItems: List<ConversationNew>, isDeleteEvent: Boolean) {
        binding.swipeAbleView.visible()
        var msg = ""
        msg = if (isDeleteEvent) {
            resources.getQuantityString(
                R.plurals.delete_conversations, selectedItems.size, selectedItems.size
            ) + " " + getString(R.string.delete)
        } else {
            resources.getQuantityString(
                R.plurals.delete_conversations, selectedItems.size, selectedItems.size
            ) + " " + getString(R.string.archives)
        }

        val snackBar = Snackbar.make(
            binding.swipeAbleView, msg, Snackbar.LENGTH_LONG
        )

        val params = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = SwipeDismissBehavior<View>()
        behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
        params.behavior = behavior
        snackBar.view.layoutParams = params
        snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_MANUAL) {
                    binding.swipeAbleView.gone()
                }
            }
        })

        if (isDeleteEvent) {

        } else {
            snackBar.setAction(getString(R.string.undo)) {
                snackBar.dismiss()
                unArchiveConversations(selectedItems)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (bindingAdapterPosition == 0) {
                        scrollToTop()
                    }
                }, 300)
            }
        }

        snackBar.show()
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
//            viewModel.deleteMessageByThreadId(it.id)
            notificationManager.cancel(it.id.toInt())
        }
    }

    private fun viewClick() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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
        binding.conversationsFab.setOnClickListener(1000L) {
            checkRunTimePermission(
                Manifest.permission.READ_CONTACTS
            ) {
                startActivity(Intent(this@TempActivity, NewConversationActivity::class.java))
            }
        }
        binding.btnSearch.setOnClickListener(1000L) {
            checkRunTimePermission(
                Manifest.permission.READ_CONTACTS
            ) {
                startActivity(Intent(this, SearchActivity::class.java))
            }
        }
        binding.settingsButton.setOnClickListener(1000L) {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        binding.ivScrollUp.setOnClickListener(1000L) {
            scrollToTop()
            binding.ivScrollUp.gone()
        }
        binding.btnPermission.setOnClickListener(1000L) {
            loadData()
        }

        //----------------------------DrawerItemClick-----------------------------
        binding.btnUnreadMessage.setOnClickListener(1000L) {
            closeDrawer()
        }
        binding.btnPrivateBox.setOnClickListener(1000L) {
            closeDrawer()
        }
        binding.btnArchive.setOnClickListener(1000L) {
            closeDrawer()
            startActivity(Intent(this@TempActivity, ArchivedActivity::class.java))
        }
        binding.btnSchedule.setOnClickListener(1000L) {
            closeDrawer()
            startActivity(Intent(this, ScheduledActivity::class.java))
        }
        binding.btnStared.setOnClickListener(1000L) {
            closeDrawer()
            startActivity(Intent(this, StarredActivity::class.java))
        }
        binding.btnBlock.setOnClickListener(1000L) {
            closeDrawer()
            startActivity(Intent(this, BlockListActivity::class.java))
        }
        binding.btnBackup.setOnClickListener(1000L) {
            startActivity(Intent(this@TempActivity, BackupRestoreActivity::class.java))
            closeDrawer()
        }
        binding.btnSwipeAction.setOnClickListener(1000L) {
            startActivity(Intent(this@TempActivity, SwipeActionsActivity::class.java))
            closeDrawer()
        }
        binding.btnPrivacy.setOnClickListener(1000L) {
            closeDrawer()
            AdsUtility.privacyPolicy(this)
        }
        binding.btnShare.setOnClickListener(1000L) {
            closeDrawer()
            AdsUtility.shareApp(this)
        }
        binding.btnRateUs.setOnClickListener(1000L) {
            closeDrawer()
            AdsUtility.rateUs(this)
        }
    }

    private fun loadData() {
        checkAppDefault {
            checkRunTimePermission(
                Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS
            ) {
                val lastSync = Realm.getDefaultInstance()
                    .use { realm -> realm.where(SyncLog::class.java)?.max("date") ?: 0 }
                Log.e("TAG", "checkIfLastSync: ${lastSync}")
                if (lastSync == 0) {
                    initFlow(true)
                } else {
                    initFlow(false)
                }
                observerContactUpdate()
            }
        }
    }

    private fun scrollToTop() {
        val ll = binding.rvConversations.layoutManager as LinearLayoutManager
        ll.scrollToPositionWithOffset(0, 0)
    }

    private fun manageMarquee() {
        binding.search.isSelected = true
        binding.tvChat.isSelected = true
        binding.tvHeader.isSelected = true
        binding.tvUnreadMsg.isSelected = true
        binding.tvPrivateBox.isSelected = true
        binding.tvArchive.isSelected = true
        binding.tvSchedule.isSelected = true
        binding.tvBlock.isSelected = true
        binding.tvStarred.isSelected = true
        binding.tvBackup.isSelected = true
        binding.tvSwipeAction.isSelected = true
        binding.tvMoreOption.isSelected = true
        binding.tvPrivacy.isSelected = true
        binding.tvInvite.isSelected = true
        binding.tvRateUs.isSelected = true
    }

    private fun observerContactUpdate() {
        checkRunTimePermission(
            Manifest.permission.READ_CONTACTS
        ) {
            val contentResolver = contentResolver
            contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, MyContactObserver(this, viewModel)
            )
        }
    }

    private fun hideView(isVisible: Boolean) {
        if (isVisible) {
            binding.searchView.gone()
            binding.btnSearch.visible()
            binding.rvConversations.visible()
            binding.conversationsFab.visible()
            binding.settingsButton.visible()
            binding.containerProgress.gone()
        } else {
            binding.searchView.gone()
            binding.containerProgress.visible()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSetting(event: UpdateSetting) {
        conversationAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateConversations(event: UpdateConversations) {
//        observeConversationData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateSwipeAction(event: UpdateSwipeAction) {
        setupSwipeActions()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun networkStateChanged(state: NetworkChangeReceiver.NetworkState?) {
        super.networkStateChanged(state)
        if (state == NetworkChangeReceiver.NetworkState.CONNECTED && AdsUtility.checkIsIdNotEmpty()) {
            showAd()
            if (!isAdShown) {
                isAdShown = true
                bannerAd()
                nativeAdSmall()
            }
        } else if (state == NetworkChangeReceiver.NetworkState.NOT_CONNECTED) {
            hideAd()
        }

    }

    private fun hideAd() {
        runOnUiThread {
            binding.root.findViewById<FrameLayout>(R.id.banner_ad_container).gone()
            binding.collapseLayout.gone()
        }
    }

    private fun showAd() {
        runOnUiThread {
            if (AdsUtility.isNetworkConnected(this@TempActivity)) binding.root.findViewById<FrameLayout>(
                R.id.banner_ad_container
            ).visible()
            binding.collapseLayout.visible()
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + doubleBackToExit > System.currentTimeMillis()) {
            super.onBackPressed() // If the time interval has passed, exit the app
        } else {
            showToast(getString(R.string.exit_app))
        }

        backPressedTime = System.currentTimeMillis()
    }

}