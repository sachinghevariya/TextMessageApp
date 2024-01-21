package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.receivers

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.Keep
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getContactsFromCursorN
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.getThreadId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.queryCursor
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.UpdateContacts
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Contact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.CursorToContactImpl
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.PhoneNumberUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.Recipient
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm.map
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getLongValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.viewmodels.ConversationViewModel
import org.greenrobot.eventbus.EventBus

class MyContactObserver(
    private val mContext: Context,
    private val viewModel: ConversationViewModel
) : ContentObserver(null) {

//    private var initialContactsState: Map<Long, ContactData> = loadInitialContactsState()

    init {
        mContext.contentResolver.registerContentObserver(
            Uri.parse("content://com.android.contacts/contacts"),
            true,
            this
        )
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        syncContacts()
//        CoroutineScope(Dispatchers.IO).launch {
//            // Query the current state of contacts
//            val currentContactsState = loadCurrentContactsState()
//
//            // Detect changes
//            val addedContacts = mutableListOf<ContactData>()
//            val deletedContacts = mutableListOf<ContactData>()
//            val modifiedContacts = mutableListOf<ContactData>()
//
//            for (contactId in currentContactsState.keys) {
//                val currentContact = currentContactsState[contactId]
//                val initialContact = initialContactsState[contactId]
//
//                if (initialContact == null) {
//                    // Contact added
//                    addedContacts.add(currentContact!!)
//                } else if (initialContact != currentContact) {
//                    // Contact modified
//                    modifiedContacts.add(currentContact!!)
//                }
//            }
//
//            for (contactId in initialContactsState.keys) {
//                if (!currentContactsState.containsKey(contactId)) {
//                    // Contact deleted
//                    deletedContacts.add(initialContactsState[contactId]!!)
//                }
//            }
//            // Update the initial state for the next comparison
//            initialContactsState = currentContactsState
//
//            deletedContacts.forEach { item ->
//                viewModel.getContactsFromDbLocal().forEach {
//                    it.threadId = mContext.getThreadId(setOf(it.phoneNumbers[0].normalizedNumber))
//                    if (it.contactId == item.id.toInt()) {
//                        val conversation = viewModel.getConversationByThreadId(it.threadId)
//                        if (conversation != null) {
//                            val listMessage = viewModel.getMessagesByThreadId(it.threadId)
//                            conversation.title = conversation.phoneNumber
//                            conversation.photoUri = ""
////                            val oldItem = conversation.lastSnippet[0].participants[0]
////                            val participantsList = arrayListOf<SimpleContact>()
////                            oldItem.name = conversation.phoneNumber
////                            oldItem.photoUri = ""
////                            participantsList.add(oldItem)
////                            conversation.lastSnippet[0].participants = participantsList
//                            viewModel.updateConversation(conversation)
//
//                            listMessage.forEach { msg ->
//                                val msgParticipants = msg.participants
//                                msgParticipants.forEach { par ->
//                                    par.name = conversation.phoneNumber
//                                    par.photoUri = ""
//                                }
//
//                                msg.participants = msgParticipants
//                                msg.senderName = it.name
//                                msg.senderPhotoUri = it.photoUri
//                                viewModel.updateMessages(msg)
//                            }
//
//                        }
//
//                        viewModel.deleteContact(it)
//                    }
//                }
//            }
//
//            modifiedContacts.forEach { item ->
//                mContext.getContactsFromCursorN(item.id).forEach {
//                    if (it.contactId == item.id.toInt()) {
//                        val conversation = viewModel.getConversationByThreadId(it.threadId)
//                        if (conversation != null) {
//                            val listMessage = viewModel.getMessagesByThreadId(it.threadId)
//                            conversation.title = it.name
//                            conversation.photoUri = it.photoUri
////                            val participantsList = arrayListOf<SimpleContact>()
////                            participantsList.add(it)
////                            conversation.lastSnippet[0].participants = participantsList
//                            viewModel.updateConversation(conversation)
//
//                            listMessage.forEach { msg ->
//                                val msgParticipants = msg.participants
//                                msgParticipants.forEach { par ->
//                                    par.name = it.name
//                                    par.photoUri = it.photoUri
//                                }
//
//                                msg.participants = msgParticipants
//                                msg.senderName = it.name
//                                msg.senderPhotoUri = it.photoUri
//                                viewModel.updateMessages(msg)
//                            }
//
//                        }
//                        viewModel.insertOrUpdateContact(it)
//                    }
//                }
//            }
//
//            addedContacts.forEach { item ->
//                mContext.getContactsFromCursorN(item.id).forEach {
//                    it.threadId = mContext.getThreadId(setOf(it.phoneNumbers[0].normalizedNumber))
//                    if (it.contactId == item.id.toInt()) {
//                        val conversation = viewModel.getConversationByThreadId(it.threadId)
//                        if (conversation != null) {
//                            val listMessage = viewModel.getMessagesByThreadId(it.threadId)
//                            conversation.title = it.name
//                            conversation.photoUri = it.photoUri
////                            val participantsList = arrayListOf<SimpleContact>()
////                            participantsList.add(it)
////                            conversation.lastSnippet[0].participants = participantsList
//                            viewModel.updateConversation(conversation)
//
//                            listMessage.forEach { msg ->
//                                val msgParticipants = msg.participants
//                                msgParticipants.forEach { par ->
//                                    par.name = it.name
//                                    par.photoUri = it.photoUri
//                                }
//
//                                msg.participants = msgParticipants
//                                msg.senderName = it.name
//                                msg.senderPhotoUri = it.photoUri
//                                viewModel.updateMessages(msg)
//                            }
//                        }
//
//                        if (it.name == null || it.name.isEmpty()) {
//
//                        } else {
//                            viewModel.insertOrUpdateContact(it)
//                        }
////                        viewModel.insertOrUpdateContact(it)
//                    }
//                }
//            }
//        }
    }

    private fun syncContacts() {
        // Load all the contacts
        Log.e("TAG", "syncContacts: ")
        val phoneNumberUtils = PhoneNumberUtils(mContext)
        var contacts = getContacts()

        Realm.getDefaultInstance()?.use { realm ->
            val recipients = realm.where(Recipient::class.java).findAll()

            realm.executeTransaction {
                realm.delete(Contact::class.java)
                contacts = realm.copyToRealmOrUpdate(contacts)
                // Update all the recipients with the new contacts
                recipients.forEach { recipient ->
                    recipient.contact = contacts.find { contact ->
                        contact.numbers.any { phoneNumberUtils.compare(recipient.address, it.address) }
                    }
                }

                realm.insertOrUpdate(recipients)
            }
        }
        EventBus.getDefault().post(UpdateContacts())
    }

    private fun getContacts(): List<Contact> {
        val defaultNumberIds = Realm.getDefaultInstance().use { realm ->
            realm.where(PhoneNumber::class.java)
                .equalTo("isDefault", true)
                .findAll()
                .map { number -> number.id }
        }
        val phoneNumberUtils = PhoneNumberUtils(mContext)
        val cursorToContact = CursorToContactImpl(mContext)
        return cursorToContact.getContactsCursor()
            ?.map { cursor -> cursorToContact.map(cursor) }
            ?.groupBy { contact -> contact.lookupKey }
            ?.map { contacts ->
                // Sometimes, contacts providers on the phone will create duplicate phone number entries. This
                // commonly happens with Whatsapp. Let's try to detect these duplicate entries and filter them out
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contacts.value
                    .flatMap { it.numbers }
                    .forEach { number ->
                        number.isDefault = defaultNumberIds.any { id -> id == number.id }
                        val duplicate = uniqueNumbers.find { other ->
                            phoneNumberUtils.compare(number.address, other.address)
                        }

                        if (duplicate == null) {
                            uniqueNumbers += number
                        } else if (!duplicate.isDefault && number.isDefault) {
                            duplicate.isDefault = true
                        }
                    }

                contacts.value.first().apply {
                    numbers.clear()
                    numbers.addAll(uniqueNumbers)
                }
            } ?: listOf()
    }

    /*private fun loadInitialContactsState(): Map<Long, ContactData> {
        val initialContacts = mutableMapOf<Long, ContactData>()
        mContext.queryCursor(
            ContactsContract.Contacts.CONTENT_URI,
            emptyArray(),
            null,
            null
        ) { cursor ->
            val contactId = cursor.getLongValue(ContactsContract.Contacts._ID)
            val contactName = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
            val contactPhoto = cursor.getStringValue(ContactsContract.Contacts.PHOTO_URI)
            if (contactName != null) {
                if (contactPhoto == null) {
                    val contactData = ContactData(contactId, contactName, "", "")
                    initialContacts[contactId] = contactData
                } else {
                    val contactData = ContactData(contactId, contactName, "", contactPhoto)
                    initialContacts[contactId] = contactData
                }
            }
        }
        return initialContacts
    }

    private fun loadCurrentContactsState(): Map<Long, ContactData> {
        val currentContacts = mutableMapOf<Long, ContactData>()
        mContext.queryCursor(
            ContactsContract.Contacts.CONTENT_URI,
            emptyArray(),
            null,
            null
        ) { cursor ->
            val contactId = cursor.getLongValue(ContactsContract.Contacts._ID)
            val contactName = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
            val contactPhoto = cursor.getStringValue(ContactsContract.Contacts.PHOTO_URI)
            if (contactName != null) {
                if (contactPhoto == null) {
                    val contactData = ContactData(contactId, contactName, "", "")
                    currentContacts[contactId] = contactData
                } else {
                    val contactData = ContactData(contactId, contactName, "", contactPhoto)
                    currentContacts[contactId] = contactData
                }
            }
        }
        return currentContacts
    }

    @Keep
    data class ContactData(
        val id: Long,
        val name: String,
        val phoneNumber: String,
        val photoUri: String,
    )*/
}