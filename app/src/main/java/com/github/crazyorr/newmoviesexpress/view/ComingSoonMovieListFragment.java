package com.github.crazyorr.newmoviesexpress.view;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class ComingSoonMovieListFragment extends MovieListFragment {
    private static final String TAG = ComingSoonMovieListFragment.class.getSimpleName();

    public static ComingSoonMovieListFragment newInstance() {
        ComingSoonMovieListFragment fragment = new ComingSoonMovieListFragment();
        return fragment;
    }

    @Override
    protected String getPathSegment() {
        return "coming_soon";
    }
}
