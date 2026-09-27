package it.howsthere.howsthere2.objects;

import java.io.Serializable;

public class Position implements Comparable<Position>, Serializable {
    public int hour;
    public int minutes;
    public double height;
    public double azimuth;

    public Position() {}
    public Position(int hour_, int minutes_, double height_, double azimuth_){
        hour = hour_;
        minutes = minutes_;
        height = height_;
        azimuth = azimuth_;
    }

    @Override
    public int compareTo(Position other) {
        return Double.compare(azimuth, other.azimuth);
    }
}
