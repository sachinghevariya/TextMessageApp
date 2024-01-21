package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.MessagingUtils.Companion.ADDRESS_SEPARATOR
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BlockedNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.MessageType
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass.Companion.MESSAGES_LIMIT
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.areDigitsOnly
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getIntValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getLongValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.hasPermission
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.isPhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.showToast
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.trimToComparableNumber
import java.util.Locale

fun Context.getThreadPhoneNumbers(recipientIds: List<Int>): ArrayList<String> {
    val numbers = ArrayList<String>()
    recipientIds.forEach {
        numbers.add(getPhoneNumberFromAddressId(it))
    }
    return numbers
}

fun Context.getPhoneNumberFromAddressId(canonicalAddressId: Int): String {
    val uri = Uri.withAppendedPath(Telephony.MmsSms.CONTENT_URI, "canonical-addresses")
    val projection = arrayOf(
        Telephony.Mms.Addr.ADDRESS
    )

    val selection = "${Telephony.Mms._ID} = ?"
    val selectionArgs = arrayOf(canonicalAddressId.toString())
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(Telephony.Mms.Addr.ADDRESS)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun Context.queryCursorUnsafe(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    callback: (cursor: Cursor) -> Unit
) {
    val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
    cursor?.use {
        if (cursor.moveToFirst()) {
            do {
                callback(cursor)
            } while (cursor.moveToNext())
        }
    }
}

fun Context.getThreadId(address: Set<String>): Long {
    return try {
        Telephony.Threads.getOrCreateThreadId(this, address)
    } catch (e: Exception) {
        0L
    }
}


fun Context.getThreadContactNames(phoneNumbers: List<String>): ArrayList<String> {
    val names = ArrayList<String>()
    phoneNumbers.forEach { number ->
        val name = getNameFromPhoneNumber(number)
        names.add(name)
    }
    return names
}

fun Context.getNameFromPhoneNumber(number: String): String {
    if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
        return number
    }

    val uri =
        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    val projection = arrayOf(
        ContactsContract.PhoneLookup.DISPLAY_NAME
    )

    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor.use {
            if (cursor?.moveToFirst() == true) {
                return cursor.getStringValue(ContactsContract.PhoneLookup.DISPLAY_NAME)
            }
        }
    } catch (ignored: Exception) {
    }

    return number
}


fun Context.getNameAndPhotoFromPhoneNumber(number: String): Pair<String, String> {
    if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
        return Pair(number, "")
    }

    val uri =
        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    val projection = arrayOf(
        ContactsContract.PhoneLookup.DISPLAY_NAME,
        ContactsContract.PhoneLookup.PHOTO_URI
    )

    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor.use {
            if (cursor?.moveToFirst() == true) {
                val name = cursor.getStringValue(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoUri = cursor.getStringValue(ContactsContract.PhoneLookup.PHOTO_URI) ?: ""
                return Pair(name, photoUri)
            }
        }
    } catch (ignored: Exception) {
    }

    return Pair(number, "")
}

fun Context.insertNewSMSCursor(
    address: String,
    subject: String,
    body: String,
    date: Long,
    read: Int,
    threadId: Long,
    type: Int,
    subscriptionId: Int
): Long {
    val uri = Telephony.Sms.CONTENT_URI
    val contentValues = ContentValues().apply {
        put(Telephony.Sms.ADDRESS, address)
        put(Telephony.Sms.SUBJECT, subject)
        put(Telephony.Sms.BODY, body)
        put(Telephony.Sms.DATE, date)
        put(Telephony.Sms.READ, read)
        put(Telephony.Sms.THREAD_ID, threadId)
        put(Telephony.Sms.TYPE, type)
        put(Telephony.Sms.SUBSCRIPTION_ID, subscriptionId)
    }

    return try {
        val newUri = contentResolver.insert(uri, contentValues)
        newUri?.lastPathSegment?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun Context.getConversations(threadId: Long? = null): ArrayList<Conversation> {
    val blockedThreadIds = getBlockedThreadIds()

    val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
    val projection = mutableListOf(
        Telephony.Threads._ID,
        Telephony.Threads.SNIPPET,
        Telephony.Threads.DATE,
        Telephony.Threads.READ,
        Telephony.Threads.RECIPIENT_IDS,
    )

    var selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"
    var selectionArgs = arrayOf("0")
    if (threadId != null) {
        selection += " AND ${Telephony.Threads._ID} = ?"
        selectionArgs = arrayOf("0", threadId.toString())
    }

    val sortOrder = "${Telephony.Threads.DATE} DESC"

    val conversations = ArrayList<Conversation>()
    try {
        queryCursorUnsafe(
            uri,
            projection.toTypedArray(),
            selection,
            selectionArgs,
            sortOrder
        ) { cursor ->
            val id = cursor.getLongValue(Telephony.Threads._ID)
            val snippet = cursor.getStringValue(Telephony.Threads.SNIPPET) ?: ""


            var date = cursor.getLongValue(Telephony.Threads.DATE)
            if (date.toString().length > 10) {
                date /= 1000
            }

            val rawIds = cursor.getStringValue(Telephony.Threads.RECIPIENT_IDS)
            val recipientIds =
                rawIds.split(" ").filter { it.areDigitsOnly() }.map { it.toInt() }.toMutableList()
            val phoneNumbers = getThreadPhoneNumbers(recipientIds)

            val names = getThreadContactNames(phoneNumbers)
            val title = TextUtils.join(", ", names.toTypedArray())
            val photoUri =
                if (phoneNumbers.size == 1) getNameAndPhotoFromPhoneNumber(phoneNumbers.first()).second else ""

            val isGroupConversation = phoneNumbers.size > 1
            val read = cursor.getIntValue(Telephony.Threads.READ) == 1
            val lastSnippet = getMessages(id, limit = 1)
            var msgId = -1L
            msgId = if (lastSnippet.isNotEmpty()) {
                lastSnippet[0].id
            } else {
                -1
            }
            val isBlocked = if (blockedThreadIds.contains(id)) {
                1
            } else {
                0
            }
            val conversation = Conversation(
                id,
                snippet,
                date.toInt(),
                read,
                title,
                photoUri,
                isGroupConversation,
                phoneNumbers.first(),
                categorizeMessage(snippet),
                msgId = msgId,
                0,
                0,
                isBlocked,
                false
            )
            conversations.add(conversation)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    conversations.sortByDescending { it.date }
    return conversations
}

fun Context.getConversationsLimitFunction(
    limit: Int,
    offset: Int,
    threadId: Long? = null
): ArrayList<Conversation> {
    val blockedThreadIds = getBlockedThreadIds()

    val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
    val projection = mutableListOf(
        Telephony.Threads._ID,
        Telephony.Threads.SNIPPET,
        Telephony.Threads.DATE,
        Telephony.Threads.READ,
        Telephony.Threads.RECIPIENT_IDS,
    )

    var selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"
    var selectionArgs = arrayOf("0")
    if (threadId != null) {
        selection += " AND ${Telephony.Threads._ID} = ?"
        selectionArgs = arrayOf("0", threadId.toString())
    }

    val sortOrder = "${Telephony.Threads.DATE} DESC LIMIT $limit OFFSET $offset"

    val conversations = ArrayList<Conversation>()
    try {
        queryCursorUnsafe(
            uri,
            projection.toTypedArray(),
            selection,
            selectionArgs,
            sortOrder
        ) { cursor ->
            val id = cursor.getLongValue(Telephony.Threads._ID)
            val snippet = cursor.getStringValue(Telephony.Threads.SNIPPET) ?: ""

            var date = cursor.getLongValue(Telephony.Threads.DATE)
            if (date.toString().length > 10) {
                date /= 1000
            }

            val rawIds = cursor.getStringValue(Telephony.Threads.RECIPIENT_IDS)
            val recipientIds =
                rawIds.split(" ").filter { it.areDigitsOnly() }.map { it.toInt() }.toMutableList()
            val phoneNumbers = getThreadPhoneNumbers(recipientIds)

            val names = getThreadContactNames(phoneNumbers)
            val title = TextUtils.join(", ", names.toTypedArray())
            val photoUri =
                if (phoneNumbers.size == 1) getNameAndPhotoFromPhoneNumber(phoneNumbers.first()).second else ""

            val isGroupConversation = phoneNumbers.size > 1
            val read = cursor.getIntValue(Telephony.Threads.READ) == 1
            val lastSnippet = getMessages(id, limit = 1)
            var msgId = -1L
            msgId = if (lastSnippet.isNotEmpty()) {
                lastSnippet[0].id
            } else {
                -1
            }
            val isBlocked = if (blockedThreadIds.contains(id)) {
                1
            } else {
                0
            }
            val conversation = Conversation(
                id,
                snippet,
                date.toInt(),
                read,
                title,
                photoUri,
                isGroupConversation,
                phoneNumbers.first(),
                categorizeMessage(snippet),
                msgId = msgId,
                0,
                0,
                isBlocked,
                false
            )
            conversations.add(conversation)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    conversations.sortByDescending { it.date }
    return conversations
}

fun Context.updateLastConversationMessage(threadId: Long) {
    val uri = Telephony.Threads.CONTENT_URI
    val selection = "${Telephony.Threads._ID} = ?"
    val selectionArgs = arrayOf(threadId.toString())
    try {
        contentResolver.delete(uri, selection, selectionArgs)
        val newConversation = getConversations(threadId)[0]
    } catch (e: Exception) {
        e.message
    }
}

fun Context.getMessages(
    threadId: Long,
    dateFrom: Int = -1,
    limit: Int = MESSAGES_LIMIT
): ArrayList<Message> {
    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms._ID,
        Telephony.Sms.BODY,
        Telephony.Sms.TYPE,
        Telephony.Sms.ADDRESS,
        Telephony.Sms.DATE,
        Telephony.Sms.READ,
        Telephony.Sms.THREAD_ID,
        Telephony.Sms.SUBSCRIPTION_ID,
        Telephony.Sms.STATUS
    )

    val rangeQuery =
        if (dateFrom == -1) "" else "AND ${Telephony.Sms.DATE} < ${dateFrom.toLong() * 1000}"
    val selection = "${Telephony.Sms.THREAD_ID} = ? $rangeQuery"
    val selectionArgs = arrayOf(threadId.toString())
    val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

    var messages = ArrayList<Message>()
    queryCursor(uri, projection, selection, selectionArgs, sortOrder) { cursor ->
        val senderNumber = cursor.getStringValue(Telephony.Sms.ADDRESS) ?: return@queryCursor
        val id = cursor.getLongValue(Telephony.Sms._ID)
        val body = cursor.getStringValue(Telephony.Sms.BODY)
        val type = cursor.getIntValue(Telephony.Sms.TYPE)
        val namePhoto = getNameAndPhotoFromPhoneNumber(senderNumber)
        val senderName = namePhoto.first
        val photoUri = namePhoto.second
        val date = (cursor.getLongValue(Telephony.Sms.DATE) / 1000).toInt()
        val read = cursor.getIntValue(Telephony.Sms.READ) == 1
        val thread = cursor.getLongValue(Telephony.Sms.THREAD_ID)
        val subscriptionId = cursor.getIntValue(Telephony.Sms.SUBSCRIPTION_ID)
        val status = cursor.getIntValue(Telephony.Sms.STATUS)
        val participants = senderNumber.split(ADDRESS_SEPARATOR).map { number ->
            val phoneNumber = PhoneNumber(number, 0, "", number)
            val participantPhoto = getNameAndPhotoFromPhoneNumber(number)
            SimpleContact(
                0,
                0,
                participantPhoto.first,
                photoUri,
                arrayListOf(phoneNumber),
                ArrayList(),
                ArrayList(),
                threadId
            )
        }
        val isMMS = false
        val message =
            Message(
                id,
                body,
                type,
                status,
                ArrayList(participants),
                date,
                read,
                thread,
                isMMS,
                senderNumber,
                senderName,
                photoUri,
                subscriptionId,
                categorizeMessage(body)
            )
        messages.add(message)
    }

    messages = messages
        .sortedWith(compareBy<Message> { it.date }.thenBy { it.id })
        .takeLast(limit)
        .toMutableList() as ArrayList<Message>

    return messages
}


fun Context.getMessagesForConversation(
    threadId: Long,
    dateFrom: Int = -1,
    limit: Int = MESSAGES_LIMIT,
    offset: Int = 0,
    mRepository: DataRepository
): ArrayList<Message> {
    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms._ID,
        Telephony.Sms.BODY,
        Telephony.Sms.TYPE,
        Telephony.Sms.ADDRESS,
        Telephony.Sms.DATE,
        Telephony.Sms.READ,
        Telephony.Sms.THREAD_ID,
        Telephony.Sms.SUBSCRIPTION_ID,
        Telephony.Sms.STATUS
    )

    val rangeQuery =
        if (dateFrom == -1) "" else "AND ${Telephony.Sms.DATE} < ${dateFrom.toLong() * 1000}"
    val selection = "${Telephony.Sms.THREAD_ID} = ? $rangeQuery"
    val selectionArgs = arrayOf(threadId.toString())
    val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit OFFSET $offset"

    val messages = ArrayList<Message>()
    queryCursor(uri, projection, selection, selectionArgs, sortOrder) { cursor ->
        val senderNumber = cursor.getStringValue(Telephony.Sms.ADDRESS) ?: return@queryCursor
        val id = cursor.getLongValue(Telephony.Sms._ID)
        val body = cursor.getStringValue(Telephony.Sms.BODY)
        val type = cursor.getIntValue(Telephony.Sms.TYPE)
        val namePhoto = getNameAndPhotoFromPhoneNumber(senderNumber)
        val senderName = namePhoto.first
        val photoUri = namePhoto.second
        val date = (cursor.getLongValue(Telephony.Sms.DATE) / 1000).toInt()
        val read = cursor.getIntValue(Telephony.Sms.READ) == 1
        val thread = cursor.getLongValue(Telephony.Sms.THREAD_ID)
        val subscriptionId = cursor.getIntValue(Telephony.Sms.SUBSCRIPTION_ID)
        val status = cursor.getIntValue(Telephony.Sms.STATUS)
        val participants = senderNumber.split(ADDRESS_SEPARATOR).map { number ->
            val phoneNumber = PhoneNumber(number, 0, "", number)
            val participantPhoto = getNameAndPhotoFromPhoneNumber(number)
            SimpleContact(
                0,
                0,
                participantPhoto.first,
                photoUri,
                arrayListOf(phoneNumber),
                ArrayList(),
                ArrayList(),
                threadId
            )
        }
        val isMMS = false
        val message =
            Message(
                id,
                body,
                type,
                status,
                ArrayList(participants),
                date,
                read,
                thread,
                isMMS,
                senderNumber,
                senderName,
                photoUri,
                subscriptionId,
                categorizeMessage(body)
            )

//        mRepository.insertMessages(messages)
        messages.add(message)
    }

    return messages
}


fun categorizeMessage(messageBody: String): MessageType {
    val lowercaseMessage = messageBody.lowercase(Locale.getDefault())
    when {
        Regex("\\b\\d{6}\\b").find(lowercaseMessage) != null -> return MessageType.OTP
        Regex(
            "\\b(OTP|verification code|code)\\b",
            RegexOption.IGNORE_CASE
        ).find(lowercaseMessage) != null -> return MessageType.OTP

        Regex(
            "\\b(offer|discount|promo|sale|deal|શ્રેષ્ઠ ઓફર|surprise|coupon|deactivate|opt-out|wa\\\\.me|create|make|set|caller tunes|કોલરટ્યુન્સ|vi\\\\.app\\\\.link|charge|Rs|validity|एटीएम|पिन|wow|mobile pack|cashback|UL|upto)\\b",
            RegexOption.IGNORE_CASE
        ).find(lowercaseMessage) != null -> return MessageType.OFFER

        Regex("\\b(transaction|payment|invoice|credited|debited)\\b", RegexOption.IGNORE_CASE).find(
            lowercaseMessage
        ) != null -> return MessageType.TRANSACTION
    }
    return MessageType.OTHER
}

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.getThreadParticipants(
    threadId: Long,
    contactsMap: HashMap<Int, SimpleContact>?
): ArrayList<SimpleContact> {
    val uri = Uri.parse("${Telephony.MmsSms.CONTENT_CONVERSATIONS_URI}?simple=true")
    val projection = arrayOf(
        Telephony.ThreadsColumns.RECIPIENT_IDS
    )
    val selection = "${Telephony.Mms._ID} = ?"
    val selectionArgs = arrayOf(threadId.toString())
    val participants = ArrayList<SimpleContact>()
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val address = cursor.getStringValue(Telephony.ThreadsColumns.RECIPIENT_IDS)
                address.split(" ").filter { it.areDigitsOnly() }.forEach {
                    val addressId = it.toInt()
                    if (contactsMap?.containsKey(addressId) == true) {
                        participants.add(contactsMap[addressId]!!)
                        return@forEach
                    }

                    val number = getPhoneNumberFromAddressId(addressId)
                    val namePhoto = getNameAndPhotoFromPhoneNumber(number)
                    val name = namePhoto.first
                    val photoUri = namePhoto.second
                    val phoneNumber = PhoneNumber(number, 0, "", number)
                    val contact = SimpleContact(
                        addressId,
                        addressId,
                        name,
                        photoUri,
                        arrayListOf(phoneNumber),
                        ArrayList(),
                        ArrayList()
                    )
                    participants.add(contact)
                }
            }
        }
    } catch (e: Exception) {
        showToast(e.message ?: "")
    }
    return participants
}


fun Context.markMessageRead(id: Long, isMMS: Boolean) {
    val uri = if (isMMS) Telephony.Mms.CONTENT_URI else Telephony.Sms.CONTENT_URI
    val contentValues = ContentValues().apply {
        put(Telephony.Sms.READ, 1)
        put(Telephony.Sms.SEEN, 1)
    }
    val selection = "${Telephony.Sms._ID} = ?"
    val selectionArgs = arrayOf(id.toString())
    contentResolver.update(uri, contentValues, selection, selectionArgs)
}

fun Context.deleteMessage(id: Long, isMMS: Boolean) {
    val uri = if (isMMS) Telephony.Mms.CONTENT_URI else Telephony.Sms.CONTENT_URI
    val selection = "${Telephony.Sms._ID} = ?"
    val selectionArgs = arrayOf(id.toString())
    try {
        contentResolver.delete(uri, selection, selectionArgs)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.deleteConversation(threadId: Long) {
//    var uri = Telephony.Sms.CONTENT_URI
//    val selection = "${Telephony.Sms.THREAD_ID} = ?"
//    val selectionArgs = arrayOf(threadId.toString())
//    try {
//        contentResolver.delete(uri, selection, selectionArgs)
//    } catch (e: Exception) {
//        showToast(e.message!!)
//    }
//
//    uri = Telephony.Mms.CONTENT_URI
//    try {
//        contentResolver.delete(uri, selection, selectionArgs)
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }

    try {
        val uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId)
        contentResolver.delete(uri, null, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.updateConversationArchivedStatus(threadId: Long, archived: Boolean) {
    val uri = Telephony.Threads.CONTENT_URI
    val values = ContentValues().apply {
        put(Telephony.Threads.ARCHIVED, archived)
    }
    val selection = "${Telephony.Threads._ID} = ?"
    val selectionArgs = arrayOf(threadId.toString())
    try {
        contentResolver.update(uri, values, selection, selectionArgs)
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    }
}

fun Context.markThreadMessagesRead(threadId: Long) {
//    arrayOf(Telephony.Sms.CONTENT_URI, Telephony.Mms.CONTENT_URI).forEach { uri ->
//        val contentValues = ContentValues().apply {
//            put(Telephony.Sms.READ, 1)
//            put(Telephony.Sms.SEEN, 1)
//        }
//        val selection = "${Telephony.Sms.THREAD_ID} = ?"
//        val selectionArgs = arrayOf(threadId.toString())
//        contentResolver.update(uri, contentValues, selection, selectionArgs)
//    }

    try {
        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, true)
        values.put(Telephony.Sms.READ, true)
        val uri = ContentUris.withAppendedId(
            Telephony.MmsSms.CONTENT_CONVERSATIONS_URI,
            threadId
        )
        contentResolver.update(uri, values, "${Telephony.Sms.READ} = 0", null)
    } catch (exception: Exception) {
        Log.e("TAG", "markAsRead: ${exception.message}")
    }
}

fun Context.markThreadMessagesUnread(threadId: Long) {
//    arrayOf(Telephony.Sms.CONTENT_URI, Telephony.Mms.CONTENT_URI).forEach { uri ->
//        val contentValues = ContentValues().apply {
//            put(Telephony.Sms.READ, 0)
//            put(Telephony.Sms.SEEN, 0)
//        }
//        val selection = "${Telephony.Sms.THREAD_ID} = ?"
//        val selectionArgs = arrayOf(threadId.toString())
//        contentResolver.update(uri, contentValues, selection, selectionArgs)
//    }
    try {
        val values = ContentValues()
        values.put(Telephony.Sms.SEEN, false)
        values.put(Telephony.Sms.READ, false)
        val uri = ContentUris.withAppendedId(
            Telephony.MmsSms.CONTENT_CONVERSATIONS_URI,
            threadId
        )
        contentResolver.update(uri, values, "${Telephony.Sms.READ} = 1", null)
    } catch (exception: Exception) {
        Log.e("TAG", "markAsRead: ${exception.message}")
    }
}

fun Context.getBlockedNumbers(): ArrayList<BlockedNumber> {
    val blockedNumbers = ArrayList<BlockedNumber>()
    val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
    val projection = arrayOf(
        BlockedNumberContract.BlockedNumbers.COLUMN_ID,
        BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
        BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
    )

    queryCursor(uri, projection) { cursor ->
        val id = cursor.getLongValue(BlockedNumberContract.BlockedNumbers.COLUMN_ID)
        val number =
            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
        val normalizedNumber =
            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER) ?: number
        val comparableNumber = normalizedNumber.trimToComparableNumber()
        val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber)
        blockedNumbers.add(blockedNumber)
    }

    return blockedNumbers
}

fun Context.getBlockedThreadIds(): List<Long> {
    return getBlockedNumbers().map { blockedNumber ->
        getThreadId(setOf(blockedNumber.number))
    }
}

fun Context.addBlockedNumber(number: String): Boolean {
    ContentValues().apply {
        put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        if (number.isPhoneNumber()) {
            put(
                BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER,
                PhoneNumberUtils.normalizeNumber(number)
            )
        }
        try {
            contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, this)
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                CommonClass.blockedThreadIds = getBlockedThreadIds()
            }
        } catch (e: Exception) {
            return false
        }
    }
    return true
}

fun Context.deleteBlockedNumber(number: String): Boolean {
    val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
    val selectionArgs = arrayOf(number)
    val deletedRowCount = contentResolver.delete(
        BlockedNumberContract.BlockedNumbers.CONTENT_URI,
        selection,
        selectionArgs
    )

    CoroutineScope(Dispatchers.IO).launch {
        delay(500)
        CommonClass.blockedThreadIds = getBlockedThreadIds()
    }
    return deletedRowCount > 0
}

fun Context.countRowsThreads(uri: Uri): Int {
    if (hasPermission(Manifest.permission.READ_SMS)) {
        val selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"
        val selectionArgs = arrayOf("0")
        val sortOrder = "${Telephony.Threads.DATE} DESC"

        val cursor = contentResolver.query(
            uri, null, selection, selectionArgs, sortOrder
        ) ?: return 0

        cursor.use {
            return cursor.count
        }
    } else {
        return 0
    }
}

fun Context.countRows(uri: Uri): Int {

    val cursor = contentResolver.query(
        uri, null, null, null, null
    ) ?: return 0

    cursor.use {
        return cursor.count
    }
}

fun Context.countMessagesInThread(threadId: Long): Int {
    val uri = Uri.parse("content://sms/")
    val selection = "thread_id = ?"
    val selectionArgs = arrayOf(threadId.toString())

    val cursor = contentResolver.query(
        uri, null, selection, selectionArgs, null
    ) ?: return 0

    cursor.use {
        return cursor.count
    }
}






