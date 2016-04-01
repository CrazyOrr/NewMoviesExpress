package com.github.crazyorr.newmoviesexpress.util;

import android.text.TextUtils;

import com.github.crazyorr.newmoviesexpress.model.MovieSimple;

/**
 * Created by wanglei02 on 2015/10/20.
 */
public class GlobalVar {
    public static MovieSimple selectedMovie;
    private static String mToken;

    public static boolean hasToken() {
        return !TextUtils.isEmpty(mToken);
    }

    public static String getToken() {
        return mToken;
    }

    public static void setToken(String token) {
        if (!TextUtils.equals(mToken, token)) {
            mToken = token;
        }
    }
}
