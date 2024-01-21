package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseQuickAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.AdLayoutNativeSmallBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.inVisible
import plugin.adsdk.service.AdsUtility
import plugin.adsdk.service.BaseActivity

open class ScheduleAdapter(
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    val isFromBlockList: Boolean = false,
    private val isFromSchedule: Boolean = false,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val actionCallback: () -> Unit,
    private val callBack: (item: ConversationNew?, deleteEvent: Boolean, archiveEvent: Boolean, pinEvent: Boolean, unPinEvent: Boolean, markAsReadEvent: Boolean, markAsUnReadEvent: Boolean, blockEvent: Boolean, unblockEvent: Boolean, selectedItems: List<ConversationNew>) -> Unit
) : BaseQuickAdapter<ConversationNew, ScheduleAdapter.ViewHolder>() {

    val textColor = ContextCompat.getColor(baseActivity, R.color.textColor)

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
            pinIndicator.gone()

            conversationAddress.apply {
                text = mConversation.getTitle()
            }

            conversationBodyShort.apply {
                text = when {
                    mConversation.draft.isNotEmpty() -> mConversation.draft
                    mConversation.me -> "${context.getString(R.string.you)}: " + mConversation.lastMessage?.body
                    else -> mConversation.lastMessage?.body
                }
            }

            conversationDate.apply {
                text = (mConversation.date / 1000).toInt().formatDateOrTime(
                    context, true,
                    showYearEvenIfCurrent = false,
                    hideSevenDays = false
                )
            }

            conversationBodyShort.alpha = 0.7f
            conversationDate.alpha = 0.7f
            conversationRead.inVisible()

            imgSelect.gone()
            arrayListOf(conversationAddress, conversationBodyShort, conversationDate).forEach {
                it.setTextColor(textColor)
            }

            root.setOnClickListener {
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
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemConversationNewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: ConversationNew?) {
        if (item?.id != 0L) {
            val view = holder.binding as ItemConversationNewBinding
            setupView(view, item!!, position)
        }
    }


}