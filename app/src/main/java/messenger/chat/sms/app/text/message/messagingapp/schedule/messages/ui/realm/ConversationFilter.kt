package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

class ConversationFilter constructor(private val recipientFilter: RecipientFilter) : Filter<ConversationNew>() {

    override fun filter(item: ConversationNew, query: CharSequence): Boolean {
        return item.recipients.any { recipient -> recipientFilter.filter(recipient, query) }
    }

}