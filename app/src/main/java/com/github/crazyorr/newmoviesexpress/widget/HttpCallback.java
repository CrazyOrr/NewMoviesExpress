package com.github.crazyorr.newmoviesexpress.widget;

import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.github.crazyorr.newmoviesexpress.util.HttpHelper;

import java.io.IOException;

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
            ApiError error = null;
            try {
                error = HttpHelper.mErrorConverter.convert(response.errorBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
            onError(call, response, error);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
//        Log.e(TAG, t.toString());
        t.printStackTrace();
    }

    public abstract void onSuccess(Call<T> call, Response<T> response);

    public abstract void onError(Call<T> call, Response<T> response, ApiError error);
}
