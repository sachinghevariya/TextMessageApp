package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import messenger.chat.sms.app.text.message.messagingapp.schedule.messages.R
import java.util.Locale

fun loadContactImage(
    context: Context,
    path: String,
    imageView: ImageView,
    placeholderName: String,
    placeholderImage: Drawable? = null,
    showProfilePhoto: Boolean = false
) {
    val placeholder = placeholderImage ?: BitmapDrawable(
        context.resources,
        getContactLetterIcon(context, placeholderName)
    )

    val options = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .error(placeholder)
        .centerCrop()

    if (showProfilePhoto) {
        Glide.with(context)
            .load(path)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(placeholder)
            .apply(options)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    } else {
        Glide.with(context)
            .load(placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(placeholder)
            .apply(options)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }

}

private val letterBitmapCache = LruCache<String, Bitmap>(10) // Cache size can be adjusted

fun getContactLetterIcon(context: Context, name: String): Bitmap {
    val cachedBitmap = letterBitmapCache[name]
    if (cachedBitmap != null) {
        return cachedBitmap
    }

    val letter = name.getNameLetter()
    val size = context.resources.getDimension(com.intuit.sdp.R.dimen._20sdp).toInt()

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val circlePaint = Paint().apply {
        color =
            letterBackgroundColors[Math.abs(name.hashCode()) % letterBackgroundColors.size].toInt()
        isAntiAlias = true
        isDither = true
    }

    val wantedTextSize = size / 2f
    val textPaint = Paint().apply {
        color = DARK_GREY
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = wantedTextSize
        style = Paint.Style.FILL
        typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
        isSubpixelText = false
        isLinearText = true
    }

    canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint)

    val xPos = canvas.width / 2f
    val yPos = canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(letter, xPos, yPos, textPaint)

    // Cache the bitmap for future use
    letterBitmapCache.put(name, bitmap)

    return bitmap
}

/*fun getContactLetterIcon(context: Context, name: String): Bitmap {
    val letter = name.getNameLetter()
    val size = context.resources.getDimension(com.intuit.sdp.R.dimen._20sdp).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val view = TextView(context)
    view.layout(0, 0, size, size)

    val circlePaint = Paint().apply {
        color =
            letterBackgroundColors[Math.abs(name.hashCode()) % letterBackgroundColors.size].toInt()
        isAntiAlias = true
        isDither = true
    }

    val wantedTextSize = size / 2f
    val textPaint = Paint().apply {
        color = DARK_GREY
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = wantedTextSize
        style = Paint.Style.FILL
        typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
        isSubpixelText = false
        isLinearText = true
    }

    canvas.drawCircle(size / 2f, size / 2f, size / 2f, circlePaint)

    val xPos = canvas.width / 2f
    val yPos = canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText(letter, xPos, yPos, textPaint)
    view.draw(canvas)
    return bitmap
}*/

private fun String.getNameLetter() =
    normalizeString().toCharArray().getOrNull(0)?.toString()?.uppercase(
        Locale.getDefault()
    ) ?: "A"

private val DARK_GREY = 0xFF9AA0A6.toInt()

private val letterBackgroundColors = arrayListOf(0xCCF5F5F5)