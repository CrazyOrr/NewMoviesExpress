package com.github.crazyorr.newmoviesexpress.view;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class InTheatersMovieListFragment extends MovieListFragment {
    private static final String TAG = InTheatersMovieListFragment.class.getSimpleName();

    public static InTheatersMovieListFragment newInstance() {
        InTheatersMovieListFragment fragment = new InTheatersMovieListFragment();
        return fragment;
    }

    @Override
    protected String getPathSegment() {
        return "in_theaters";
    }

}
