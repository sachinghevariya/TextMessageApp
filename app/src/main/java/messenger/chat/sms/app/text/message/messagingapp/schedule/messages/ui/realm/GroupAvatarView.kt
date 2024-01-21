package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.setBackgroundTint

class GroupAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var avatar1: AvatarView? = null
    private var avatar1Frame: FrameLayout? = null

    var title: Pair<String?, String?> = Pair("", "")
        set(value) {
            field = value
            updateView()
        }

    init {
        View.inflate(context, R.layout.group_avatar_view, this)
        avatar1 = findViewById(R.id.avatar1)
        avatar1Frame = findViewById(R.id.avatar1Frame)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        avatar1Frame?.setBackgroundTint(0xCCF5F5F5.toInt())
        title.run(avatar1!!::setRecipientPair)
    }

}
