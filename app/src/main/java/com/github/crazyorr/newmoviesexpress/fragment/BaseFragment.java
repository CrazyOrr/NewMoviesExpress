package com.github.crazyorr.newmoviesexpress.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.crazyorr.newmoviesexpress.activity.BaseActivity;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public void showLoadingDialog() {
        getBaseActivity().showLoadingDialog();
    }

    public void showLoadingDialog(String prompt) {
        getBaseActivity().showLoadingDialog(prompt);
    }

    public void dismissLoadingDialog() {
        getBaseActivity().dismissLoadingDialog();
    }

    public void runOnUiThread(Runnable runnable) {
        Activity activity = getBaseActivity();
        if (activity != null) {
            activity.runOnUiThread(runnable);
        }
    }
}
