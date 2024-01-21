package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.realm.Realm

fun <T> tryOrNull(logOnError: Boolean = true, body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Log.e("TAG", "tryOrNull: ${e.message}")
        }
        null
    }
}

fun getRealmThread(callback: (realm: Realm) -> Unit) {
    val handler = Handler(Looper.getMainLooper())

    Thread {
        handler.post {
            // Access Realm on the main thread
            val realm = Realm.getDefaultInstance()
            realm.use { realm ->
                callback.invoke(realm)
            }
        }
    }.start()

}