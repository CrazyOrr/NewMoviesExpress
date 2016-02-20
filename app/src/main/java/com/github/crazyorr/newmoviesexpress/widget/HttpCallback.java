package com.github.crazyorr.newmoviesexpress.widget;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by wanglei02 on 2015/10/14.
 */
public abstract class HttpCallback<T> implements Callback<T> {
    private static final String TAG = HttpCallback.class.getSimpleName();

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccess()) {
            onSuccess(call, response);
        } else {
            onFailure(call, new Throwable("code: " + response.code()
                    + " message: " + response.message()));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
//        Log.e(TAG, t.toString());
        t.printStackTrace();
    }

    public abstract void onSuccess(Call<T> call, Response<T> response);
}
