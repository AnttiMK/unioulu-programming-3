package com.server;

public class WarningMessage {

    private String nickname;
    private double latitude;
    private double longitude;
    private String dangertype;

    public WarningMessage(String nickname, double latitude, double longitude, String dangertype) {
        this.nickname = nickname;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDangertype() {
        return dangertype;
    }

    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }

}
