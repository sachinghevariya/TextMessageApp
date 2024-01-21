package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.util.Log

/**
 * Utility class to make ContentResolver queries with Kotlin more concise
 */
object SqliteWrapper {

    fun query(
        context: Context,
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        logError: Boolean = true
    ): Cursor? {
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        } catch (e: SQLiteException) {
            if (logError) {
                Log.e("TAG", "${e.message}")
            }
            null
        }

    }

}