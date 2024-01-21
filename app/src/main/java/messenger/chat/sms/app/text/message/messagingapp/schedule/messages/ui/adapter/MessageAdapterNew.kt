package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.realm.RealmRecyclerViewAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemMessageBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemThreadDateTimeBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemThreadErrorBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemThreadLoadingBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemThreadSendingBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemThreadSuccessBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.MessageNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ThreadItemNew.ThreadDateTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_RECEIVED_MESSAGE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.THREAD_SENT_MESSAGE
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setVisible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible

class MessageAdapterNew(
    var baseActivity: AppCompatActivity,
    private val callBack: (item: MessageNew?, deleteEvent: Boolean, copyEvent: Boolean, blockEvent: Boolean, starredEvent: Boolean, selectedItems: ArrayList<MessageNew>) -> Unit
) : RealmRecyclerViewAdapter<MessageNew, MessageAdapterNew.ViewHolder>(
    null,
    true,
    true
) {
    var actionMode: ActionMode? = null
    private var isSelectionMode = false
    private var isAllMsgSelected = false
    private var isConversationBlocked = false
    private var sentTextColor = ContextCompat.getColor(baseActivity, R.color.sendTextColor)
    private var receiveTextColor = ContextCompat.getColor(baseActivity, R.color.textColor)
    private var selectedTextColor = ContextCompat.getColor(baseActivity, R.color.sendTextColor)
    private val selectedItems = mutableListOf<MessageNew>()
    val statusBarColor = ContextCompat.getColor(baseActivity, R.color.bg)
    private var selection = listOf<Long>()

    inner class ViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root)

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
            val unArchivedMenuItem = menu?.findItem(R.id.menu_unarchive)
            val archivedMenuItem = menu?.findItem(R.id.menu_archive)
            val readMenuItem = menu?.findItem(R.id.menu_read)
            val unReadMenuItem = menu?.findItem(R.id.menu_unread)
            val pinMenuItem = menu?.findItem(R.id.menu_pin)
            val unPinMenuItem = menu?.findItem(R.id.menu_unpin)
            val unblockMenuItem = menu?.findItem(R.id.menu_unblock)
            val blockMenuItem = menu?.findItem(R.id.menu_block)
            unArchivedMenuItem?.isVisible = false
            archivedMenuItem?.isVisible = false
            readMenuItem?.isVisible = false
            unReadMenuItem?.isVisible = false
            pinMenuItem?.isVisible = false
            unPinMenuItem?.isVisible = false
            unblockMenuItem?.isVisible = false
//            if(isConversationBlocked){
            blockMenuItem?.isVisible = false
//            }
            checkStarredBtnVisibility(menu!!)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            clearSelection()
        }
    }

    private fun checkStarredBtnVisibility(menu: Menu) {
        val selectedConversations = selectedItems
        val allUnStarred = selectedConversations.all { !it.isStarred }
        val allStarred = selectedConversations.all { it.isStarred }
        val starredMenuItem = menu.findItem(R.id.menu_starred)
        if (allUnStarred) {
            starredMenuItem.isVisible = true
        } else if (allStarred) {
            starredMenuItem.isVisible = false
        } else {
            starredMenuItem.isVisible = false
        }
    }


    fun menuItemClick(item: Menu) {
        item.findItem(R.id.menu_delete).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<MessageNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, true, false, false, false, selectedItemsList
            )
        }

        item.findItem(R.id.menu_copy).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<MessageNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, false, true, false, false, selectedItemsList
            )
        }

        item.findItem(R.id.menu_block).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<MessageNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, false, false, true, false, selectedItemsList
            )
        }

        item.findItem(R.id.menu_starred).actionView?.setOnClickListener {
            val selectedItemsList = arrayListOf<MessageNew>()
            selectedItemsList.addAll(selectedItems)
            callBack.invoke(
                null, false, false, false, true, selectedItemsList
            )
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

    fun clearSelection() {
        selection = listOf()
        selectedItems.clear()
        isSelectionMode = false
//        notifyDataSetChanged()
        actionMode?.finish()
        actionMode?.invalidate()
        actionMode = null
        notifyDataSetChanged()
    }

    fun setIsConversationBlocked(isConversationBlocked: Boolean) {
        this.isConversationBlocked = isConversationBlocked
        actionMode?.invalidate()
    }

    private fun toggleItemSelection(msg: MessageNew, position: Int): Boolean {
        selection = when (selection.contains(msg.contentId)) {
            true -> selection - msg.contentId
            false -> selection + msg.contentId
        }
        val isSelected = selectedItems.contains(msg)
        if (isSelected) {
            selectedItems.remove(msg)
        } else {
            selectedItems.add(msg)
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

    private fun setupDateTime(view: View, dateTime: ThreadDateTime) {
        ItemThreadDateTimeBinding.bind(view).apply {
            threadDateTime.apply {
                text = dateTime.date.formatDateOrTime(
                    context,
                    hideTimeAtOtherDays = false,
                    showYearEvenIfCurrent = false,
                    true
                )
//                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
            }
//            threadDateTime.setTextColor(textColor)

            threadSimIcon.gone()
            threadSimNumber.gone()
        }
    }

    private fun setupThreadSuccess(view: View, isDelivered: Boolean) {
        ItemThreadSuccessBinding.bind(view).apply {
            threadSuccess.setImageResource(if (isDelivered) R.drawable.ic_check_double_vector else R.drawable.ic_check_vector)
//            threadSuccess.applyColorFilter(textColor)
        }
    }

    private fun setupThreadError(view: View) {
        val binding = ItemThreadErrorBinding.bind(view)
//        binding.threadError.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize - 4)
    }

    private fun setupThreadSending(view: View) {
        ItemThreadSendingBinding.bind(view).threadSending.apply {
//            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
//            setTextColor(textColor)
        }
    }

    private fun setupThreadLoading(view: View) {
        val binding = ItemThreadLoadingBinding.bind(view)
//        binding.threadLoading.setIndicatorColor(properPrimaryColor)
    }

    private fun setupView(holder: ViewHolder, view: View, message: MessageNew, position: Int) {
        ItemMessageBinding.bind(view).apply {
//            threadMessageHolder.isSelected = selectedKeys.contains(message.hashCode())

            threadMessageBody.apply {
                text = message.body
//                setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
//                beVisibleIf(message.body.isNotEmpty())
//                setOnLongClickListener {
//                    holder.viewLongClicked()
//                    true
//                }

//                setOnClickListener {
//                    holder.viewClicked(message)
//                }
            }


            if (message.isReceivedMessage()) {
                setupReceivedMessageView(messageBinding = this, message = message, position)
            } else {
                setupSentMessageView(messageBinding = this, message = message, position)
            }

            root.setOnClickListener {
                if (actionMode != null) {
                    toggleItemSelection(message, position)
                    if (selectedItems.isEmpty()) {
                        clearSelection()
                    }
                } else {
                    if (message.isStarred) {
                        callBack(message, false, false, false, false, arrayListOf())
                    }
                }
            }

            root.setOnLongClickListener {
                if (actionMode == null) {
                    isSelectionMode = true
                    toggleItemSelection(message, position)
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

    private fun setupReceivedMessageView(
        messageBinding: ItemMessageBinding,
        message: MessageNew,
        position: Int
    ) {
        messageBinding.apply {
            with(ConstraintSet()) {
                clone(threadMessageHolder)
                clear(threadMessageWrapper.id, ConstraintSet.END)
                connect(
                    threadMessageWrapper.id,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                applyTo(threadMessageHolder)
            }
            msgDateTime.apply {
                text = (message.date / 1000).toInt().formatDateOrTime(
                    baseActivity,
                    hideTimeAtOtherDays = false,
                    showYearEvenIfCurrent = false,
                    true
                )
            }
            threadMessageBody.apply {
                /* setTextColor(receiveTextColor)
                 background =
                     ContextCompat.getDrawable(baseActivity, R.drawable.item_received_background_)*/
                background = if (isSelectionMode) {
                    if (isSelected(message.contentId)) {
                        setTextColor(selectedTextColor)
                        ContextCompat.getDrawable(
                            baseActivity,
                            R.drawable.item_msg_received_selected_background_
                        )
                    } else {
                        setTextColor(receiveTextColor)
                        ContextCompat.getDrawable(
                            baseActivity,
                            R.drawable.item_received_background_
                        )
                    }
                } else {
                    setTextColor(receiveTextColor)
                    ContextCompat.getDrawable(baseActivity, R.drawable.item_received_background_)
                }
            }

            val layoutParams = RelativeLayout.LayoutParams(
                ivStar.layoutParams.width,
                ivStar.layoutParams.height
            )
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            layoutParams.addRule(RelativeLayout.BELOW, R.id.threadMessageBody)


            ivStar.layoutParams = layoutParams

            val layoutParamsN = RelativeLayout.LayoutParams(
                threadSending.layoutParams.width,
                threadSending.layoutParams.height
            )
            layoutParamsN.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            layoutParamsN.addRule(RelativeLayout.ALIGN_PARENT_START)
            layoutParamsN.addRule(RelativeLayout.BELOW, R.id.ivStar)
            threadSending.layoutParams = layoutParamsN

            threadSending.text = when {
                message.isSending() -> baseActivity.getString(R.string.sending)
                message.isFailedMessage() -> baseActivity.getString(R.string.message_not_sent_touch_retry)
                else -> ""
            }
            threadSending.setVisible(
                when {
                    message.isSending() -> true
                    message.isFailedMessage() -> true
                    message.isDelivered() -> false
                    else -> false
                }
            )

            val previous = if (position == 0) null else getItem(position - 1)
            val timeSincePrevious = message.date - (previous?.date ?: 0)
            val squareRootOfDifference =
                kotlin.math.sqrt(timeSincePrevious.toDouble() * timeSincePrevious.toDouble())

            if (squareRootOfDifference > 84600) {
                threadDateTimeHolder.visible()
                threadDateTime.apply {
                    text = (message.date / 1000).toInt().formatDateOrTime(
                        context,
                        hideTimeAtOtherDays = false,
                        showYearEvenIfCurrent = false,
                        true
                    )
                }
                threadSimIcon.gone()
                threadSimNumber.gone()
            } else {
                threadDateTimeHolder.gone()
            }

            if (message.isStarred) {
                ivStar.visible()
            } else {
                ivStar.gone()
            }
        }
    }

    private fun setupSentMessageView(
        messageBinding: ItemMessageBinding,
        message: MessageNew,
        position: Int
    ) {
        messageBinding.apply {
            with(ConstraintSet()) {
                clone(threadMessageHolder)
                clear(threadMessageWrapper.id, ConstraintSet.START)
                connect(
                    threadMessageWrapper.id,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
                applyTo(threadMessageHolder)
            }
            msgDateTime.apply {
                updateLayoutParams<RelativeLayout.LayoutParams> {
                    removeRule(RelativeLayout.END_OF)
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                }
                text = (message.date / 1000).toInt().formatDateOrTime(
                    baseActivity,
                    hideTimeAtOtherDays = false,
                    showYearEvenIfCurrent = false,
                    true
                )
            }
            threadMessageBody.apply {
                updateLayoutParams<RelativeLayout.LayoutParams> {
                    removeRule(RelativeLayout.END_OF)
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                }
                /* background =
                     ContextCompat.getDrawable(baseActivity, R.drawable.item_sent_background_)
                 setTextColor(sentTextColor)*/

                background = if (isSelectionMode) {
                    if (isSelected(message.contentId)) {
                        setTextColor(selectedTextColor)
                        ContextCompat.getDrawable(
                            baseActivity,
                            R.drawable.item_msg_sent_selected_background_
                        )
                    } else {
                        setTextColor(sentTextColor)
                        ContextCompat.getDrawable(baseActivity, R.drawable.item_sent_background_)
                    }
                } else {
                    setTextColor(sentTextColor)
                    ContextCompat.getDrawable(baseActivity, R.drawable.item_sent_background_)
                }
            }

            // Create RelativeLayout.LayoutParams for ivStar
            val layoutParams = RelativeLayout.LayoutParams(
                ivStar.layoutParams.width,
                ivStar.layoutParams.height
            )
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams.addRule(RelativeLayout.BELOW, R.id.threadMessageBody)
            ivStar.layoutParams = layoutParams


            val layoutParamsN = RelativeLayout.LayoutParams(
                threadSending.layoutParams.width,
                threadSending.layoutParams.height
            )
            layoutParamsN.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            layoutParamsN.addRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParamsN.addRule(RelativeLayout.BELOW, R.id.ivStar)
            threadSending.layoutParams = layoutParamsN

            threadSending.text = when {
                message.isSending() -> baseActivity.getString(R.string.sending)
                message.isFailedMessage() -> baseActivity.getString(R.string.message_not_sent_touch_retry)
                else -> ""
            }
            threadSending.setVisible(
                when {
                    message.isSending() -> true
                    message.isFailedMessage() -> true
                    message.isDelivered() -> false
                    else -> false
                }
            )

            val previous = if (position == 0) null else getItem(position - 1)
            val timeSincePrevious = message.date - (previous?.date ?: 0)
            val squareRootOfDifference =
                kotlin.math.sqrt(timeSincePrevious.toDouble() * timeSincePrevious.toDouble())
            if (squareRootOfDifference > 86400) {
                threadDateTimeHolder.visible()
                threadDateTime.apply {
                    text = (message.date / 1000).toInt().formatDateOrTime(
                        context,
                        hideTimeAtOtherDays = false,
                        showYearEvenIfCurrent = false,
                        true
                    )
                }
                threadSimIcon.gone()
                threadSimNumber.gone()
            } else {
                threadDateTimeHolder.gone()
            }


            if (message.isStarred) {
                ivStar.visible()
            } else {
                ivStar.gone()
            }
        }
    }


    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    private fun setStarView() {
        // Create RelativeLayout.LayoutParams for ivStar
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

// Set rules to position ivStar to the right bottom corner of threadMessageBody
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
        layoutParams.addRule(RelativeLayout.BELOW, R.id.threadMessageBody)

// Set margins if needed
//        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen._your_bottom_margin)
//        layoutParams.endMargin = resources.getDimensionPixelSize(R.dimen._your_end_margin)

// Apply the layout params to ivStar
//        ivStar.layoutParams = layoutParams
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.isReceivedMessage()!!) THREAD_RECEIVED_MESSAGE else THREAD_SENT_MESSAGE
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        setupView(holder, holder.itemView, item!!, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(baseActivity.layoutInflater, parent, false)
        return ViewHolder(binding)
    }

}
