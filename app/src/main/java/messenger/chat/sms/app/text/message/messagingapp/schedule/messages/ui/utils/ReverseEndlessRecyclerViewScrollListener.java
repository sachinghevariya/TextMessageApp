package messenger.chat.sms.app.text.message.messagingapp.schedule.messages.ui.utils;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public abstract class ReverseEndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have above your current scroll position
    // before loading more.
    private int visibleThreshold = 5;
    // The current offset index of data you have loaded
    private int currentPage = 0;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;
    // Sets the starting page index
    private final int startingPageIndex = 0;


    private boolean scrollingUp = false;
    private boolean scrollingDown = false;

    private final RecyclerView.LayoutManager mLayoutManager;

    private OnScrollListener onScrollListener;

    public ReverseEndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager, OnScrollListener onScrollListener) {
        this.mLayoutManager = layoutManager;
        this.onScrollListener = onScrollListener;
    }

    public ReverseEndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    public ReverseEndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    private int getFirstVisibleItem(int[] firstVisibleItemPositions) {
        int minSize = Integer.MAX_VALUE;
        for (int i = 0; i < firstVisibleItemPositions.length; i++) {
            if (i == 0) {
                minSize = firstVisibleItemPositions[i];
            } else if (firstVisibleItemPositions[i] < minSize) {
                minSize = firstVisibleItemPositions[i];
            }
        }
        return minSize;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int firstVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager)
                    .findFirstVisibleItemPositions(null);
            // get minimum element within the list
            firstVisibleItemPosition = getFirstVisibleItem(firstVisibleItemPositions);
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) mLayoutManager)
                    .findFirstVisibleItemPosition();
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && firstVisibleItemPosition <= visibleThreshold) {
            currentPage++;
            onLoadMore(currentPage, totalItemCount, view);
            loading = true;
        }

        // If dy is negative, the RecyclerView is scrolling up
        scrollingUp = dy < 0;

        // If dy is positive, the RecyclerView is scrolling down
        scrollingDown = dy > 0;

        if (scrollingUp) {
            onScrollListener.onScrolledUp();
        } else if (scrollingDown) {
            onScrollListener.onScrolledDown();
        }

        // Check if the user has reached the bottom
        LinearLayoutManager layoutManager = (LinearLayoutManager) mLayoutManager;
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (lastVisibleItemPosition == totalItemCount - 1) {
            onScrollListener.onReachedBottom();
        }

        // Check if the user has reached the top
        if (firstVisibleItemPosition == 0) {
            onScrollListener.onReachedTop();
        }
    }

    public void resetState() {
        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
    }

    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);
}
