package com.github.crazyorr.newmoviesexpress.model;

import android.text.TextUtils;

import com.google.gson.Gson;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanglei02 on 2015/11/13.
 */
public class AddKeywordsData {
    private static final Gson mGson = new Gson();

    List<String> titles;
    List<String> casts;
    List<String> directors;

    public AddKeywordsData() {
        titles = new ArrayList<>();
        casts = new ArrayList<>();
        directors = new ArrayList<>();
    }

    public void addTitle(String title){
        titles.add(title);
    }

    public void addCast(String cast){
        casts.add(cast);
    }

    public void addDirector(String director){
        directors.add(director);
    }

    public static AddKeywordsData fromJson(String json) {
        Assert.assertTrue(!TextUtils.isEmpty(json));
        return mGson.fromJson(json, AddKeywordsData.class);
    }

    public String toJson() {
        return mGson.toJson(this);
    }
}
