package com.github.crazyorr.newmoviesexpress.controller;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class HttpCallback implements Callback {
    private static final String TAG = HttpCallback.class.getSimpleName();

    @Override
    public void onFailure(Request request, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        Log.i(TAG, response.toString());
    }
}
