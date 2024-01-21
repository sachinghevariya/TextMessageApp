package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Telephony
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.fetchData.queryCursor

fun Context.showToast(message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(this@showToast, message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.copied_label), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = getString(R.string.value_copied_to_clipboard_show)
    showToast(toastText)
}

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun Context.getTimeFormat() = if (false) CommonClass.TIME_FORMAT_24 else CommonClass.TIME_FORMAT_12

fun Context.getNotificationBitmap(photoUri: String): Bitmap? {
    val size = resources.getDimension(com.intuit.sdp.R.dimen._55sdp).toInt()
    if (photoUri.isEmpty()) {
        return null
    }

    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .centerCrop()

    return try {
        Glide.with(this)
            .asBitmap()
            .load(photoUri)
            .apply(options)
            .apply(RequestOptions.circleCropTransform())
            .into(size, size)
            .get()
    } catch (e: Exception) {
        null
    }
}

fun Context.removeDiacriticsIfNeeded(text: String): String {
    return text.normalizeString()
}

fun Context.hasPermission(permId: String) =
    ContextCompat.checkSelfPermission(this, permId) == PackageManager.PERMISSION_GRANTED


fun Context.getConversationIds(): List<Long> {
    val uri = Uri.parse("${Telephony.Threads.CONTENT_URI}?simple=true")
    val projection = arrayOf(Telephony.Threads._ID)
    val selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"
    val selectionArgs = arrayOf("0")
    val sortOrder = "${Telephony.Threads.DATE} ASC"
    val conversationIds = mutableListOf<Long>()
    queryCursor(uri, projection, selection, selectionArgs, sortOrder) { cursor ->
        val id = cursor.getLongValue(Telephony.Threads._ID)
        conversationIds.add(id)
    }
    return conversationIds
}

fun Context.isUsingSystemDarkTheme() = resources.configuration.uiMode