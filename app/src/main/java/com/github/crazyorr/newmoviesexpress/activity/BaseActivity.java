package com.github.crazyorr.newmoviesexpress.activity;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.github.crazyorr.newmoviesexpress.fragment.LoadingDialogFragment;

/**
 * Created by wanglei02 on 2015/11/12.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private DialogFragment mLoadingDialogFragment;

    public void showLoadingDialog() {
        showLoadingDialog(null);
    }

    public void showLoadingDialog(String prompt) {
        mLoadingDialogFragment = LoadingDialogFragment.newInstance(prompt);
        mLoadingDialogFragment.setCancelable(false);
        mLoadingDialogFragment.show(getSupportFragmentManager(), this.getClass().getSimpleName());
    }

    public void dismissLoadingDialog() {
        mLoadingDialogFragment.dismiss();
    }
}
