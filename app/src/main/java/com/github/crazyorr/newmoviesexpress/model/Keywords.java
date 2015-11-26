package com.github.crazyorr.newmoviesexpress.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by wanglei02 on 2015/11/10.
 */
public class Keywords {
    private static final Gson mGson = new Gson();

    private List<String> titles;
    private List<String> casts;
    private List<String> directors;

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public List<String> getCasts() {
        return casts;
    }

    public void setCasts(List<String> casts) {
        this.casts = casts;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public static Keywords fromJson(String json) {
        Keywords keywords = mGson.fromJson(json, Keywords.class);
        if(keywords == null){
            keywords = new Keywords();
        }
        return keywords;
    }

    public String toJson() {
        return mGson.toJson(this);
    }
}
