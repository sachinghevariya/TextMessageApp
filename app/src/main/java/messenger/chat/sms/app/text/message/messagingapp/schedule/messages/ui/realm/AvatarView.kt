package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.realm

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.MyPreferences
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.getContactLetterIcon
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils.gone

class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

//    @Inject lateinit var colors: Colors

    private var lookupKey: String? = null
    private var fullName: String? = null
    private var photoUri: String? = null
    private var title: String? = null
    private var lastUpdated: Long? = null
    private var initial: TextView? = null
    private var icon: ImageView? = null
    private var photo: ImageView? = null
    private var myPreferences: MyPreferences? = null

    init {
        myPreferences = MyPreferences(context)

        View.inflate(context, R.layout.avatar_view, this)
        initial = findViewById(R.id.initial)
        icon = findViewById(R.id.icon)
        photo = findViewById(R.id.photo)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * Use the [contact] information to display the avatar.
     */
    fun setRecipient(recipient: Recipient?) {
        lookupKey = recipient?.contact?.lookupKey
        fullName = recipient?.contact?.name
        photoUri = recipient?.contact?.photoUri
        lastUpdated = recipient?.contact?.lastUpdate
//        theme = colors.theme(recipient)
        updateView()
    }

    fun setRecipientPair(recipient: Pair<String?, String?>) {
        fullName = recipient.first
        photoUri = recipient.second
        updateView()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        if (myPreferences?.showProfilePhoto!! && photoUri != null && photoUri?.isNotEmpty()!!) {
            Glide.with(context)
                .load(photoUri)
                .into(photo!!)
        } else {
            fullName?.let {
                photo?.setImageBitmap(getContactLetterIcon(context, it))
            }
        }
        icon?.visibility = GONE
        initial?.gone()

    }
}
