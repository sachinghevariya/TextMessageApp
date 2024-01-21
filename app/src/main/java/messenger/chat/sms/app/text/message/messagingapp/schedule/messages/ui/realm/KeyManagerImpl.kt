package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import io.realm.Realm

class KeyManagerImpl : KeyManager {

    private var initialized = false
    private var maxValue: Long = 0

    override fun reset() {
        initialized = true
        maxValue = 0L
    }


    companion object {
        var instance: KeyManagerImpl? = null
        fun newInstance(): KeyManagerImpl {
            return if (instance == null) {
                instance = KeyManagerImpl()
                instance!!
            } else {
                instance!!
            }
        }
    }

    override fun newId(): Long {
        synchronized(this) {
            if (!initialized) {
                maxValue = Realm.getDefaultInstance().use { realm ->
                    realm.where(MessageNew::class.java).max("id")?.toLong() ?: 0L
                }
                initialized = true
            }

            maxValue++
            return maxValue
        }
    }

}