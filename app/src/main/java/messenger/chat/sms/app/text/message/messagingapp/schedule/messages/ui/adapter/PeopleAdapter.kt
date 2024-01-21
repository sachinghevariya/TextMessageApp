package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemPeopleBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.loadContactImage

class PeopleAdapter(
    private var baseActivity: AppCompatActivity,
    val myPreferences: MyPreferences,
    private val callBack: (item: Contact) -> Unit
) : BaseQuickAdapter<Contact, PeopleAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPeopleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: Contact?) {
        setupView(holder.itemView, item!!, position)
    }

    private fun setupView(view: View, item: Contact, position: Int) {
        ItemPeopleBinding.bind(view).apply {
            tvName.isSelected = true
            tvName.apply {
                text = item.name
            }
            loadContactImage(
                baseActivity,
                item.photoUri?:"",
                ivContact,
                item.name,
                null,
                myPreferences.showProfilePhoto
            )

            root.setOnClickListener {
                callBack.invoke(
                    item
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
            ItemPeopleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

}