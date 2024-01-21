package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun View.fadeIn() {
    animate().alpha(1f).setDuration(150L).withStartAction { visible() }.start()
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}


fun EditText.showKeyboard() {
    requestFocus()
    val systemService =
        context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
    (systemService as InputMethodManager).showSoftInput(this, 1)
}

fun EditText.hideKeyboard() {
    requestFocus()
    val systemService =
        context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
    (systemService as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.onTextChangeListener(onTextChangedAction: (newText: String) -> Unit) =
    addTextChangedListener(object :
        TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            onTextChangedAction(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })


val EditText.value: String get() = text.toString().trim()

