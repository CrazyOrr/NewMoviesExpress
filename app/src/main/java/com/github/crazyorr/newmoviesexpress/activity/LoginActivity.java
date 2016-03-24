package com.github.crazyorr.newmoviesexpress.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.crazyorr.newmoviesexpress.R;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.model.TokenInfo;
import com.github.crazyorr.newmoviesexpress.util.Const;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;
import com.github.crazyorr.newmoviesexpress.widget.HttpCallback;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

import static butterknife.ButterKnife.findById;

/**
 * Created by wanglei02 on 2015/11/18.
 */
public class LoginActivity extends BackableActivity {

    @Bind(R.id.et_username)
    EditText etUsername;
    @Bind(R.id.et_password)
    EditText etPassword;
//    @Bind(R.id.btn_login)
//    Button btnLogin;
//    @Bind(R.id.btn_signup)
//    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Toolbar toolbar = findById(this, R.id.toolbar);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @OnClick({R.id.btn_login, R.id.btn_signup})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "请填写用户名和密码",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                showLoadingDialog();
                HttpHelper.mNewMoviesExpressService.login(username, password)
                        .enqueue(new HttpCallback<TokenInfo>() {
                            @Override
                            public void onSuccess(Call<TokenInfo> call, Response<TokenInfo> response) {
                                dismissLoadingDialog();
                                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                                final TokenInfo tokenInfo = response.body();
                                String token = tokenInfo.getToken();

                                // save token
                                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(Const.SHARED_PREFERENCES_TOKEN, token);
                                editor.apply();

                                Intent data = new Intent();
                                data.putExtra(Const.INTENT_EXTRA_TOKEN, token);
                                setResult(RESULT_OK, data);
                                finish();
                            }

                            @Override
                            public void onError(Call<TokenInfo> call, Response<TokenInfo> response, ApiError error) {
                                dismissLoadingDialog();
                                Toast.makeText(LoginActivity.this, error.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<TokenInfo> call, final Throwable t) {
                                super.onFailure(call, t);
                                dismissLoadingDialog();
                                if (t instanceof ConnectException) {
                                    Toast.makeText(LoginActivity.this, "连接失败，请检查网络",
                                            Toast.LENGTH_SHORT).show();
                                } else if (t instanceof SocketTimeoutException) {
                                    Toast.makeText(LoginActivity.this, "连接超时，请稍后重试",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case R.id.btn_signup:
                Toast.makeText(LoginActivity.this, "暂未开放",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
