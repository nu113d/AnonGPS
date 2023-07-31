package com.application.anongps;

import java.io.Serializable;

public class Device implements Serializable {
    private float lon, lat, speed, alt;
    private String uuid;
    private int deleteInterval, updateInterval;

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAlt() {
        return alt;
    }

    public void setAlt(float alt) {
        this.alt = alt;
    }

    public int getDeleteInterval() {
        return deleteInterval;
    }

    public void setDeleteInterval(int deleteInterval) {
        this.deleteInterval = deleteInterval;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
