package com.startup.soundstack.customclass;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Dheeraj on 8/2/2015.
 */
public class CustomRecycleView extends RecyclerView {
    private int mLastVisibleItem;
    private OnLastVisible mCallbacks;
    private IScrollCallbacks mScrollCallbacks;
    private boolean loadMore;
    private boolean reachedEnd;

    public void setReachedEnd(boolean reachedEnd) {
        this.reachedEnd = reachedEnd;
    }

    public CustomRecycleView(Context context) {
        super(context);
        init();
    }

    public CustomRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    if (newState == SCROLL_STATE_IDLE) {
                                        if (mScrollCallbacks != null)
                                            mScrollCallbacks.onIdleScroll();
                                    } else if (mScrollCallbacks != null)
                                        mScrollCallbacks.onScroll();
                                    super.onScrollStateChanged(recyclerView, newState);
                                }

                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    int lastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                    int firstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                                    if(mScrollCallbacks != null){
                                        mScrollCallbacks.onScrolled(firstVisible);
                                    }
                                    if (!reachedEnd && recyclerView.getAdapter().getItemCount() - 1 == lastVisible && lastVisible != mLastVisibleItem &&  !loadMore) {
                                        mLastVisibleItem = lastVisible;
                                        if (mCallbacks != null) {
                                            loadMore = true;
                                            mCallbacks.loadMore();
                                        }
                                    }
                                }
                            }
        );
    }

    public OnLastVisible getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(OnLastVisible callbacks) {
        mCallbacks = callbacks;
    }


    public IScrollCallbacks getScrollCallbacks() {
        return mScrollCallbacks;
    }

    public void setScrollCallbacks(IScrollCallbacks scrollCallbacks) {
        mScrollCallbacks = scrollCallbacks;
    }

    public void setLastVisibleItem(int mLastVisibleItem) {
        this.mLastVisibleItem = mLastVisibleItem;
    }

    public interface IScrollCallbacks {
        void onScroll();

        void onIdleScroll();

        void onScrolled(int i);
    }

    public interface OnLastVisible {
        void loadMore();
    }

    public boolean isLoadMore() {
        return loadMore;
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }
}