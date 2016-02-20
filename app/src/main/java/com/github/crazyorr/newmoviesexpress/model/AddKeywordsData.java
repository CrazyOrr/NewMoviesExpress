package com.github.crazyorr.newmoviesexpress.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanglei02 on 2015/11/13.
 */
public class AddKeywordsData {
    List<String> titles;
    List<String> casts;
    List<String> directors;

    public AddKeywordsData() {
        titles = new ArrayList<>();
        casts = new ArrayList<>();
        directors = new ArrayList<>();
    }

    public List<String> getTitles() {
        return titles;
    }

    public List<String> getCasts() {
        return casts;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public void addTitle(String title) {
        titles.add(title);
    }

    public void addCast(String cast) {
        casts.add(cast);
    }

    public void addDirector(String director) {
        directors.add(director);
    }
}
