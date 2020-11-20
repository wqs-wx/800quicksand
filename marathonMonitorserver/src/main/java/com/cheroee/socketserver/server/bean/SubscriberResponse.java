package com.cheroee.socketserver.server.bean;

public class SubscriberResponse {
    private String status;
    private String msg;
    private String type;
    private String uid;
    private String avatar;
    private String name;

    //额外的一些用户信息。
    private String info;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getuName() {
        return name;
    }

    public void setuName(String uName) {
        this.name = uName;
    }

}
