package com.github.crazyorr.newmoviesexpress.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.controller.IFooter;
import com.github.crazyorr.newmoviesexpress.model.PagedList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import butterknife.ButterKnife;

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
    private LoadCallback mLoadCallback;
    private ExecutorService mExecutor;

    private boolean isBottomReached;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemCountPerLoad = getResources().getInteger(R.integer.item_count_per_load);
        if (savedInstanceState == null) {
            mItems = new ArrayList<>();
            mAdapter = buildAdapter(getActivity(), mItems);
            mLoadCallback = new LoadCallback<T>() {
                @Override
                public void onSucceed(PagedList<T> list) {
                    mTotal = list.getTotal();
                    mCurIndex = list.getStart() + list.getCount();
                    addItems(list.getSubjects());
                    isBottomReached = false;
                }

                @Override
                public void onFail() {
                    getSupportActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissFooter();
                            Toast.makeText(getContext(), R.string.load_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            mExecutor = Executors.newCachedThreadPool();
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
        //TODO 修改为UltimateRecyclerView
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
                            loadAsync(mCurIndex, mItemCountPerLoad, mLoadCallback);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void clearItems() {
        mItems.clear();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void load() {
        if (mCurIndex < 0) {
            loadAsync(0, mItemCountPerLoad, mLoadCallback);
        }
    }

    protected abstract int getRecyclerViewId();

    protected abstract FutureTask<? extends PagedList<T>> loadData(int start, int count);

    private void loadAsync(int start, int count, LoadCallback<T> callback) {
        showFooter();
        mExecutor.execute(new LoadRunnable(loadData(start, count), callback));
    }

    private void showFooter(){
        if(mAdapter instanceof IFooter){
            ((IFooter) mAdapter).showFooter();
        }
    }

    private void dismissFooter(){
        if(mAdapter instanceof IFooter){
            ((IFooter) mAdapter).dismissFooter();
        }
    }

    interface LoadCallback<T> {
        void onSucceed(PagedList<T> items);

        void onFail();
    }

    class LoadRunnable implements Runnable {

        private FutureTask<? extends PagedList<T>> mFuture;
        private LoadCallback<T> mCallback;

        public LoadRunnable(FutureTask<? extends PagedList<T>> future, LoadCallback<T> callback) {
            mFuture = future;
            mCallback = callback;
        }

        @Override
        public void run() {
            mFuture.run();
            try {
                mCallback.onSucceed(mFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
                mCallback.onFail();
            } catch (ExecutionException e) {
                e.printStackTrace();
                mCallback.onFail();
            }
        }
    }

}
