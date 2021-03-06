package com.github.crazyorr.newmoviesexpress.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Created by wanglei02 on 2015/11/5.
 */
public class Util {
    private static final String DELIMITER = ",";

    public static <T> String flat(List<T> list, Function<T, String> toString) {
        return TextUtils.join(DELIMITER, Lists.transform(list, toString));
    }

    public static <T> String flat(List<T> list) {
        return TextUtils.join(DELIMITER, list);
    }

    /**
     * 是否有sd卡
     *
     * @return
     */
    public static boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获得应用存储目录
     *
     * @param context
     * @return
     */
    public static String getSavingPath(Context context) {
        String path;
        if (isSdCardMounted()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath();
        }
        return path + File.separator + context.getPackageName() + File.separator;
    }

    public static void saveTokenToPreference(Context context, String token) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Const.SHARED_PREFERENCES_TOKEN, token);
        editor.apply();
    }

    public static String loadTokenFromPreference(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(Const.SHARED_PREFERENCES_TOKEN, null);
    }
}
