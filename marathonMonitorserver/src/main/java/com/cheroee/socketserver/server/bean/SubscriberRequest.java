package com.cheroee.socketserver.server.bean;

public class SubscriberRequest {
    private  String doctorUserId;//医生userInfoId
    private String userInfoId; //患者userInfoId


    public String getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(String userInfoId) {
        this.userInfoId = userInfoId;
    }

    public String getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(String doctorUserId) {
        this.doctorUserId = doctorUserId;
    }
}
