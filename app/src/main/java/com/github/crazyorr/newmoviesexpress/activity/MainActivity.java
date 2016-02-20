package com.github.crazyorr.newmoviesexpress.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.fragment.HelpFragment;
import com.github.crazyorr.newmoviesexpress.fragment.KeywordsFragment;
import com.github.crazyorr.newmoviesexpress.fragment.NewMoviesFragment;
import com.github.crazyorr.newmoviesexpress.service.BackgroundService;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.id_container, NewMoviesFragment.newInstance())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Fragment fragment = null;
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                fragment = NewMoviesFragment.newInstance();
                                break;
//                            case R.id.nav_notifications:
//                                fragment = NotificationsFragment.newInstance();
//                                break;
                            case R.id.nav_keywords:
                                fragment = KeywordsFragment.newInstance();
                                break;
                            case R.id.nav_settings:
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case R.id.nav_help:
                                fragment = HelpFragment.newInstance();
                                break;
                        }
                        if (fragment != null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.id_container, fragment)
                                    .commit();
                            menuItem.setChecked(true);
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
}
