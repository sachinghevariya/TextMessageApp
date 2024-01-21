package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.provider.Telephony
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Conversation
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.Message
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.PhoneNumber
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SimpleContact
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.CommonClass
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.areDigitsOnly
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getIntValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getLongValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValue

fun Context.updateConversationsByTimeInterval(
    limit: Int,
    offset: Int,
    threadId: Long? = null,
    timestamp: Long? = null,
    mRepository: DataRepository
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

    if (timestamp != null) {
        selection += " AND ${Telephony.Threads.DATE} > ?"
        selectionArgs = arrayOf("0", timestamp.toString())
    }

    if (threadId != null) {
        selection += " AND ${Telephony.Threads._ID} = ?"
        selectionArgs = selectionArgs.plus(threadId.toString())
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
            mRepository.insertOrUpdateConversation(conversation)
            conversations.add(conversation)

        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
//    conversations.sortByDescending { it.date }
    return conversations
}

fun Context.updateMessagesByTimeInterval(
    timestamp: Long = 0,
    threadId: Long = -1,
    mRepository: DataRepository
): List<Message> {
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

//    val selection = "${Telephony.Sms.DATE} > ?"
//    val selectionArgs = arrayOf(timestamp.toString())
//    val sortOrder = "${Telephony.Sms.DATE} DESC"
    val selection = "${Telephony.Sms.DATE} > ? AND ${Telephony.Sms.THREAD_ID} = ?"
    val selectionArgs = arrayOf(timestamp.toString(), threadId.toString())
    val sortOrder = "${Telephony.Sms.DATE} DESC"


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
        val participants = senderNumber.split(MessagingUtils.ADDRESS_SEPARATOR).map { number ->
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
                thread
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


        mRepository.insertMessages(messages)
        messages.add(message)
    }
    MyPreferences(mRepository.mContext).lastUpdateDbTime = System.currentTimeMillis() / 1000
    return messages
}

fun Context.getMessagesInsertDb(
    threadId: Long,
    dateFrom: Int = -1,
    limit: Int = CommonClass.MESSAGES_LIMIT,
    mRepository: DataRepository,
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
        val participants = senderNumber.split(MessagingUtils.ADDRESS_SEPARATOR).map { number ->
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
        mRepository.insertOrUpdateMessages(message)
        messages.add(message)
    }


//    messages = messages
//        .sortedWith(compareBy<Message> { it.date }.thenBy { it.id })
//        .takeLast(limit)
//        .toMutableList() as ArrayList<Message>

    return messages
}


fun Context.getConversationsInsertDb(
    threadId: Long? = null,
    mRepository: DataRepository,
    callBack: (Long) -> Unit
): ArrayList<Conversation> {
    val jsonString = MyPreferences.getPreferences(this)?.needToUpdateThreadDateList
    var needToUpdateThreadDateList: List<Long> = arrayListOf()
    needToUpdateThreadDateList = if (jsonString == null || jsonString == "") {
        arrayListOf<Long>()
    } else {
        val itemType = object : TypeToken<List<Long>>() {}.type
        Gson().fromJson(jsonString, itemType)
    }

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
            if (phoneNumbers.size > 0) {
                val nameAndPhoto = getNameAndPhotoFromPhoneNumber(phoneNumbers.first())
                //            val names = getThreadContactNames(phoneNumbers)
                val title = nameAndPhoto.first
//            val title = TextUtils.join(", ", names.toTypedArray())
                val photoUri =
                    if (phoneNumbers.size == 1) getNameAndPhotoFromPhoneNumber(phoneNumbers.first()).second else ""

                val isGroupConversation = phoneNumbers.size > 1
                val read = cursor.getIntValue(Telephony.Threads.READ) == 1
//                val lastSnippet = getMessages(id, limit = 1)
//                val lastSnippet = getMessagesForConversation(
//                    id,
//                    limit = 1,
//                    offset = 0,
//                    mRepository = mRepository
//                )
                val msgId = -1L
//                msgId = if (lastSnippet.isNotEmpty()) {
//                    lastSnippet[0].id
//                } else {
//                    -1
//                }

                if (needToUpdateThreadDateList.contains(id)) {
                    val lastSnippet = getMessages(id, limit = 1)
                    date = lastSnippet.first().date.toLong()
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
                mRepository.insertOrUpdateConversation(conversation)
                callBack.invoke(conversation.threadId)
                conversations.add(conversation)

            } else {
                callBack.invoke(-1)
            }

        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
//    conversations.sortByDescending { it.date }
    return conversations
}


fun Context.getConversationsNNNN(
    threadId: Long? = null,
    mRepository: DataRepository,
    callBack: (Conversation) -> Unit
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
            mRepository.insertOrUpdateConversation(conversation)
            conversations.add(conversation)
            callBack.invoke(conversation)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    conversations.sortByDescending { it.date }


    return conversations
}


fun Context.getThreadIdListFromCursor(): List<Long> {
    val threadIds = mutableListOf<Long>()

    val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
    val projection = mutableListOf(
        Telephony.Threads._ID
    )

    val selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"
    val selectionArgs = arrayOf("0")
    val sortOrder = "${Telephony.Threads.DATE} DESC"

    try {
        queryCursorUnsafe(
            uri,
            projection.toTypedArray(),
            selection,
            selectionArgs,
            sortOrder
        ) { cursor ->
            val id = cursor.getLongValue(Telephony.Threads._ID)
            threadIds.add(id)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return threadIds
}


fun Context.getThreadIdMessageCountMapFromCursor(): HashMap<Long, Int> {
    val threadIdMessageCountMap = HashMap<Long, Int>()

    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms.THREAD_ID,
        "COUNT(${Telephony.Sms._ID}) AS messageCount"
    )

    val selection = "${Telephony.Sms.THREAD_ID} IS NOT NULL) GROUP BY (${Telephony.Sms.THREAD_ID}"
    val sortOrder = "${Telephony.Sms.DATE} DESC"

    try {
        queryCursorUnsafe(
            uri,
            projection,
            selection,
            null,
            sortOrder
        ) { cursor ->
            val threadId = cursor.getLongValue(Telephony.Sms.THREAD_ID)
            val messageCount = cursor.getIntValue("messageCount")
            threadIdMessageCountMap[threadId] = messageCount
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return threadIdMessageCountMap
}


fun Context.getThreadIdMessageIdListMapFromCursor(): HashMap<Long, ArrayList<Long>> {
    val threadIdMessageIdListMap = HashMap<Long, ArrayList<Long>>()

    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms.THREAD_ID,
        Telephony.Sms._ID
    )

    val sortOrder = "${Telephony.Sms.DATE} DESC"

    try {
        queryCursorUnsafe(
            uri,
            projection,
            null,
            null,
            sortOrder
        ) { cursor ->
            val threadId = cursor.getLongValue(Telephony.Sms.THREAD_ID)
            val messageId = cursor.getLongValue(Telephony.Sms._ID)

            val messageIdList = threadIdMessageIdListMap.getOrPut(threadId) { arrayListOf() }
            messageIdList.add(messageId)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return threadIdMessageIdListMap
}


fun getThreadIdMessageCountMapFromLocalDb(mRepository: DataRepository): HashMap<Long, ArrayList<Long>> {
    val threadIdMessageIdListMap = HashMap<Long, ArrayList<Long>>()
    val messages = mRepository.mAppDb.getMessagesDao().getAllMessages()
    messages.forEach { message ->
        val threadId = message.threadId
        val messageId = message.id

        // Add messageId to the list associated with the threadId
        val messageIdList = threadIdMessageIdListMap.getOrPut(threadId) { arrayListOf() }
        messageIdList.add(messageId)
    }
    return threadIdMessageIdListMap
}


/*fun Context.getThreadIdMessageIdListMapFromCursor(): SparseArray<ArrayList<Long>> {
    val threadIdMessageIdListMap = SparseArray<ArrayList<Long>>()

    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms.THREAD_ID,
        Telephony.Sms._ID
    )

    val sortOrder = "${Telephony.Sms.DATE} DESC"

    try {
        queryCursorUnsafe(
            uri,
            projection,
            null,
            null,
            sortOrder
        ) { cursor ->
            val threadId = cursor.getLongValue(Telephony.Sms.THREAD_ID)
            val messageId = cursor.getLongValue(Telephony.Sms._ID)

            // Get or create the list associated with the threadId
            val messageIdList = threadIdMessageIdListMap.get(threadId.toInt()) ?: ArrayList()

            // Add the messageId to the list
            messageIdList.add(messageId)

            // Update the SparseArray
            threadIdMessageIdListMap.put(threadId.toInt(), messageIdList)
        }
    } catch (sqliteException: SQLiteException) {
        sqliteException.printStackTrace()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return threadIdMessageIdListMap
}

fun getThreadIdMessageCountMapFromLocalDb(mRepository: DataRepository): SparseArray<ArrayList<Long>> {
    val threadIdMessageIdListMap = SparseArray<ArrayList<Long>>()

    // Assuming you have a column named "threadId" in Message entity
    val messages = mRepository.mAppDb.getMessagesDao().getAllMessages()

    // Map messages to threadId and messageId
    messages.forEach { message ->
        val threadId = message.threadId
        val messageId = message.id

        // Get or create the list associated with the threadId
        val messageIdList = threadIdMessageIdListMap.get(threadId.toInt()) ?: ArrayList()

        // Add the messageId to the list
        messageIdList.add(messageId)

        // Update the SparseArray
        threadIdMessageIdListMap.put(threadId.toInt(), messageIdList)
    }
    return threadIdMessageIdListMap
}*/


fun Context.getMessageIdListFromCursor(): List<Long> {
    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
        Telephony.Sms._ID
    )

    val messages = ArrayList<Long>()
    queryCursor(uri, projection, null, null, null) { cursor ->
        val senderNumber = cursor.getStringValue(Telephony.Sms.ADDRESS) ?: return@queryCursor
        val id = cursor.getLongValue(Telephony.Sms._ID)
        messages.add(id)
    }
    return messages
}
