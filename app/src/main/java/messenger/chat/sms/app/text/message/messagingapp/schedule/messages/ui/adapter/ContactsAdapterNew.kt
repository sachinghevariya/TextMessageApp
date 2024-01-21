package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemContactBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setOnClickListener

class ContactsAdapterNew(
    var data: ArrayList<Contact> = arrayListOf<Contact>(),
    private var baseActivity: AppCompatActivity,
    var myPreferences: MyPreferences,
    private val callBack: (item: Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapterNew.ViewHolder>() {

    inner class ViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)


    private fun setupView(view: View, contact: Contact) {
        ItemContactBinding.bind(view).apply {
            itemContactName.apply {
                text = contact.name
            }
            itemContactNumber.apply {
                text = contact.numbers[0]?.address
//                text = TextUtils.join(", ", contact.phoneNumbers.map { it.normalizedNumber })
            }
            loadContactImage(
                baseActivity,
                contact.photoUri ?: "",
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

    fun updateData(data: ArrayList<Contact>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data.get(position)
        setupView(holder.itemView, item!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }


}