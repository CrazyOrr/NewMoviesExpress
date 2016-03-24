package com.github.crazyorr.newmoviesexpress.util;

import com.github.crazyorr.newmoviesexpress.model.MovieNotificationStatus;
import com.github.crazyorr.newmoviesexpress.model.MovieSimple;
import com.github.crazyorr.newmoviesexpress.model.PagedList;
import com.github.crazyorr.newmoviesexpress.model.TokenInfo;
import com.github.crazyorr.newmoviesexpress.model.UserInfo;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by wanglei02 on 2016/2/18.
 */
public interface NewMoviesExpressService {

    int NOTIFICATIONS_ALL = 0;
    int NOTIFICATIONS_DUE = 1;
    String QUERY_PARAM_BEFORE = "before";
    String QUERY_PARAM_AFTER = "after";

    @GET("api/movie/{id}/notification")
    Call<MovieNotificationStatus> queryMovieNotification(@Path("id") String movieId,
                                                         @Header("Authorization") String token);

    @POST("api/movie/{id}/notification")
    Call<Void> addMovieNotification(@Path("id") String movieId,
                                    @Header("Authorization") String token);

    @DELETE("api/movie/{id}/notification")
    Call<Void> removeMovieNotification(@Path("id") String movieId,
                                       @Header("Authorization") String token);

    @FormUrlEncoded
    @POST("api/login")
    Call<TokenInfo> login(@Field("username") String username, @Field("password") String password);

    @GET("api/notifications")
    Call<PagedList<MovieSimple>> notifications(@Header("Authorization") String token,
                                               @Query("start") int start,
                                               @Query("count") int count,
                                               @Query("filter") int filter,
                                               @QueryMap Map<String, String> params);

    @GET("api/userinfo")
    Call<UserInfo> userinfo(@Header("Authorization") String token);
}
