package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.removeAccents

class ContactFilter constructor(private val phoneNumberFilter: PhoneNumberFilter) : Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        return item.name.removeAccents().contains(query, true) || // Name
                item.numbers.map { it.address }.any { address -> phoneNumberFilter.filter(address, query) } // Number
    }

}
