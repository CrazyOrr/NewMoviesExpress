package com.github.crazyorr.newmoviesexpress.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.fragment.SettingsFragment;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/11/17.
 */
public class SettingsActivity extends BackableActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findById(this, R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .replace(R.id.fl_container, new SettingsFragment())
                    .commit();
        }
    }
}
