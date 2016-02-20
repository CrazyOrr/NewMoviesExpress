package com.github.crazyorr.newmoviesexpress.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by wanglei02 on 2016/2/18.
 */
public interface NewMoviesExpressService {

    @GET("/api/keywords")
    Call<Keywords> keywords();

    @GET("/api/notifications")
    Call<List<MovieSimple>> notifications();

    @POST("/add_keywords")
    Call<Void> addKeywords(@Body AddKeywordsData data);
}
