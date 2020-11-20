package com.cheroee.socketserver.server.bean;

public class PublisherRequest {
    private String token;
    private String pid;
    private String phoneId;
    private String type;
    private String userInfoId;
    private String avatar;
    private String userName;




    public String getPid() {
        return pid;
    }

    @Override
    public String toString() {
        return "PublisherRequest{" +
                "token='" + token + '\'' +
                ", pid='" + pid + '\'' +
                ", phoneId='" + phoneId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", uName='" + userName + '\'' +
                '}';
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(String userInfoId) {
        this.userInfoId = userInfoId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setYserName(String uName) {
        this.userName = uName;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return this.token;
    }
}
