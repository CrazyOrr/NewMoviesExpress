package com.github.crazyorr.newmoviesexpress.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by wanglei02 on 2015/10/23.
 */
public abstract class LazyLoadFragment extends BaseFragment {

    // load only when ready
    private boolean shouldLoad = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            onLoad();
        }
    }

    final public void onLoad() {
        if (shouldLoad) {
            load();
        } else {
            shouldLoad = true;
        }
    }

    public abstract void load();
}
