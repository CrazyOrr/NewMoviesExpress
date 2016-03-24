package com.github.crazyorr.newmoviesexpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.crazyorr.newmoviesexpress.BuildConfig;
import com.github.crazyorr.newmoviesexpress.R;

import java.util.Calendar;

import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class HelpFragment extends BaseFragment {

    private static final String TAG = HelpFragment.class.getSimpleName();

    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_help, container, false);
        ButterKnife.bind(this, view);

        Toolbar toolbar = findById(view, R.id.toolbar);
        toolbar.setTitle(R.string.help);
        getBaseActivity().setSupportActionBar(toolbar);

        final ActionBar ab = getBaseActivity().getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        ab.setDisplayHomeAsUpEnabled(true);

        TextView tvVersion = findById(view, R.id.tv_version);
        tvVersion.setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME));

        TextView tvCopyright = findById(view, R.id.tv_copyright);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(String.format(getString(R.string.copyright), year));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
