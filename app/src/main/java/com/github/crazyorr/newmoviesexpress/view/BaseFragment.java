package com.github.crazyorr.newmoviesexpress.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected BaseActivity getSupportActivity() {
        return (BaseActivity) getActivity();
    }

    public void showLoadingDialog() {
        getSupportActivity().showLoadingDialog();
    }

    public void showLoadingDialog(String prompt) {
        getSupportActivity().showLoadingDialog(prompt);
    }

    public void dismissLoadingDialog() {
        getSupportActivity().dismissLoadingDialog();
    }

}
