package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.database.Cursor

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}

fun <T> Cursor.map(map: (Cursor) -> T): List<T> {
    return List(count) { position ->
        moveToPosition(position)
        map(this)
    }
}


/**
 * Dumps the contents of the cursor as a CSV string
 */
fun Cursor.dump(): String {
    val lines = mutableListOf<String>()

    lines += columnNames.joinToString(",")
    forEach { lines += (0 until columnCount).joinToString(",", transform = ::getString) }

    return lines.joinToString("\n")
}
