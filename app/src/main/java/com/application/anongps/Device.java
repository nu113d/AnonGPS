package com.application.anongps;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

//holds the encrypted data
public class Device implements Serializable {
    private String uuid, lon, lat, speed, alt, time;

    public Device(String uuid) {
        this.uuid = uuid;
    }
    public Device() {

    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    @Exclude
    public String getUuid() {
        return uuid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
