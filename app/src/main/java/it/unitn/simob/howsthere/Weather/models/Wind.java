package it.unitn.simob.howsthere.Weather.models;

import java.io.Serializable;

public class Wind implements Serializable {
    private String speed;

    private String deg;

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDeg() {
        return deg;
    }

    public void setDeg(String deg) {
        this.deg = deg;
    }

    @Override
    public String toString() {
        return "ClassPojo [speed = " + speed + ", deg = " + deg + "]";
    }
}
