package com.github.crazyorr.newmoviesexpress.widget;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by wanglei02 on 2016/3/15.
 */
public class OnRecyclerViewScrollListener extends RecyclerView.OnScrollListener
        implements OnEndReachedListener {

    private int[] lastVisibleItemPositions;
    private boolean isOnBottomReachNotifiedOnOneScroll;

    private static int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                isOnBottomReachNotifiedOnOneScroll = false;
                break;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0) {
            checkPositionToNotify(recyclerView);
        }
    }

    private void checkPositionToNotify(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition;
        if (layoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            if (lastVisibleItemPositions == null) {
                lastVisibleItemPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            }
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastVisibleItemPositions);
            lastVisibleItemPosition = findMax(lastVisibleItemPositions);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager.");
        }
        if (visibleItemCount > 0) {
            if (lastVisibleItemPosition >= totalItemCount - 1) {
                if (!isOnBottomReachNotifiedOnOneScroll) {
                    onEndReached();
                    isOnBottomReachNotifiedOnOneScroll = true;
                }
            }
        }
    }

    @Override
    public void onEndReached() {
    }
}
