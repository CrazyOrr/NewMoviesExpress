package com.github.crazyorr.newmoviesexpress.model;

/**
 * Created by wanglei02 on 2016/3/10.
 */
public class LoginParams {
    private String username;
    private String password;

    public LoginParams(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
