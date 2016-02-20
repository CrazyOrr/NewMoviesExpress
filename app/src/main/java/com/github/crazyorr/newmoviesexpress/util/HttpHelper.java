package com.github.crazyorr.newmoviesexpress.util;

import com.github.crazyorr.newmoviesexpress.model.DoubanService;
import com.github.crazyorr.newmoviesexpress.model.NewMoviesExpressService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class HttpHelper {
    private static OkHttpClient mClient;
    private static final Gson mGson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    public static DoubanService mDoubanService;
    public static NewMoviesExpressService mNewMoviesExpressService;

    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl("https://api.douban.com")
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();

        mDoubanService = retrofit.create(DoubanService.class);

        retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl("https://new-movies-express.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();
        mNewMoviesExpressService = retrofit.create(NewMoviesExpressService.class);
    }
}
