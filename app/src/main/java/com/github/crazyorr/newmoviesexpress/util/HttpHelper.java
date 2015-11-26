package com.github.crazyorr.newmoviesexpress.util;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class HttpHelper {
    private static final OkHttpClient mClient = new OkHttpClient();
    public static final MediaType FORM_ENCODED
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static String get(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    public static void getAsync(HttpUrl url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        mClient.newCall(request).enqueue(callback);
    }

    public static String post(HttpUrl url, String content) throws IOException {
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    public static void postAsync(HttpUrl url, String content, Callback callback) {
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        mClient.newCall(request).enqueue(callback);
    }
}
