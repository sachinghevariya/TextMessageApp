package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemConversationNewBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.SearchResult
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.formatDateOrTime
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone

class SearchAdapter(
    var baseActivity: AppCompatActivity,
    val myPreferences: MyPreferences,
    private val callBack: (item: SearchResult) -> Unit
) : BaseQuickAdapter<SearchResult, SearchAdapter.ViewHolder>() {
    val  phoneNumberUtils = PhoneNumberUtils(baseActivity)

    inner class ViewHolder(val binding: ItemConversationNewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SearchResult?) {
        setupView(holder.itemView, item!!, position)
    }

    private fun setupView(view: View, mConversation: SearchResult, position: Int) {
        ItemConversationNewBinding.bind(view).apply {
            val result = getItem(position)
            val title = SpannableString(result?.conversation?.getTitle())
            conversationAddress.isSelected = true
            conversationDate.isSelected = true
            draftIndicator.isSelected = true
            conversationBodyShort.isSelected = true
            pinIndicator.gone()

            val lastMessage = result?.conversation?.lastMessage
            val recipient = when {
                result?.conversation?.recipients?.size == 1 || lastMessage == null -> result?.conversation?.recipients?.firstOrNull()
                else -> result?.conversation?.recipients?.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, lastMessage.address)
                }
            }
            var photoUri = ""
            photoUri = if (recipient?.contact?.photoUri == null) {
                ""
            } else {
                recipient.contact?.photoUri!!
            }
            conversationImage.title = Pair(result?.conversation?.getTitle(), photoUri)

            conversationAddress.apply {
                text = title
            }

            conversationBodyShort.apply {
                text = when (result?.conversation?.me) {
                    true -> context.getString(R.string.you)+": " +result.conversation.snippet
                    false -> result.conversation.snippet
                    else -> {""}
                }
                conversationDate.apply {
                    text = (result?.conversation?.date!!/1000).toInt().formatDateOrTime(
                        context,
                        hideTimeAtOtherDays = true,
                        showYearEvenIfCurrent = false,
                        hideSevenDays = false
                    )
                }

                conversationBodyShort.alpha = 1f
                conversationDate.alpha = 1f
                conversationRead.gone()

//                val placeholder = null
//                loadContactImage(
//                    baseActivity,
//                    mConversation.photoUri,
//                    conversationImage,
//                    mConversation.title,
//                    placeholder,
//                    myPreferences.showProfilePhoto
//                )

                imgSelect.gone()
                root.setOnClickListener {
                    callBack.invoke(
                        mConversation
                    )
                }

            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemConversationNewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

}