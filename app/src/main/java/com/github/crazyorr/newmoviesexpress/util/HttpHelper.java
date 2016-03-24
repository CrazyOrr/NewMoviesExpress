package com.github.crazyorr.newmoviesexpress.util;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class HttpHelper {
    private static final Gson mGson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    public static DoubanService mDoubanService;
    public static NewMoviesExpressService mNewMoviesExpressService;
    public static Converter<ResponseBody, ApiError> mErrorConverter;
    private static OkHttpClient mClient;

    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        mClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request;
//                        if (!TextUtils.isEmpty(GlobalVar.token)) {
//                            request = chain.request().newBuilder()
//                                    .addHeader("Authorization", GlobalVar.token).build();
//                        } else {
//                            request = chain.request();
//                        }
//
//                        return chain.proceed(request);
//                    }
//                })
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl("https://api.douban.com")
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();

        mDoubanService = retrofit.create(DoubanService.class);

        retrofit = new Retrofit.Builder()
                .client(mClient)
                .baseUrl("https://new-movies-express.herokuapp.com")
//                .baseUrl("http://172.18.67.30:3000")
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();
        mNewMoviesExpressService = retrofit.create(NewMoviesExpressService.class);

        mErrorConverter = retrofit.responseBodyConverter(ApiError.class, ApiError.class.getAnnotations());
    }
}
