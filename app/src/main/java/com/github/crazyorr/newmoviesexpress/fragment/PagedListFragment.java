package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.databinding.ViewLoadMoreFooterBinding;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.PagedStates;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class PagedListFragment<T> extends LazyLoadFragment {
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mAdapter;
    private int mItemCountPerLoad = Const.ITEM_COUNT_PER_PAGE;
    private List<T> mItems;
    private PagedStates mPagedStates;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
            mAdapter = buildAdapter(getActivity(), mItems);
        }
    }

    protected abstract RecyclerView.Adapter<? extends RecyclerView.ViewHolder> buildAdapter(Context context, List<T> items);

    @Nullable
    @Override
    final public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createView(inflater, container, savedInstanceState);
        mSwipeRefreshLayout = getSwipeRefreshLayout();
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        // 在顶部向下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });

        // 滑到底部加载更多
        getNestedScrollView().setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // We take the last son in the scrollview
                View view = v.getChildAt(v.getChildCount() - 1);
                int diff = (view.getBottom() - (v.getHeight() + v.getScrollY()));
                int footerHeight = getFooter().llFooter.getHeight();
                boolean hasScrolledDown = scrollY - oldScrollY > 0;
                boolean hasReachedBottom = diff <= footerHeight;
                if (hasScrolledDown && hasReachedBottom) {
                    if (mPagedStates.hasMoreToLoad()) {
                        loadAsync(mPagedStates.getCur(), mItemCountPerLoad);
                    }
                }
            }
        });

        // 点击footer加载更多
        getFooter().llFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPagedStates.hasMoreToLoad()) {
                    loadAsync(mPagedStates.getCur(), mItemCountPerLoad);
                }
            }
        });

        mRecyclerView = getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(mRecyclerView.getContext(), 3));
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = getResources().getDimensionPixelSize(R.dimen.movie_item_vertical_spacing);
            }
        });
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);

        mPagedStates = new PagedStates();
        mPagedStates.setOnStateChangeListener(new PagedStates.OnStateChangeListener() {
            @Override
            public void onStateChange(PagedStates.State state, int cur, int total, String message) {
                if (state != PagedStates.State.LOADING) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                ViewLoadMoreFooterBinding footer = getFooter();

                switch (state) {
                    case FAILED:
                    case LOADING:
                    case NO_MORE_CONTENT:
                        if (cur > 0) {
                            footer.llFooter.setVisibility(View.VISIBLE);
                        } else {
                            footer.llFooter.setVisibility(View.INVISIBLE);
                            break;
                        }

                        switch (state) {
                            case LOADING:
                                footer.tvLoadingMore.setText(R.string.loading);
                                break;
                            case NO_MORE_CONTENT:
                                footer.tvLoadingMore.setText(R.string.no_more_content);
                                break;
                            case FAILED:
                                footer.tvLoadingMore.setText(R.string.load_more_fail);
                                break;
                        }

                        break;
                    default:
                        footer.llFooter.setVisibility(View.INVISIBLE);
                }

                if (state == PagedStates.State.LOADING) {
                    footer.pbLoadingMore.setVisibility(View.VISIBLE);
                } else {
                    footer.pbLoadingMore.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    @Nullable
    public abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void load() {
        if (mPagedStates.getState() == PagedStates.State.READY
                && mPagedStates.getCur() == 0) {
            refresh(false);
        }
    }

    private void refresh(boolean isTriggeredBySwipe) {
        if (!isTriggeredBySwipe) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }

        mPagedStates.setCur(0);
        mItems.clear();
        mAdapter.notifyDataSetChanged();
        loadAsync(0, mItemCountPerLoad);
    }

    protected abstract RecyclerView getRecyclerView();

    protected abstract ViewLoadMoreFooterBinding getFooter();

    protected abstract SwipeRefreshLayout getSwipeRefreshLayout();

    protected abstract NestedScrollView getNestedScrollView();

    protected abstract Call<PagedList<T>> loadData(int start, int count);

    private void loadAsync(final int start, int count) {
        if (mPagedStates.getState() == PagedStates.State.LOADING) {
            return;
        } else {
            mPagedStates.onPageLoading();
        }
        loadData(start, count).enqueue(new HttpCallback<PagedList<T>>() {
            @Override
            public void onSuccess(Call<PagedList<T>> call, Response<PagedList<T>> response) {
                PagedList<T> list = response.body();
                mItems.addAll(list.getSubjects());
                int realStart = list.getStart();
                int realCount = list.getCount();
                mAdapter.notifyItemRangeChanged(realStart, realCount);
                mPagedStates.onPageLoadSucceed(realStart, realCount, list.getTotal());
            }

            @Override
            public void onError(Call<PagedList<T>> call, Response<PagedList<T>> response, ApiError error) {
                mPagedStates.onPageLoadFail(error.getMsg());
            }

            @Override
            public void onFailure(Call<PagedList<T>> call, Throwable t) {
                super.onFailure(call, t);
                mPagedStates.onPageLoadFail(getString(R.string.load_fail));
            }
        });
    }
}
