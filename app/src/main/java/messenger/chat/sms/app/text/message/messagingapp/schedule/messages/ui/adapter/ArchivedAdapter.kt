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

class ArchivedAdapter(
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    private val callBack: (item: Conversation?, deleteEvent: Boolean, unArchiveEvent: Boolean, selectedItems: List<Conversation>) -> Unit
) : BaseDifferAdapter<Conversation, ArchivedAdapter.ViewHolder>(diffCallback = DiffCallback()) {
    private var actionMode: ActionMode? = null
    private var isSelectionMode = false
    private val selectedItems = mutableListOf<Conversation>()
    private val selectedTextColor = ContextCompat.getColor(baseActivity, R.color.progressColor)
    val textColor = ContextCompat.getColor(baseActivity, R.color.textColor)
    val statusBarColor = ContextCompat.getColor(baseActivity, R.color.bg)

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            baseActivity.menuInflater.inflate(
                R.menu.selection_menu, menu
            )
            updateTitle()
            menuItemClick(menu!!)
            switchStatusColor(
                baseActivity.window.statusBarColor, statusBarColor, 0
            )
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val archivedMenuItem = menu?.findItem(R.id.menu_archive)
            val readMenuItem = menu?.findItem(R.id.menu_read)
            val unReadMenuItem = menu?.findItem(R.id.menu_unread)
            val pinMenuItem = menu?.findItem(R.id.menu_pin)
            val unPinMenuItem = menu?.findItem(R.id.menu_unpin)
            val copyMenuItem = menu?.findItem(R.id.menu_copy)
            val blockMenuItem = menu?.findItem(R.id.menu_block)
            val unblockMenuItem = menu?.findItem(R.id.menu_unblock)
            val starredMenuItem = menu?.findItem(R.id.menu_starred)
            archivedMenuItem?.isVisible = false
            readMenuItem?.isVisible = false
            unReadMenuItem?.isVisible = false
            pinMenuItem?.isVisible = false
            unPinMenuItem?.isVisible = false
            copyMenuItem?.isVisible = false
            blockMenuItem?.isVisible = false
            unblockMenuItem?.isVisible = false
            starredMenuItem?.isVisible = false
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.menu_delete -> {
                    val selectedItemsList = arrayListOf<Conversation>()
                    selectedItemsList.addAll(selectedItems)
                    callBack.invoke(
                        null, true, false, selectedItemsList
                    )
                    return true
                }

                R.id.menu_unarchive -> {
                    val selectedItemsList = arrayListOf<Conversation>()
                    selectedItemsList.addAll(selectedItems)
                    callBack.invoke(
                        null, false, true, selectedItemsList
                    )
                    return true
                }
            }
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
                null, true, false, selectedItemsList
            )
        }
        item.findItem(R.id.menu_unarchive).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<Conversation>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, false, true, selectedItemsList
            )
        }
    }

    inner class ViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root)

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
                    context, true, showYearEvenIfCurrent = false, hideSevenDays = false
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
                        conversationAddress, conversationBodyShort, conversationDate
                    ).forEach {
                        it.setTextColor(selectedTextColor)
                    }
                } else {
                    imgSelect.setImageResource(R.drawable.ic_uncheck)
                    arrayListOf(
                        conversationAddress, conversationBodyShort, conversationDate
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
                        mConversation, false, false, emptyList()
                    )
                }
            }

            root.setOnLongClickListener {
                if (actionMode == null) {
                    isSelectionMode = true
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
        notifyDataSetChanged()
        actionMode?.finish()
        actionMode = null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: Conversation?) {
        /*if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
            if (item?.threadId != -1L && position > 0) {
                val view = holder.binding as ItemConversationBinding
                setupView(view, item!!, null, position)
            }
        } else {
            if (item?.threadId != -1L) {
                val view = holder.binding as ItemConversationBinding
                setupView(view, item!!, null, position)
            }
        }*/
        if (item?.threadId != -1L) {
            val view = holder.binding as ItemConversationBinding
            setupView(view, item!!, null, position)
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

    override fun getItemViewType(position: Int, list: List<Conversation>): Int {
//        return if (AdsUtility.isNetworkConnected(baseActivity) && AdsUtility.config.adMob.nativeAd.isNotEmpty()) {
//            if (position == 0) {
//                0
//            } else {
//                1
//            }
//        } else {
//            1
//        }
        return 1
    }


}