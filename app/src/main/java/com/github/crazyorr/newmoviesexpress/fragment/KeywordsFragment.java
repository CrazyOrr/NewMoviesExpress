package com.github.crazyorr.newmoviesexpress.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.activity.AddKeywordsActivity;
import com.github.crazyorr.newmoviesexpress.model.Keywords;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;
import com.github.crazyorr.newmoviesexpress.widget.MyFragmentPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class KeywordsFragment extends BaseFragment {

    private static final String TAG = KeywordsFragment.class.getSimpleName();

    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    public static KeywordsFragment newInstance() {
        KeywordsFragment fragment = new KeywordsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_keywords, container, false);
        ButterKnife.bind(this, view);

        Toolbar toolbar = findById(view, R.id.toolbar);
        toolbar.setTitle(R.string.keywords);
        getBaseActivity().setSupportActionBar(toolbar);

        final ActionBar ab = getBaseActivity().getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findById(view, R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseActivity(), AddKeywordsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            HttpHelper.mNewMoviesExpressService.keywords().enqueue(new HttpCallback<Keywords>() {

                @Override
                public void onSuccess(retrofit2.Call<Keywords> call, retrofit2.Response<Keywords> response) {
                    final Keywords keywords = response.body();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MyFragmentPagerAdapter<BaseFragment> adapter = new MyFragmentPagerAdapter<>(getChildFragmentManager());
                            adapter.addFragment(KeywordListFragment.newInstance(keywords.getTitles()), getString(R.string.titles));
                            adapter.addFragment(KeywordListFragment.newInstance(keywords.getCasts()), getString(R.string.casts));
                            adapter.addFragment(KeywordListFragment.newInstance(keywords.getDirectors()), getString(R.string.directors));
                            mViewPager.setAdapter(adapter);
                            mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                }

                                @Override
                                public void onPageSelected(int position) {
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {
                                }
                            };
                            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
                            mTabLayout.setupWithViewPager(mViewPager);
                        }
                    });
                }

                @Override
                public void onFailure(retrofit2.Call<Keywords> call, Throwable t) {
                    super.onFailure(call, t);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.load_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        ButterKnife.unbind(this);
    }

}
