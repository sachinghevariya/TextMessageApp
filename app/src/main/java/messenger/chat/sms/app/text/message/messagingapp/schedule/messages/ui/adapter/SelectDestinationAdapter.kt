package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.ItemSelectDestinationBinding

class SelectDestinationAdapter(
    private val callBack: (item: String) -> Unit
) : BaseQuickAdapter<String, SelectDestinationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSelectDestinationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, item: String?) {
        setupView(holder.itemView, item!!, position)
    }

    private fun setupView(view: View, item: String, position: Int) {
        ItemSelectDestinationBinding.bind(view).apply {
            tvName.isSelected = true
            tvName.apply {
                text = item
            }

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
            ItemSelectDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

}