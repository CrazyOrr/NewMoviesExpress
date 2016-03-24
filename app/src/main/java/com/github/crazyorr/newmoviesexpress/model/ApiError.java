package com.github.crazyorr.newmoviesexpress.model;

/**
 * Created by wanglei02 on 2016/3/11.
 */
public class ApiError {
    private int code;
    private String msg;
    private String request;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
