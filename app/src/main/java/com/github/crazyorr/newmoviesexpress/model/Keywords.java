package com.github.crazyorr.newmoviesexpress.model;

import java.util.List;

/**
 * Created by wanglei02 on 2015/11/10.
 */
public class Keywords {
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
}
