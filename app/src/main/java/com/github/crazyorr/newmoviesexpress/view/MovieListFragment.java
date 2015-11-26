package com.github.crazyorr.newmoviesexpress.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.controller.MovieRecyclerViewAdapter;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class MovieListFragment extends PagedListFragment<MovieSimple> {
    private static final String TAG = MovieListFragment.class.getSimpleName();

    @Override
    protected RecyclerView.Adapter<? extends RecyclerView.ViewHolder> buildAdapter(Context context, List<MovieSimple> items) {
        return new MovieRecyclerViewAdapter(context, items);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerview;
    }

    @Override
    protected FutureTask<PagedList<MovieSimple>> loadData(int start, int count) {
        final HttpUrl url = getCommonUrlBuilder()
                .addPathSegment(getPathSegment())
                .addQueryParameter("start", String.valueOf(start))
                .addQueryParameter("count", String.valueOf(count))
                .build();

        return new FutureTask<PagedList<MovieSimple>>(new Callable<PagedList<MovieSimple>>() {
            @Override
            public PagedList<MovieSimple> call() throws Exception {
                String json = HttpHelper.get(url);
                Type type = new TypeToken<PagedList<MovieSimple>>() {
                }.getType();
                return PagedList.fromJson(json, type);
            }
        });
    }

    protected abstract String getPathSegment();

    protected HttpUrl.Builder getCommonUrlBuilder() {
        return Const.getDoubanUrlBuilder().addPathSegment("movie");
    }


}
