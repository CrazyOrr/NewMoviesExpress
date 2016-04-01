package com.github.crazyorr.newmoviesexpress.fragment;

import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;

import retrofit2.Call;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class ComingSoonMovieListFragment extends MovieListFragment {
    public static ComingSoonMovieListFragment newInstance() {
        ComingSoonMovieListFragment fragment = new ComingSoonMovieListFragment();
        return fragment;
    }

    @Override
    protected Call<PagedList<MovieSimple>> loadData(int start, int count) {
        return HttpHelper.mDoubanService.listComingSoonMovies(start, count);
    }
}
