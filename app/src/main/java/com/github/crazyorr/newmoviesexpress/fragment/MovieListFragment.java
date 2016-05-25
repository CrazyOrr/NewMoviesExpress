package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.databinding.FragmentMovieListBinding;
import com.github.crazyorr.newmoviesexpress.databinding.ViewLoadMoreFooterBinding;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.widget.MovieRecyclerViewAdapter;

import java.util.List;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class MovieListFragment extends PagedListFragment<MovieSimple> {
    private FragmentMovieListBinding mBinding;

    @Override
    protected RecyclerView.Adapter<? extends RecyclerView.ViewHolder> buildAdapter(Context context, List<MovieSimple> items) {
        return new MovieRecyclerViewAdapter(context, items);
    }

    @Nullable
    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_movie_list, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return mBinding.recyclerview;
    }

    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return mBinding.swiperefresh;
    }

    @Override
    protected ViewLoadMoreFooterBinding getFooter() {
        return mBinding.footer;
    }

    @Override
    protected NestedScrollView getNestedScrollView() {
        return mBinding.nestedScrollView;
    }
}
