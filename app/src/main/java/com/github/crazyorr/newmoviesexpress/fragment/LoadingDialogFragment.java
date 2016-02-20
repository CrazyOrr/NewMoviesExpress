package com.github.crazyorr.newmoviesexpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.crazyorr.newmoviesexpress.R;

import butterknife.ButterKnife;

/**
 * Created by wanglei02 on 2015/10/22.
 */
public class LoadingDialogFragment extends AppCompatDialogFragment {
    private static final String TAG = LoadingDialogFragment.class.getSimpleName();

    private static final String KEY_PROMPT = "KEY_PROMPT";

    private String mPrompt;

    public static LoadingDialogFragment newInstance(String prompt) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        if (prompt != null) {
            Bundle args = new Bundle();
            args.putString(KEY_PROMPT, prompt);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mPrompt = args.getString(KEY_PROMPT);
        }
        setStyle(AppCompatDialogFragment.STYLE_NO_FRAME, getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_loading, container, false);
        if (mPrompt != null) {
            TextView tvDescription = ButterKnife.findById(view, R.id.id_tv_prompt);
            tvDescription.setText(mPrompt);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.bg_loading_dialog);
    }
}
