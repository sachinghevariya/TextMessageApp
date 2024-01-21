package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemContactBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener

class ContactsAdapter(
    private var baseActivity: AppCompatActivity,
    var myPreferences: MyPreferences,
    private val callBack: (item: SimpleContact) -> Unit
) : BaseDifferAdapter<SimpleContact, ContactsAdapter.ViewHolder>(diffCallback = DiffCallback()) {

    inner class ViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: SimpleContact?) {
        setupView(holder.itemView, item!!)
    }

    private fun setupView(view: View, contact: SimpleContact) {
        ItemContactBinding.bind(view).apply {
            itemContactName.apply {
                text = contact.name
            }
            itemContactNumber.apply {
                text = TextUtils.join(", ", contact.phoneNumbers.map { it.normalizedNumber })
            }
            loadContactImage(
                baseActivity,
                contact.photoUri,
                itemContactImage,
                contact.name,
                null,
                myPreferences.showProfilePhoto
            )
            root.setOnClickListener(1000L) {
                callBack.invoke(contact)
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    class DiffCallback : DiffUtil.ItemCallback<SimpleContact>() {
        override fun areItemsTheSame(oldItem: SimpleContact, newItem: SimpleContact): Boolean {
            return SimpleContact.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: SimpleContact, newItem: SimpleContact): Boolean {
            return SimpleContact.areContentsTheSame(oldItem, newItem)
        }
    }

}