package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.content.Context
import android.provider.ContactsContract
import android.text.TextUtils
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getIntValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.normalizePhoneNumber

fun Context.getContactsFromCursorN(contactId: Long?): List<SimpleContact> {
    // Retrieve contacts from the cursor, removing duplicates
    val contactsMap = getUniqueContactsFromCursor(contactId)

    // Apply names and photo URIs to the contacts
    applyNamesAndPhotos(contactsMap.toMutableMap(), contactId)

    // Remove duplicates based on specific criteria
    val filteredContacts = removeDuplicates(contactsMap.toMutableMap())
    // Sort and return the filtered contacts
    return filteredContacts.values.toList().sorted()
}

private fun Context.getUniqueContactsFromCursor(contactId: Long?): Map<Int, SimpleContact> {
    val contactsMap = mutableMapOf<Int, SimpleContact>()
    val phoneNumbers = getContactPhoneNumbers(contactId)
    for (contact in phoneNumbers) {
        val rawId = contact.rawId
        contactsMap[rawId] = contact
    }
    // Apply names and photo URIs to the contacts
    applyNamesAndPhotos(contactsMap, contactId)
    return contactsMap
}

private fun Context.applyNamesAndPhotos(
    contactsMap: MutableMap<Int, SimpleContact>,
    contactId: Long?
) {
    val names = getContactNames(contactId)

    for ((rawId, contact) in contactsMap) {
        val nameInfo = names.firstOrNull { it.rawId == rawId }
        nameInfo?.let {
            contact.name = it.name
            contact.photoUri = it.photoUri
        }
    }
}

private fun removeDuplicates(contactsMap: MutableMap<Int, SimpleContact>): Map<Int, SimpleContact> {
    val filteredContacts = contactsMap.values
        .filter { it.name.trim().isNotEmpty() }
        .distinctBy {
            val startIndex = Math.max(0, it.phoneNumbers.first().normalizedNumber.length - 9)
            it.phoneNumbers.first().normalizedNumber.substring(startIndex)
        }
        .distinctBy { it.rawId }
        .toMutableList() as ArrayList<SimpleContact>

    // Create a map from the filtered list
    val resultMap = filteredContacts.associateBy { it.rawId }
    return resultMap
}

fun Context.getContactPhoneNumbers(contactId: Long?): ArrayList<SimpleContact> {
    val contacts = ArrayList<SimpleContact>()
    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    val projection = arrayOf(
        ContactsContract.Data.RAW_CONTACT_ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.TYPE,
        ContactsContract.CommonDataKinds.Phone.LABEL,
        ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
    )

    val selection: String?
    val selectionArgs: Array<String>?

    if (contactId != null) {
        selection = "${ContactsContract.Data.CONTACT_ID} = ?"
        selectionArgs = arrayOf(contactId.toString())
    } else {
        selection = null
        selectionArgs = null
    }

    queryCursor(uri, projection, selection, selectionArgs) { cursor ->
        val normalizedNumber =
            cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                ?: cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    ?.normalizePhoneNumber() ?: return@queryCursor

        val rawId = cursor.getIntValue(ContactsContract.Data.RAW_CONTACT_ID)
        val contactId = cursor.getIntValue(ContactsContract.Data.CONTACT_ID)
        val type = cursor.getIntValue(ContactsContract.CommonDataKinds.Phone.TYPE)
        val label = cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.LABEL) ?: ""
        val isPrimary =
            cursor.getIntValue(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY) != 0
        if (contacts.firstOrNull { it.rawId == rawId } == null) {
            val contact =
                SimpleContact(rawId, contactId, "", "", ArrayList(), ArrayList(), ArrayList())
            contacts.add(contact)
        }

        val phoneNumber =
            PhoneNumber(normalizedNumber, type, label, normalizedNumber, isPrimary)
        val phoneNumbers =
            contacts.firstOrNull { it.rawId == rawId }?.phoneNumbers!! as ArrayList<PhoneNumber>
        phoneNumbers.add(phoneNumber)
    }
    return contacts
}

fun Context.getContactNames(contactId: Long?): List<SimpleContact> {
    val contacts = ArrayList<SimpleContact>()
    val startNameWithSurname = false
    val uri = ContactsContract.Data.CONTENT_URI
    val projection = arrayOf(
        ContactsContract.Data.RAW_CONTACT_ID,
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.CommonDataKinds.StructuredName.PREFIX,
        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
        ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
        ContactsContract.CommonDataKinds.StructuredName.SUFFIX,
        ContactsContract.CommonDataKinds.StructuredName.PHOTO_THUMBNAIL_URI,
        ContactsContract.CommonDataKinds.Organization.COMPANY,
        ContactsContract.CommonDataKinds.Organization.TITLE,
        ContactsContract.Data.MIMETYPE
    )

    val selection: String
    val selectionArgs: Array<String>

    if (contactId != null) {
        selection =
            "(${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?) AND ${ContactsContract.Data.CONTACT_ID} = ?"
        selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
            contactId.toString()
        )
    } else {
        selection =
            "(${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?)"
        selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )
    }


    queryCursor(uri, projection, selection, selectionArgs) { cursor ->
        val rawId = cursor.getIntValue(ContactsContract.Data.RAW_CONTACT_ID)
        val contactId = cursor.getIntValue(ContactsContract.Data.CONTACT_ID)
        val mimetype = cursor.getStringValue(ContactsContract.Data.MIMETYPE)
        val photoUri =
            cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.PHOTO_THUMBNAIL_URI)
                ?: ""
        val isPerson =
            mimetype == ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        if (isPerson) {
            val prefix =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX)
                    ?: ""
            val firstName =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                    ?: ""
            val middleName =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                    ?: ""
            val familyName =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                    ?: ""
            val suffix =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)
                    ?: ""
            if (firstName.isNotEmpty() || middleName.isNotEmpty() || familyName.isNotEmpty()) {
                val names = if (startNameWithSurname) {
                    arrayOf(
                        prefix,
                        familyName,
                        middleName,
                        firstName,
                        suffix
                    ).filter { it.isNotEmpty() }
                } else {
                    arrayOf(
                        prefix,
                        firstName,
                        middleName,
                        familyName,
                        suffix
                    ).filter { it.isNotEmpty() }
                }

                val fullName = TextUtils.join(" ", names)
                val contact = SimpleContact(
                    rawId,
                    contactId,
                    fullName,
                    photoUri,
                    ArrayList(),
                    ArrayList(),
                    ArrayList()
                )
                contacts.add(contact)
            }
        }

        val isOrganization =
            mimetype == ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        if (isOrganization) {
            val company =
                cursor.getStringValue(ContactsContract.CommonDataKinds.Organization.COMPANY)
                    ?: ""
            val jobTitle =
                cursor.getStringValue(ContactsContract.CommonDataKinds.Organization.TITLE) ?: ""
            if (company.isNotEmpty() || jobTitle.isNotEmpty()) {
                val fullName = "$company $jobTitle".trim()
                val contact = SimpleContact(
                    rawId,
                    contactId,
                    fullName,
                    photoUri,
                    ArrayList(),
                    ArrayList(),
                    ArrayList()
                )
                contacts.add(contact)
            }
        }
    }
    return contacts
}