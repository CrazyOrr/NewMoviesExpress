package com.github.crazyorr.newmoviesexpress.util;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.github.crazyorr.newmoviesexpress.model.ApiError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
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
        mClient = new OkHttpClient.Builder()
                .addInterceptor(new GzipRequestInterceptor())
                .addNetworkInterceptor(new StethoInterceptor())
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

    /**
     * This interceptor compresses the HTTP request body. Many webservers can't handle this!
     */
    static class GzipRequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }
}
