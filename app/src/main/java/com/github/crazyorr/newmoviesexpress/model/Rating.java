package com.github.crazyorr.newmoviesexpress.model;

/**
 * Created by wanglei02 on 2015/10/14.
 */
public class Rating {
    private int max;
    private int min;
    private float average;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public float getAverage() {
        return average;
    }

    public void setAverage(float average) {
        this.average = average;
    }
}
