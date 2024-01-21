package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.dialog

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.databinding.DialogSelectDestinationBinding
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.adapter.SelectDestinationAdapter
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.base.BaseDialog
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.CallbackString
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class DialogSelectDestination : BaseDialog<DialogSelectDestinationBinding>() {

    private var phoneNumbers = emptyList<String>()

    override fun getViewBinding() = DialogSelectDestinationBinding.inflate(layoutInflater)

    companion object {
        fun newInstance(): DialogSelectDestination {
            return DialogSelectDestination()
        }
    }

    override fun init() {
        if (arguments != null && requireArguments().containsKey("KEY_LIST")) {
            phoneNumbers = (requireArguments().getSerializable("KEY_LIST") as? List<String>)!!
        }
        setUpAdapter()
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpAdapter() {
        binding.rvListMobileNumber.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        val mAdapter = SelectDestinationAdapter {
            val tempAddress = StringBuilder().append(it).toString()
            EventBus.getDefault().post(CallbackString(tempAddress))
            dismiss()
        }
        binding.rvListMobileNumber.adapter = mAdapter
        mAdapter.submitList(phoneNumbers)
    }


}