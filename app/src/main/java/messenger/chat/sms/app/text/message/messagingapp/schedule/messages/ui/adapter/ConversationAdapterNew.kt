package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.dragswipe.listener.DragAndSwipeDataCallback
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.AdLayoutNativeSmallBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.inVisible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.BaseActivity

class ConversationAdapterNew(
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    val isFromBlockList: Boolean = false,
    private val isFromSchedule: Boolean = false,
    private val actionCallback: () -> Unit,
    private val callBack: (item: Conversation?, deleteEvent: Boolean, archiveEvent: Boolean, pinEvent: Boolean, unPinEvent: Boolean, markAsReadEvent: Boolean, markAsUnReadEvent: Boolean, blockEvent: Boolean, unblockEvent: Boolean, selectedItems: List<Conversation>) -> Unit
) : BaseDifferAdapter<Conversation, ConversationAdapterNew.ViewHolder>(diffCallback = DiffCallback()),
    DragAndSwipeDataCallback {
    private var actionMode: ActionMode? = null
    var isSelectionMode = false
    private val selectedItems = mutableListOf<Conversation>()

    //    private val typedArray: TypedArray =
//        baseActivity.obtainStyledAttributes(intArrayOf(R.attr.textColor))
//    private val typedArrayBg: TypedArray =
//        baseActivity.obtainStyledAttributes(intArrayOf(R.attr.bg))
//    val textColor = typedArray.getColor(0, 0)
//    val statusBarColor = typedArrayBg.getColor(0, 0)
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
            /*  when (item?.itemId) {
                  R.id.menu_delete -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      return true
                  }

                  R.id.menu_archive -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      return true
                  }

                  R.id.menu_pin -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      clearSelection()
                      return true
                  }

                  R.id.menu_unpin -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      clearSelection()
                      return true
                  }

                  R.id.menu_read -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      clearSelection()
                      return true
                  }

                  R.id.menu_unread -> {
                      val selectedItemsList = arrayListOf<Conversation>()
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
                          selectedItemsList
                      )
                      clearSelection()
                      return true
                  }
              }*/
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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
            val selectedItemsList = arrayListOf<Conversation>()
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

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        item: Conversation?
    ) {
        /*if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
            if (item?.threadId != -1L && item?.message != null && position > 0) {
                val view = holder.binding as ItemConversationBinding
                setupView(view, item, item.message!!, position)
            }
        } else {
            if (item?.threadId != -1L && item?.message != null) {
                val view = holder.binding as ItemConversationBinding
                setupView(view, item, item.message!!, position)
            }
        }*/



        if (isFromBlockList || isFromSchedule) {
            if (item?.threadId != -1L) {
                val view = holder.binding as ItemConversationBinding
                setupView(view, item!!, null, position)
            }
        } else {
            if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
                if (item?.threadId != -1L && position > 0) {
                    val view = holder.binding as ItemConversationBinding
                    setupView(view, item!!, null, position)
                }
            } else {
                if (item?.threadId != -1L) {
                    val view = holder.binding as ItemConversationBinding
                    setupView(view, item!!, null, position)
                }
            }
        }

    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
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
                    ViewHolder(binding)
                }

                1 -> {
                    val binding =
                        ItemConversationBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    ViewHolder(binding)
                }

                else -> {
                    val binding =
                        ItemConversationBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    ViewHolder(binding)
                }
            }
        } else {
            val binding =
                ItemConversationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return ViewHolder(binding)
        }
    }

    private fun setupView(
        view: ItemConversationBinding,
        mConversation: Conversation,
        msg: Message?,
        position: Int
    ) {
        view.apply {
            conversationAddress.isSelected = true
            conversationDate.isSelected = true
            draftIndicator.isSelected = true
            conversationBodyShort.isSelected = true
            if (mConversation.isItemPinned()) {
                pinIndicator.visible()
            } else {
                pinIndicator.gone()
            }

            conversationAddress.apply {
                text = mConversation.title
            }

            conversationBodyShort.apply {
                /*text = if (msg != null) {
                    if (msg.isMe()) {
                        "${baseActivity.getString(R.string.you)}: ${msg.body}"
                    } else {
                        msg.body
                    }
                } else {
                    ""
                }*/

                text = mConversation.snippet
            }

            conversationDate.apply {
                text = mConversation.date.formatDateOrTime(
                    context, true,
                    showYearEvenIfCurrent = false,
                    hideSevenDays = false
                )
            }

            val style = if (mConversation.read) {
                conversationBodyShort.alpha = 0.7f
                conversationDate.alpha = 0.7f
                conversationRead.inVisible()
            } else {
                conversationBodyShort.alpha = 1f
                conversationDate.alpha = 1f
                conversationRead.visible()
            }

            val placeholder = null
            loadContactImage(
                baseActivity,
                mConversation.photoUri,
                conversationImage,
                mConversation.title,
                placeholder,
                myPreferences.showProfilePhoto
            )

            if (isSelectionMode) {
                imgSelect.visible()
                if (mConversation.selected) {
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

    class DiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return Conversation.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return Conversation.areContentsTheSame(oldItem, newItem)
        }
    }

    private fun toggleItemSelection(conversation: Conversation, position: Int): Boolean {
        val isSelected = selectedItems.contains(conversation)
        if (isSelected) {
            conversation.selected = false
            selectedItems.remove(conversation)
        } else {
            conversation.selected = true
            selectedItems.add(conversation)
        }
        items.forEach { currentItem ->
            currentItem.selected = selectedItems.any { selectedItem -> selectedItem == currentItem }
        }

        notifyItemChanged(position)
        updateTitle()
        actionMode?.invalidate()
        return !isSelected
    }

    private fun updateTitle() {
        actionMode?.title =
            "${selectedItems.size} ${baseActivity.resources.getString(R.string.item_selected)}"
    }

    fun clearSelection() {
        items.forEach {
            it.selected = false
        }
        selectedItems.clear()
        isSelectionMode = false
        actionMode?.finish()
        actionMode = null
        actionCallback.invoke()
        notifyDataSetChanged()

    }

    private fun checkPinBtnVisibility(menu: Menu) {
        val pinnedConversations = items.filter { it.isPin == 1 }
        val selectedConversations = selectedItems

        val allUnpinned = selectedConversations.all { !pinnedConversations.contains(it) }
        val allPinned = selectedConversations.all { pinnedConversations.contains(it) }

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
        val allUnread = selectedConversations.all { !it.read }
        val allRead = selectedConversations.all { it.read }
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

    override fun getItemViewType(position: Int, list: List<Conversation>): Int {
        if (isFromBlockList) {
            return 1
        }

        if (isFromSchedule) {
            return 1
        }

        return if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
            if (position == 0) {
                0
            } else {
                1
            }
        } else {
            1
        }
    }

    override fun dataMove(fromPosition: Int, toPosition: Int) {
        move(fromPosition, toPosition)
    }

    override fun dataRemoveAt(position: Int) {
        removeAt(position)
    }

}