package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter4.BaseQuickAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.ConversationNew
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.inVisible
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.visible
import plugin.adsdk.service.BaseActivity

class StarredAdapter(
    var baseActivity: BaseActivity,
    val myPreferences: MyPreferences,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val callBack: (item: ConversationNew?, unStarredEvent: Boolean) -> Unit
) : BaseQuickAdapter<ConversationNew, StarredAdapter.ViewHolder>() {

    val textColor = ContextCompat.getColor(baseActivity, R.color.textColor)

    inner class ViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root)


    private fun setupView(
        view: ItemConversationNewBinding,
        mConversationNew: ConversationNew,
        position: Int
    ) {
        view.apply {
            val lastMessage = mConversationNew.lastMessage
            val recipient = when {
                mConversationNew.recipients.size == 1 || lastMessage == null -> mConversationNew.recipients.firstOrNull()
                else -> mConversationNew.recipients.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, lastMessage.address)
                }
            }
            conversationAddress.isSelected = true
            conversationDate.isSelected = true
            draftIndicator.isSelected = true
            conversationBodyShort.isSelected = true
            if (mConversationNew.pinned) {
                pinIndicator.visible()
            } else {
                pinIndicator.gone()
            }

            conversationAddress.apply {
                text = mConversationNew.getTitle()
            }

            conversationBodyShort.apply {
                text = mConversationNew.snippet
            }

            conversationDate.apply {
                text = (mConversationNew.date / 1000).toInt().formatDateOrTime(
                    context, true,
                    showYearEvenIfCurrent = false,
                    hideSevenDays = false
                )
            }

            var photoUri = ""
            photoUri = if (recipient?.contact?.photoUri == null) {
                ""
            } else {
                recipient.contact?.photoUri!!
            }
            conversationImage.title = Pair(mConversationNew.getTitle(), photoUri)
            val style = if (!mConversationNew.unread) {
                conversationBodyShort.alpha = 0.7f
                conversationDate.alpha = 0.7f
                conversationRead.inVisible()
            } else {
                conversationBodyShort.alpha = 1f
                conversationDate.alpha = 1f
                conversationRead.visible()
            }

            imgSelect.gone()
            arrayListOf(conversationAddress, conversationBodyShort, conversationDate).forEach {
                it.setTextColor(textColor)
            }

            ivStar.visible()

            ivStar.setOnClickListener(1000L) {
                callBack.invoke(
                    mConversationNew,
                    true
                )
            }

            root.setOnClickListener(1000L) {
                callBack.invoke(
                    mConversationNew,
                    false
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
        if (item?.id != 0L && item?.lastMessage != null) {
            val view = holder.binding as ItemConversationNewBinding
            setupView(view, item, position)
        }
    }

}