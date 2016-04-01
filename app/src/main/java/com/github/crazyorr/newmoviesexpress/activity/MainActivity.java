package com.github.crazyorr.newmoviesexpress.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.BackgroundService;
import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.databinding.NavHeaderBinding;
import com.github.crazyorr.newmoviesexpress.fragment.HelpFragment;
import com.github.crazyorr.newmoviesexpress.fragment.NewMoviesFragment;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.UserInfo;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.GlobalVar;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.util.Util;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    private final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private final static int REQUEST_CODE_LOGIN = 0;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private NavHeaderBinding mNavHeaderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        NavigationView navigationView = ButterKnife.findById(this, R.id.nav_view);
        setupDrawerContent(navigationView);

        mNavHeaderBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0));

        String token = Util.loadTokenFromPreference(this);
        if (!TextUtils.isEmpty(token)) {
            refreshUserInfo(token);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.id_container, NewMoviesFragment.newInstance())
                    .commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        checkDangerousPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Intent intent = new Intent(MainActivity.this, BackgroundService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.saveTokenToPreference(MainActivity.this, GlobalVar.getToken());
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

    public void onUsernameClick(View v) {
        if (GlobalVar.hasToken()) {
            logout();
        } else {
            goToLogin();
        }
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
                                if (GlobalVar.hasToken()) {
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
                mNavHeaderBinding.setUserInfo(userInfo);
                GlobalVar.setToken(token);
            }

            @Override
            public void onError(Call<UserInfo> call, Response<UserInfo> response, ApiError error) {
                Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        HttpHelper.mNewMoviesExpressService.logout(GlobalVar.getToken()).enqueue(new HttpCallback<Void>() {
            @Override
            public void onSuccess(Call<Void> call, Response<Void> response) {
                mNavHeaderBinding.setUserInfo(null);
                GlobalVar.setToken(null);
            }

            @Override
            public void onError(Call<Void> call, Response<Void> response, ApiError error) {
                Toast.makeText(MainActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }

    private void checkDangerousPermission(String permission) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
