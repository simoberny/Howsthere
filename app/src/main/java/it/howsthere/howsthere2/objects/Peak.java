package it.howsthere.howsthere2.objects;

import java.io.Serializable;

public class Peak implements Serializable {
    private String name;
    private double height;
    private double azimuth;

    public Peak(){ }
    public Peak(String name_, Double azi_, Double height_){
        name = name_;
        azimuth = azi_;
        height = height_;
    }

    public String getName() {
        return name;
    }

    public void setName(String name_) {
        name = name_;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height_) {
        height = height_;
    }
}