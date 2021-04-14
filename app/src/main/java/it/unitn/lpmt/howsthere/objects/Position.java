package it.unitn.lpmt.howsthere.objects;

import java.io.Serializable;

public class Position implements Comparable, Serializable {
    public int ora;
    public int minuto;
    public double altezza;
    public double azimuth;

    public Position(){}
    public Position(int ora, int minuto, double altezza, double azimuth){
        this.ora = ora;
        this.minuto = minuto;
        this.altezza = altezza;
        this.azimuth = azimuth;
    }

    public int compareTo(Object x) {
        if(x == null) System.out.println("Missing sun and moon data");;

        if(!(x instanceof Position)) throw new ClassCastException();

        Position e = (Position) x;

        if (azimuth > e.azimuth) {
            return 1;
        } else if(azimuth < e.azimuth) {
            return -1;
        } else {
            return 0;
        }
    }
}
