package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

interface OnScrollListener {
    fun onScrolledUp()
    fun onScrolledDown()
    fun onReachedBottom()
    fun onReachedTop()
}

class MyScrollListener(private val onScrollListener: OnScrollListener) :
    RecyclerView.OnScrollListener() {

    private var scrollingUp = false
    private var scrollingDown = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // If dy is negative, the RecyclerView is scrolling up
        scrollingUp = dy < 0

        // If dy is positive, the RecyclerView is scrolling down
        scrollingDown = dy > 0

        if (scrollingUp) {
            onScrollListener.onScrolledUp()
        } else if (scrollingDown) {
            onScrollListener.onScrolledDown()
        }

        // Check if the user has reached the bottom
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        if (lastVisibleItemPosition == totalItemCount - 1) {
            onScrollListener.onReachedBottom()
        }

        // Check if the user has reached the top
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItemPosition == 0) {
            onScrollListener.onReachedTop()
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        // Check if the scroll state is idle
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollingUp) {
                onScrollListener.onScrolledUp()
            } else if (scrollingDown) {
                onScrollListener.onScrolledDown()
            }
        }
    }
}
