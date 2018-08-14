package it.unitn.lpmt.howsthere.Oggetti;

import java.io.Serializable;

/**
 * Created by fiapol on 27/05/18.
 */
public class Posizione implements Comparable, Serializable
{
    public Posizione(){}
    public Posizione(int ora, int minuto, double altezza, double azimuth){
        this.ora = ora;
        this.minuto = minuto;
        this.altezza = altezza;
        this.azimuth = azimuth;
    }

    public int compareTo(Object x) {

        if(x == null) System.out.println("dati sole/luna mancanti!");;

        if(!(x instanceof Posizione)) throw new ClassCastException();

        Posizione e = (Posizione) x;

        if(azimuth>e.azimuth){
            return 1;
        }else if(azimuth<e.azimuth){
            return -1;
        }else{
            return 0;
        }

    }
    public int ora;
    public int minuto;
    public double altezza;
    public double azimuth;
};
