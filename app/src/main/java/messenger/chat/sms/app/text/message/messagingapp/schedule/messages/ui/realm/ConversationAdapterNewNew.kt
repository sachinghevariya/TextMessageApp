package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback
import io.realm.RealmRecyclerViewAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.AdLayoutNativeSmallBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.inVisible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.BaseActivity

open class ConversationAdapterNewNew(
//    var items: ArrayList<ConversationNew>,
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    val isFromBlockList: Boolean = false,
    private val isFromSchedule: Boolean = false,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val actionCallback: () -> Unit,
    private val callBack: (item: ConversationNew?, deleteEvent: Boolean, archiveEvent: Boolean, pinEvent: Boolean, unPinEvent: Boolean, markAsReadEvent: Boolean, markAsUnReadEvent: Boolean, blockEvent: Boolean, unblockEvent: Boolean, selectedItems: List<ConversationNew>) -> Unit
) : QkRealmAdapter<ConversationNew>(), DragAndSwipeDataCallback {

    private var selection = listOf<Long>()

    private var actionMode: ActionMode? = null
    var isSelectionMode = false
    private val selectedItems = mutableListOf<ConversationNew>()
    val textColor = ContextCompat.getColor(baseActivity, R.color.textColor)
    val statusBarColor = ContextCompat.getColor(baseActivity, R.color.bg)

    private val selectedTextColor = ContextCompat.getColor(baseActivity, R.color.progressColor)

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            baseActivity.menuInflater.inflate(
                R.menu.selection_menu,
                menu
            )
            updateTitle()
            menuItemClick(menu!!)
            switchStatusColor(
                baseActivity.window.statusBarColor,
                statusBarColor, 0
            )
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if (isFromBlockList) {
                val archivedMenuItem = menu?.findItem(R.id.menu_archive)
                val unarchivedMenuItem = menu?.findItem(R.id.menu_unarchive)
                val readMenuItem = menu?.findItem(R.id.menu_read)
                val unReadMenuItem = menu?.findItem(R.id.menu_unread)
                val pinMenuItem = menu?.findItem(R.id.menu_pin)
                val unPinMenuItem = menu?.findItem(R.id.menu_unpin)
                val blockMenuItem = menu?.findItem(R.id.menu_block)
                val copyMenuItem = menu?.findItem(R.id.menu_copy)
                val starredMenuItem = menu?.findItem(R.id.menu_starred)
                archivedMenuItem?.isVisible = false
                unarchivedMenuItem?.isVisible = false
                readMenuItem?.isVisible = false
                unReadMenuItem?.isVisible = false
                pinMenuItem?.isVisible = false
                unPinMenuItem?.isVisible = false
                blockMenuItem?.isVisible = false
                copyMenuItem?.isVisible = false
                starredMenuItem?.isVisible = false
            } else {
                val unArchivedMenuItem = menu?.findItem(R.id.menu_unarchive)
                val copyMenuItem = menu?.findItem(R.id.menu_copy)
                val unblockMenuItem = menu?.findItem(R.id.menu_unblock)
                val starredMenuItem = menu?.findItem(R.id.menu_starred)
                unArchivedMenuItem?.isVisible = false
                copyMenuItem?.isVisible = false
                unblockMenuItem?.isVisible = false
                starredMenuItem?.isVisible = false
                checkPinBtnVisibility(menu!!)
                checkReadBtnVisibility(menu)
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            clearSelection()
        }
    }

    fun switchStatusColor(colorFrom: Int, colorTo: Int, duration: Long) {
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = duration
        colorAnimation.addUpdateListener { animator ->
            baseActivity.window.statusBarColor = animator.animatedValue as Int
        }
        colorAnimation.start()
    }

    fun menuItemClick(item: Menu) {
        item.findItem(R.id.menu_delete).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                selectedItemsList
            )
        }
        item.findItem(R.id.menu_archive).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                selectedItemsList
            )
        }
        item.findItem(R.id.menu_pin).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                selectedItemsList
            )
            clearSelection()
        }
        item.findItem(R.id.menu_unpin).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                selectedItemsList
            )
            clearSelection()
        }
        item.findItem(R.id.menu_read).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                selectedItemsList
            )
            clearSelection()
        }
        item.findItem(R.id.menu_unread).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                selectedItemsList
            )
            clearSelection()
        }
        item.findItem(R.id.menu_block).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                selectedItemsList
            )
        }
        item.findItem(R.id.menu_unblock).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                selectedItemsList
            )
        }
    }

    inner class ViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun setupView(
        view: ItemConversationNewBinding,
        mConversation: ConversationNew,
        position: Int
    ) {
        view.apply {
            val lastMessage = mConversation.lastMessage
            val recipient = when {
                mConversation.recipients.size == 1 || lastMessage == null -> mConversation.recipients.firstOrNull()
                else -> mConversation.recipients.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, lastMessage.address)
                }
            }
            var photoUri = ""
            photoUri = if (recipient?.contact?.photoUri == null) {
                ""
            } else {
                recipient.contact?.photoUri!!
            }
            conversationImage.title = Pair(mConversation.getTitle(), photoUri)
            conversationAddress.isSelected = true
            conversationDate.isSelected = true
            draftIndicator.isSelected = true
            conversationBodyShort.isSelected = true
            if (mConversation.pinned) {
                pinIndicator.visible()
            } else {
                pinIndicator.gone()
            }

            conversationAddress.apply {
                text = mConversation.getTitle()
            }

            conversationBodyShort.apply {
                text = when {
                    mConversation.draft.isNotEmpty() -> mConversation.draft
                    mConversation.me -> "${context.getString(R.string.you)}: " + mConversation.snippet
                    else -> mConversation.snippet
                }
            }

            conversationDate.apply {
                text = (mConversation.date / 1000).toInt().formatDateOrTime(
                    context, true,
                    showYearEvenIfCurrent = false,
                    hideSevenDays = false
                )
            }

            val style = if (!mConversation.unread) {
                conversationBodyShort.alpha = 0.7f
                conversationDate.alpha = 0.7f
                conversationRead.inVisible()
            } else {
                conversationBodyShort.alpha = 1f
                conversationDate.alpha = 1f
                conversationRead.visible()
            }

//            val placeholder = null
//            loadContactImage(
//                baseActivity,
//                recipient?.contact?.photoUri ?: "",
//                conversationImage,
//                mConversation.getTitle(),
//                placeholder,
//                myPreferences.showProfilePhoto
//            )

            if (isSelectionMode) {
                imgSelect.visible()
                if (isSelected(mConversation.id)) {
                    imgSelect.setImageResource(R.drawable.ic_check)
                    arrayListOf(
                        conversationAddress,
                        conversationBodyShort,
                        conversationDate
                    ).forEach {
                        it.setTextColor(selectedTextColor)
                    }
                } else {
                    imgSelect.setImageResource(R.drawable.ic_uncheck)
                    arrayListOf(
                        conversationAddress,
                        conversationBodyShort,
                        conversationDate
                    ).forEach {
                        it.setTextColor(textColor)
                    }
                }
            } else {
                imgSelect.gone()
                arrayListOf(conversationAddress, conversationBodyShort, conversationDate).forEach {
                    it.setTextColor(textColor)
                }
            }

            root.setOnClickListener {
                if (actionMode != null) {
                    toggleItemSelection(mConversation, position)
                    if (selectedItems.isEmpty()) {
                        clearSelection()
                    }
                } else {
                    callBack.invoke(
                        mConversation,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        emptyList()
                    )
                }
            }

            if (!isFromSchedule) {
                root.setOnLongClickListener {
                    if (actionMode == null) {
                        isSelectionMode = true
                        actionCallback.invoke()
                        toggleItemSelection(mConversation, position)
                        actionMode = baseActivity.startSupportActionMode(actionModeCallback)
                        updateTitle()
                        notifyDataSetChanged()
                        return@setOnLongClickListener true
                    } else {
//                    clearSelection()
                        return@setOnLongClickListener false
                    }
                }
            }
        }
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

//    override fun updateData(data: OrderedRealmCollection<ConversationNew>?) {
//        items.clear()
//        data?.forEach {
//            items.add(it)
//        }
//        super.updateData(data)
//    }

    private fun toggleItemSelection(conversation: ConversationNew, position: Int): Boolean {
        selection = when (selection.contains(conversation.id)) {
            true -> selection - conversation.id
            false -> selection + conversation.id
        }
        val isSelected = selectedItems.contains(conversation)
        if (isSelected) {
            selectedItems.remove(conversation)
        } else {
            selectedItems.add(conversation)
        }

        notifyItemChanged(position)
        updateTitle()
        actionMode?.invalidate()
        return !isSelected
    }

    private fun updateTitle() {
        actionMode?.title =
            "${selection.size} ${baseActivity.resources.getString(R.string.item_selected)}"
    }

    fun clearSelection() {
        selection = listOf()
        selectedItems.clear()
        isSelectionMode = false
        actionMode?.finish()
        actionMode = null
        actionCallback.invoke()
        notifyDataSetChanged()
    }

    private fun checkPinBtnVisibility(menu: Menu) {
        val pinnedConversations = data?.filter { it.pinned }
        val selectedConversations = selectedItems

        val allUnpinned = selectedConversations.all { !pinnedConversations!!.contains(it) }
        val allPinned = selectedConversations.all { pinnedConversations!!.contains(it) }

        val pinMenuItem = menu.findItem(R.id.menu_pin)
        val unpinMenuItem = menu.findItem(R.id.menu_unpin)
        if (allUnpinned) {
            pinMenuItem.isVisible = true
            unpinMenuItem.isVisible = false
        } else if (allPinned) {
            pinMenuItem.isVisible = false
            unpinMenuItem.isVisible = true
        } else {
            pinMenuItem.isVisible = false
            unpinMenuItem.isVisible = false
        }
    }

    private fun checkReadBtnVisibility(menu: Menu) {
        val selectedConversations = selectedItems
        val allUnread = selectedConversations.all { it.unread }
        val allRead = selectedConversations.all { !it.unread }
        val markReadMenuItem = menu.findItem(R.id.menu_read)
        val markUnreadMenuItem = menu.findItem(R.id.menu_unread)
        if (allUnread) {
            markReadMenuItem.isVisible = true
            markUnreadMenuItem.isVisible = false
        } else if (allRead) {
            markReadMenuItem.isVisible = false
            markUnreadMenuItem.isVisible = true
        } else {
            markReadMenuItem.isVisible = false
            markUnreadMenuItem.isVisible = false
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (isFromBlockList) {
            return 1
        }

        if (isFromSchedule) {
            return 1
        }

        return 1
//        return if(data?.get(position)?.id == -1L){
//            Log.e("TAG", "getItemViewType: ")
//            0
//        }else{
//            1
//        }
//        return if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
//            if (position == 0) {
//                0
//            } else {
//                1
//            }
//        } else {
//            1
//        }
    }

 /*   override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data?.get(position)
        if (isFromBlockList || isFromSchedule) {
            if (item?.id != 0L) {
                val view = holder.binding as ItemConversationNewBinding
                setupView(view, item!!, position)
            }
        } else {
            if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
                if (item?.id != 0L && position > 0) {
                    val view = holder.binding as ItemConversationNewBinding
                    setupView(view, item!!, position)
                }
            } else {
                if (item?.id != 0L) {
                    val view = holder.binding as ItemConversationNewBinding
                    setupView(view, item!!, position)
                }
            }
        }
    }
*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
            return when (viewType) {
                0 -> {
                    val binding =
                        AdLayoutNativeSmallBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    baseActivity.nativeAdSmall(binding.root)
                    QkViewHolder(binding)
                }

                1 -> {
                    val binding =
                        ItemConversationNewBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    QkViewHolder(binding)
                }

                else -> {
                    val binding =
                        ItemConversationNewBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    QkViewHolder(binding)
                }
            }
        } else {
            val binding =
                ItemConversationNewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return QkViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val item = data?.get(position)
        if (isFromBlockList || isFromSchedule) {
            if (item?.id != 0L) {
                val view = holder.view as ItemConversationNewBinding
                setupView(view, item!!, position)
            }
        } else {
            if (item?.id != 0L) {
                val view = holder.view as ItemConversationNewBinding
                setupView(view, item!!, position)
            }

            /*if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
                if (item?.id != 0L && position > 0) {
                    val view = holder.view as ItemConversationNewBinding
                    setupView(view, item!!, position)
                }
            } else {
                if (item?.id != 0L) {
                    val view = holder.view as ItemConversationNewBinding
                    setupView(view, item!!, position)
                }
            }*/
        }
    }

    override fun dataMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition in data?.indices!! || toPosition in data?.indices!!) {
            data?.toMutableList().also {
                val e = it?.removeAt(fromPosition)
                it?.add(toPosition, e)

            }
            updateData(data)
        }
    }

    override fun dataRemoveAt(position: Int) {
        if (position >= data?.size!!) {
            throw IndexOutOfBoundsException("position: ${position}. size:${data?.size}")
        }

        data?.toMutableList().also {
            it?.removeAt(position)
        }
        updateData(data)
    }

}