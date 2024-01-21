package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

class PhoneNumberFilter constructor(
    private val phoneNumberUtils: PhoneNumberUtils
) : Filter<String>() {

    override fun filter(item: String, query: CharSequence): Boolean {
        val allCharactersDialable = query.all { phoneNumberUtils.isReallyDialable(it) }
        return allCharactersDialable && (phoneNumberUtils.compare(item, query.toString()) ||
                phoneNumberUtils.normalizeNumber(item).contains(phoneNumberUtils.normalizeNumber(query.toString())))
    }

}