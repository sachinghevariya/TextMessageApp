package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ScheduledMessageNew(
    @PrimaryKey var id: Long = 0,
    var threadId: Long = 0,
    var date: Long = 0,
    var subId: Int = -1,
    var recipients: RealmList<String> = RealmList(),
    var sendAsGroup: Boolean = true,
    var body: String = "",
    var attachments: RealmList<String> = RealmList()
) : RealmObject() {

    fun copy(
        id: Long = this.id,
        threadId: Long = this.threadId,
        date: Long = this.date,
        subId: Int = this.subId,
        recipients: RealmList<String> = this.recipients,
        sendAsGroup: Boolean = this.sendAsGroup,
        body: String = this.body,
        attachments: RealmList<String> = this.attachments
    ): ScheduledMessageNew {

        return ScheduledMessageNew(
            id,
            threadId,
            date,
            subId,
            recipients,
            sendAsGroup,
            body,
            attachments
        )
    }

    fun getMessage(
        id: Long = this.id,
        threadId: Long = this.threadId,
        date: Long = this.date,
        subId: Int = this.subId,
        recipients: RealmList<String> = this.recipients,
        sendAsGroup: Boolean = this.sendAsGroup,
        body: String = this.body,
        attachments: RealmList<String> = this.attachments
    ): MessageNew {
        return MessageNew().apply {
            this.id = id
            this.threadId = threadId
            this.date = date
            this.subId = subId
            this.body = body
        }
    }
}