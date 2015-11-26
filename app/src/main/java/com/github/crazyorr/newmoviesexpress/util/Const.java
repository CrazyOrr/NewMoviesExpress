package com.github.crazyorr.newmoviesexpress.util;

import com.squareup.okhttp.HttpUrl;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class Const {
    public static HttpUrl.Builder getDoubanUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.douban.com")
                .addPathSegment("v2")
                .addQueryParameter("apikey", SensitiveConst.API_KEY)
                .addQueryParameter("city", "上海")
                .addEncodedQueryParameter("mClient", "s%3Amobile%7Cy%3AAndroid+6.0%7Co%3A2172151%7Cf%3A70%7Cv%3A2.7.4%7Cm%3AYingyongbao_Market%7Cd%3A355470061907743%7Ce%3Amotorola+shamu%7Css%3A1440x2392")
                .addQueryParameter("udid", SensitiveConst.UDID);
    }

    public static HttpUrl.Builder getMyUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("new-movies-express.herokuapp.com");
    }
}
