package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.IFooter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class PagedListFragment<T> extends LazyLoadFragment {
    private static final String TAG = PagedListFragment.class.getSimpleName();

    RecyclerView mRecyclerView;
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> mAdapter;
    private int mItemCountPerLoad;
    int mTotal = 0;
    int mCurIndex = -1;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private List<T> mItems;

    private boolean isBottomReached;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemCountPerLoad = getResources().getInteger(R.integer.item_count_per_load);
        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
            mAdapter = buildAdapter(getActivity(), mItems);
        }
    }

    protected abstract RecyclerView.Adapter<? extends RecyclerView.ViewHolder> buildAdapter(Context context, List<T> items);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = ButterKnife.findById(view, getRecyclerViewId());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
//        mRecyclerView.setLayoutManager(new GridLayoutManager(mRecyclerView.getContext(), 3));
        mRecyclerView.setAdapter(mAdapter);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (!isBottomReached) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        isBottomReached = true;
                        if (mCurIndex < mTotal) {
                            loadAsync(mCurIndex, mItemCountPerLoad);
                        } else {
                            dismissFooter();
                        }
                    }
                }
            }
        };
        mRecyclerView.addOnScrollListener(mOnScrollListener);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void load() {
        if (mCurIndex < 0) {
            loadAsync(0, mItemCountPerLoad);
        }
    }

    protected abstract int getRecyclerViewId();

    protected abstract Call<PagedList<T>> loadData(int start, int count);

    private void loadAsync(int start, int count) {
        showFooter();
        loadData(start, count).enqueue(new HttpCallback<PagedList<T>>() {
            @Override
            public void onSuccess(Call<PagedList<T>> call, Response<PagedList<T>> response) {
                PagedList<T> list = response.body();
                mTotal = list.getTotal();
                mCurIndex = list.getStart() + list.getCount();
                addItems(list.getSubjects());
                isBottomReached = false;
            }

            @Override
            public void onFailure(Call<PagedList<T>> call, Throwable t) {
                super.onFailure(call, t);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissFooter();
                        Toast.makeText(getContext(), R.string.load_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showFooter() {
        if (mAdapter instanceof IFooter) {
            ((IFooter) mAdapter).showFooter();
        }
    }

    private void dismissFooter() {
        if (mAdapter instanceof IFooter) {
            ((IFooter) mAdapter).dismissFooter();
        }
    }
}
