package com.server;

public class WarningMessage {

    private String nick;
    private String latitude;
    private String longitude;
    private String dangertype;

    public WarningMessage(String nick, String latitude, String longitude, String dangertype) {
        this.nick = nick;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dangertype = dangertype;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDangertype() {
        return dangertype;
    }

    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }

}
