package com.github.crazyorr.newmoviesexpress;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by wanglei02 on 2016/3/4.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
