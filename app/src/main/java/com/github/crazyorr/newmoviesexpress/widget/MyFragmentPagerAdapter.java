package com.github.crazyorr.newmoviesexpress.widget;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.github.crazyorr.newmoviesexpress.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanglei02 on 2015/11/10.
 */
public class MyFragmentPagerAdapter<T extends BaseFragment> extends FragmentPagerAdapter {
    private final List<T> mFragments = new ArrayList<>();
    private final List<String> mFragmentTitles = new ArrayList<>();

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public T getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }

    public void addFragment(T fragment, String title) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
    }

    public void clear() {
        mFragments.clear();
        mFragmentTitles.clear();
    }
}
