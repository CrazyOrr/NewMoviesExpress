package com.github.crazyorr.newmoviesexpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.widget.MyFragmentPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class NewMoviesFragment extends BaseFragment {
    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    public static NewMoviesFragment newInstance() {
        NewMoviesFragment fragment = new NewMoviesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_movies, container, false);
        ButterKnife.bind(this, view);

        Toolbar toolbar = findById(view, R.id.toolbar);
        getBaseActivity().setSupportActionBar(toolbar);

        final ActionBar ab = getBaseActivity().getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);

        final MyFragmentPagerAdapter<LazyLoadFragment> adapter = new MyFragmentPagerAdapter<>(getChildFragmentManager());
        /* When savedInstanceState != null(caused by configuration change) and the fragment instance has setRetainInstance(true),
        the fragment instance doesn't matter, FragmentPagerAdapter will use the retained on instead of the new one..
         */
        adapter.addFragment(InTheatersMovieListFragment.newInstance(), getString(R.string.in_theaters));
        adapter.addFragment(ComingSoonMovieListFragment.newInstance(), getString(R.string.coming_soon));
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findById(view, R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
