package com.bobbyteam.howsthere2.objects;

import java.io.Serializable;

public class Position implements Comparable, Serializable {
    public int hour;
    public int minutes;
    public double height;
    public double azimuth;

    public Position(){}
    public Position(int hour_, int minutes_, double height_, double azimuth_){
        hour = hour_;
        minutes = minutes_;
        height = height_;
        azimuth = azimuth_;
    }

    public int compareTo(Object in_) {
        if(in_ == null) System.out.println("Missing sun and moon data");;
        if(!(in_ instanceof Position)) throw new ClassCastException();

        Position instance_ = (Position) in_;

        return Double.compare(azimuth, instance_.azimuth);
    }
}
