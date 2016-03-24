package com.github.crazyorr.newmoviesexpress.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.BackgroundService;
import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.fragment.HelpFragment;
import com.github.crazyorr.newmoviesexpress.fragment.NewMoviesFragment;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.UserInfo;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int REQUEST_CODE_LOGIN = 0;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;
    TextView mTvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String token = sharedPref.getString(Const.SHARED_PREFERENCES_TOKEN, null);

        mTvUsername = ButterKnife.findById(mNavigationView.getHeaderView(0), R.id.tv_username);
        mTvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GlobalVar.isLoggedIn()) {
                    goToLogin();
                }
            }
        });
        if (!TextUtils.isEmpty(token)) {
            refreshUserInfo(token);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.id_container, NewMoviesFragment.newInstance())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Intent intent = new Intent(MainActivity.this, BackgroundService.class);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOGIN:
                if (resultCode == RESULT_OK) {
                    refreshUserInfo(data.getStringExtra(Const.INTENT_EXTRA_TOKEN));
                }
                break;
        }
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
                            case R.id.nav_notifications:
                                if (GlobalVar.isLoggedIn()) {
                                    startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                                } else {
                                    Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                                    goToLogin();
                                }
                                break;
                            case R.id.nav_settings:
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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

    private void refreshUserInfo(final String token) {
        HttpHelper.mNewMoviesExpressService.userinfo(token).enqueue(new HttpCallback<UserInfo>() {
            @Override
            public void onSuccess(Call<UserInfo> call, Response<UserInfo> response) {
                UserInfo userInfo = response.body();
                mTvUsername.setText(userInfo.getUsername());
                GlobalVar.token = token;
            }

            @Override
            public void onError(Call<UserInfo> call, Response<UserInfo> response, ApiError error) {
                Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                GlobalVar.token = null;
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }
}
