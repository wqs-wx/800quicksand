/**
 * redis数据存储
 */
package com.cheroee.socketserver.server.bean;

import java.io.Serializable;
/**
 * 
 * @author Administrator
 *
 */
public class RedisMarathonUserOnline implements Serializable {
    /**
     * 序列化id
     */
    private static final long serialVersionUID = 4125096758372084309L;
    private String userInfoId;
    private String username;
    private String userRunCode;//跑者编号
    private Integer heartRate;//平均心率
	private Double  longitude ; //经度
	private Double  latitude ;	//纬度
	public String getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(String userInfoId) {
		this.userInfoId = userInfoId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(Integer heartRate) {
		this.heartRate = heartRate;
	}

	public String getUserRunCode() {
		return userRunCode;
	}

	public void setUserRunCode(String userRunCode) {
		this.userRunCode = userRunCode;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
}