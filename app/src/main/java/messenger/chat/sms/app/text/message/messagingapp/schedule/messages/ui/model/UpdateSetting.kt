package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.model

import androidx.annotation.Keep

@Keep
class UpdateSetting

@Keep
class UpdateConversations

@Keep
class UpdateSwipeAction

@Keep
class RefreshMessages(val isScrollBottom: Boolean = false)

@Keep
class Callback(val int: Int)

@Keep
class CallbackS(val int: Int)

@Keep
class CallbackSchedule(val int: Int)

@Keep
class CallbackString(val number: String)

@Keep
class UpdateProgress(val count: Int, val total:Int, val status: String)

@Keep
class UpdateProgressStatus(val isShow: Boolean, val status: String)

@Keep
class UpdateContacts()
