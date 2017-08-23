package com.dqsoftwaresolutions.feedMyRead.data;


import static android.R.attr.id;

public class User {

    private String name;
    private String token;
    private String password;
    private long changeTime;
    private int _id;


    public User(int id, String userName, String password, String token, long changeTime) {
        this._id = id;
        this.name = userName;
        this.password = password;
        this.token = token;
        this.changeTime = changeTime;
    }

    public User() {

    }

    public long getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this._id = id;
    }
}

