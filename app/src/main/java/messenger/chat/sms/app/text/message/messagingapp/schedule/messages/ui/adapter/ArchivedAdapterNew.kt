package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

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
import io.realm.RealmRecyclerViewAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.AdLayoutNativeSmallBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.QkRealmAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.QkViewHolder
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.inVisible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.BaseActivity

open class ArchivedAdapterNew(
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val callBack: (item: ConversationNew?, deleteEvent: Boolean, unArchiveEvent: Boolean, selectedItems: List<ConversationNew>) -> Unit
) : QkRealmAdapter<ConversationNew>() {
    private var selection = listOf<Long>()
    private var actionMode: ActionMode? = null
    private var isSelectionMode = false
    private val selectedItems = mutableListOf<ConversationNew>()
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
                    val selectedItemsList = arrayListOf<ConversationNew>()
                    selectedItemsList.addAll(selectedItems)
                    callBack.invoke(
                        null, true, false, selectedItemsList
                    )
                    return true
                }

                R.id.menu_unarchive -> {
                    val selectedItemsList = arrayListOf<ConversationNew>()
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
            val selectedItemsList = arrayListOf<ConversationNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, true, false, selectedItemsList
            )
        }
        item.findItem(R.id.menu_unarchive).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<ConversationNew>()
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
                    context, true, showYearEvenIfCurrent = false, hideSevenDays = false
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

            val placeholder = null
            loadContactImage(
                baseActivity,
                recipient?.contact?.photoUri ?: "",
                conversationImage,
                mConversation.getTitle(),
                placeholder,
                myPreferences.showProfilePhoto
            )

            if (isSelectionMode) {
                imgSelect.visible()
                if (isSelected(mConversation.id)) {
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
            "${selectedItems.size} ${baseActivity.resources.getString(R.string.item_selected)}"
    }

    fun clearSelection() {
        selection = listOf()
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
        actionMode?.finish()
        actionMode = null
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

   /* override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data?.get(position)
        if (item?.id != -1L) {
            val view = holder.binding as ItemConversationBinding
            setupView(view, item!!, null, position)
        }
    }*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val binding =
            ItemConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return QkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val item = data?.get(position)
        if (item?.id != 0L) {
            val view = holder.view as ItemConversationBinding
            setupView(view, item!!, position)
        }
    }


}