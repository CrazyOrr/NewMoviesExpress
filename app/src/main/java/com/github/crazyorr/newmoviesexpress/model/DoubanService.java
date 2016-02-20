package com.github.crazyorr.newmoviesexpress.model;

import com.github.crazyorr.newmoviesexpress.util.SensitiveConst;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by wanglei02 on 2016/2/18.
 */
public interface DoubanService {
    String VERSION = "v2";

    @GET("/" + VERSION + "/movie/in_theaters?apikey=" + SensitiveConst.API_KEY)
    Call<PagedList<MovieSimple>> listInTheatersMovies(
            @Query("start") int start, @Query("count") int count
    );

    @GET("/" + VERSION + "/movie/coming_soon?apikey=" + SensitiveConst.API_KEY)
    Call<PagedList<MovieSimple>> listComingSoonMovies(
            @Query("start") int start, @Query("count") int count);

    @GET("/" + VERSION + "/movie/subject/{id}?apikey=" + SensitiveConst.API_KEY)
    Call<MovieDetail> getMovieDetail(@Path("id") String movieId);
}
