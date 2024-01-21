package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import android.provider.Telephony
import com.google.gson.Gson
import com.klinker.android.send_message.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.BackupDb
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model.SmsBackup
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.repository.DataRepository
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.generateRandomInt
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getConversationIds
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getDeviceId
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getIntValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getLongValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValue
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getStringValueOrNull

class MessagesImportExport(private val context: Context, val mRepository: DataRepository) {

    private var messagesImported = 0
    private var messagesFailed = 0

    fun getMessagesToExport(
        callbackProgress: (Int, Int) -> Unit,
        callback: (backupDb: BackupDb) -> Unit
    ) {
        val conversationIds = context.getConversationIds()
        val msgList = getSmsMessages(conversationIds, callbackProgress)
        callback(
            BackupDb(
                msgList.size, getDeviceId(context) ?: generateRandomInt().toString(), msgList
            )
        )
    }

    private fun getSmsMessages(
        threadIds: List<Long>,
        callbackProgress: (Int, Int) -> Unit
    ): List<SmsBackup> {
        var counter = 0
        val total = context.countRows(Telephony.Sms.CONTENT_URI)
        val projection = arrayOf(
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.LOCKED,
            Telephony.Sms.PROTOCOL,
            Telephony.Sms.READ,
            Telephony.Sms.STATUS,
            Telephony.Sms.TYPE,
            Telephony.Sms.SERVICE_CENTER
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val smsList = mutableListOf<SmsBackup>()

        threadIds.map { it.toString() }.forEach { threadId ->
            context.queryCursor(
                Telephony.Sms.CONTENT_URI, projection, selection, arrayOf(threadId)
            ) { cursor ->
                val subscriptionId = cursor.getLongValue(Telephony.Sms.SUBSCRIPTION_ID)
                val address = cursor.getStringValue(Telephony.Sms.ADDRESS)
                val body = cursor.getStringValueOrNull(Telephony.Sms.BODY)
                val date = cursor.getLongValue(Telephony.Sms.DATE)
                val dateSent = cursor.getLongValue(Telephony.Sms.DATE_SENT)
                val locked = cursor.getIntValue(Telephony.Sms.DATE_SENT)
                val protocol = cursor.getStringValueOrNull(Telephony.Sms.PROTOCOL)
                val read = cursor.getIntValue(Telephony.Sms.READ)
                val status = cursor.getIntValue(Telephony.Sms.STATUS)
                val type = cursor.getIntValue(Telephony.Sms.TYPE)
                val serviceCenter = cursor.getStringValueOrNull(Telephony.Sms.SERVICE_CENTER)
                smsList.add(
                    SmsBackup(
                        subscriptionId,
                        address,
                        body,
                        date,
                        dateSent,
                        locked,
                        protocol,
                        read,
                        status,
                        type,
                        serviceCenter
                    )
                )
                callbackProgress(counter, total)
                counter++
            }
        }
        return smsList
    }

    fun restoreMessages(messagesBackup: List<SmsBackup>, callback: (Int) -> Unit) {
        var counter = 0
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val threadIdList = arrayListOf<Long>()

                messagesBackup.forEach { message ->
                    try {
                        MessagesImportExport(mRepository.mContext, mRepository).writeSmsMessage(
                            message,
                            threadIdList
                        ) {
                            val jsonString = Gson().toJson(threadIdList.distinct())
                            MyPreferences.getPreferences(context)?.needToUpdateThreadDateList =
                                jsonString
                        }
                        counter++
                        messagesImported++
                    } catch (e: Exception) {
                        counter++
                        messagesFailed++
                    }
                    callback.invoke(counter)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

//            callback.invoke(
//                when {
//                    messagesImported == 0 && messagesFailed == 0 -> ImportResult.IMPORT_NOTHING_NEW
//                    messagesFailed > 0 && messagesImported > 0 -> ImportResult.IMPORT_PARTIAL
//                    messagesFailed > 0 -> ImportResult.IMPORT_FAIL
//                    else -> ImportResult.IMPORT_OK
//                }
//            )
        }
    }

    private fun writeSmsMessage(
        smsBackup: SmsBackup,
        threadIdList: ArrayList<Long>,
        callback: (ArrayList<Long>) -> Unit
    ) {
        val contentValues = smsBackup.toContentValues()
        val threadId = Utils.getOrCreateThreadId(context, smsBackup.address)
        contentValues.put(Telephony.Sms.THREAD_ID, threadId)
        if (!smsExist(smsBackup)) {
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, contentValues)
            threadIdList.add(threadId)
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(500)
//                context.updateConversationTimeStamp(threadId, smsBackup.date)
//            }
        }
        callback.invoke(threadIdList)
    }


    /*
        private fun Context.updateConversationTimeStamp(threadId: Long, date: Long) {
            val threadUri = Uri.parse("${Telephony.Threads.CONTENT_URI}/$threadId")
            val uri = Telephony.Threads.CONTENT_URI
            val projection = arrayOf(Telephony.Threads._ID)

            val selection = "${Telephony.Threads._ID} = ?"
            val selectionArgs = arrayOf(threadId.toString())
            // Check if the thread ID exists
            val cursor = contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    Log.e("TAG", "updateConversationTimeStamp: moveTOFirst()")
                    // Thread ID exists, update the timestamp
                    val values = ContentValues().apply {
                        put(Telephony.Threads.DATE, date)
                    }

                    val selection = "${Telephony.Threads._ID} = ?"
                    val selectionArgs = arrayOf(threadId.toString())

                    try {
                        contentResolver.update(threadUri, values, selection, selectionArgs)
                    } catch (sqliteException: SQLiteException) {
                        Log.e("TAG", "updateConversationTimeStamp: ${sqliteException.message}")
                        sqliteException.printStackTrace()
                    }
                } else {
                    // Thread ID doesn't exist, handle it accordingly
                    // You may choose to insert a new record or handle it in another way
                    Log.e("TAG", "Thread ID $threadId not found.")
                }
            }
        }
    */


    private fun Context.updateConversationTimeStamp(threadId: Long, date: Long) {
        val uri = Telephony.Threads.CONTENT_URI
        val values = ContentValues().apply {
            put(Telephony.Threads.DATE, date)
        }
        val selection = "${Telephony.Threads._ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())
        try {
            contentResolver.update(uri, values, selection, selectionArgs)
        } catch (sqliteException: SQLiteException) {
            sqliteException.printStackTrace()
        }
    }

    /*private fun Context.updateConversationTimeStamp(threadId: Long, date: Long) {
        if (date <= 0) {
            Log.e("TAG", "Invalid date value: $date")
            return
        }

        val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}/$threadId")
        val values = ContentValues().apply {
            put(Telephony.Threads.DATE, date)
        }

        try {
            val rowsUpdated = contentResolver.update(uri, values, null, null)
            Log.e("TAG", "updateConversationTimeStamp: Updated $rowsUpdated rows")
        } catch (sqliteException: SQLiteException) {
            Log.e("TAG", "updateConversationTimeStamp: ${sqliteException.message}")
            sqliteException.printStackTrace()
        }
    }*/

    private fun smsExist(smsBackup: SmsBackup): Boolean {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms._ID)
        val selection =
            "${Telephony.Sms.DATE} = ? AND ${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.TYPE} = ?"
        val selectionArgs =
            arrayOf(smsBackup.date.toString(), smsBackup.address, smsBackup.type.toString())
        var exists = false
        context.queryCursor(uri, projection, selection, selectionArgs) {
            exists = it.count > 0
        }
        return exists
    }

}
