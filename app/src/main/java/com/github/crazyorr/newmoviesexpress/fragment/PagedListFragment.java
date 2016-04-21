package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.OnRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class PagedListFragment<T> extends LazyLoadFragment {
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mAdapter;
    int mTotal = 0;
    int mCur = -1;
    private int mItemCountPerLoad = Const.ITEM_COUNT_PER_PAGE;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private List<T> mItems;

    private boolean isLoading;

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
        mSwipeRefreshLayout = ButterKnife.findById(view, getSwipeRefreshLayoutId());
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(true);
            }
        });
        mRecyclerView = ButterKnife.findById(view, getRecyclerViewId());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(mRecyclerView.getContext(), 3));
        mRecyclerView.setAdapter(mAdapter);
        mOnScrollListener = new OnRecyclerViewScrollListener() {
            @Override
            public void onEndReached() {
                super.onEndReached();
                if (!isLoading) {
                    if (mCur < mTotal) {
                        loadAsync(mCur, mItemCountPerLoad);
                        Toast.makeText(getContext(), R.string.loading,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.all_loaded,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        return view;
    }

    @Nullable
    public abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
    }

    public void addItems(List<T> items) {
        mItems.addAll(items);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void clearItems() {
        mItems.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void load() {
        if (mCur < 0) {
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

        loadAsync(0, mItemCountPerLoad);
    }

    private void onRefreshComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
        isLoading = false;
    }

    protected abstract int getRecyclerViewId();

    protected abstract int getSwipeRefreshLayoutId();

    protected abstract Call<PagedList<T>> loadData(int start, int count);

    private void loadAsync(final int start, int count) {
        isLoading = true;
        loadData(start, count).enqueue(new HttpCallback<PagedList<T>>() {
            @Override
            public void onSuccess(Call<PagedList<T>> call, Response<PagedList<T>> response) {
                if (start < mCur) {
                    clearItems();
                }
                PagedList<T> list = response.body();
                mTotal = list.getTotal();
                mCur = list.getStart() + list.getCount();
                addItems(list.getSubjects());
                onRefreshComplete();
            }

            @Override
            public void onError(Call<PagedList<T>> call, Response<PagedList<T>> response, ApiError error) {
                onRefreshComplete();
                Toast.makeText(getContext(), error.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<PagedList<T>> call, Throwable t) {
                super.onFailure(call, t);
                onRefreshComplete();
                Toast.makeText(getContext(), R.string.load_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
