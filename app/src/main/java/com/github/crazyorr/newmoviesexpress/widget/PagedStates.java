package com.github.crazyorr.newmoviesexpress.widget;

public class PagedStates {
    private int cur;
    private int total;
    private State state;
    private String message;
    private OnStateChangeListener mOnStateChangeListener;

    public PagedStates() {
        this(0);
    }

    public PagedStates(int cur) {
        this.cur = cur;
        setState(State.READY);
    }

    public void onPageLoading() {
        setState(State.LOADING);
    }

    public void onPageLoadSucceed(int start, int count, int total) {
        if (total <= 0) {
            setState(State.NO_CONTENT);
            return;
        }

        this.total = total;

        if (count <= 0) {
            if (cur <= 0) {
                setState(State.NO_CONTENT);
            } else {
                setState(State.NO_MORE_CONTENT);
            }
            return;
        }

        cur = start + count;
        if (hasMoreToLoad()) {
            setState(State.READY);
        } else {
            setState(State.NO_MORE_CONTENT);
        }
    }

    public void onPageLoadFail(String message) {
        this.message = message;
        setState(State.FAILED);
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = cur;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        notifyStateChange(state);
    }

    public boolean hasMoreToLoad() {
        return cur < total;
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public void notifyStateChange(State state) {
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.onStateChange(state, cur, total, message);
        }
    }

    public enum State {
        READY,
        LOADING,
        NO_MORE_CONTENT,
        NO_CONTENT,
        FAILED
    }

    public interface OnStateChangeListener {
        void onStateChange(State state, int cur, int total, String message);
    }
}
