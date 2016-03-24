package com.github.crazyorr.newmoviesexpress.util;

import android.text.TextUtils;

import com.github.crazyorr.newmoviesexpress.model.MovieSimple;

/**
 * Created by wanglei02 on 2015/10/20.
 */
public class GlobalVar {
    public static MovieSimple selectedMovie;
    public static String token;

    public static boolean isLoggedIn() {
        return !TextUtils.isEmpty(token);
    }
}
