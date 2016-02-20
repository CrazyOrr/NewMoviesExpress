package com.github.crazyorr.newmoviesexpress.model;

import java.util.List;

/**
 * Created by wanglei02 on 2015/10/13.
 */
public class PagedList<T> {
    private int count;
    private int start;
    private int total;
    private List<T> subjects;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<T> subjects) {
        this.subjects = subjects;
    }
}
