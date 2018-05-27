package it.unitn.simob.howsthere.Oggetti;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Data;

/**
 * Created by matteo on 18/05/18.
 */

public class Panorama implements Serializable {
    /*FORMATO DATI MONTAGNE (7 colonne,e 361 righe di cui una di descrizione)
           -azimuth:               0-360 vale 0 a nord e cresce verso est
           -altitude:              inclinazione all' orizzonte già calcolata
           -distance (m):          distanza montagna
           -latitude:
           -longitude
           -elevation (m amsl):    altitudine montagna dal mare
       */
    public double[][] risultatiMontagne;
     /*FORMATO DATI SOLE/LUNA (288 righe di istanze della classe "Posizione") viene eseguito un calcolo ogni 5 minuti 24*(60/5)
            -ora:
            -minuto:
            -altezza (gradi):       inclinazione all' orizzonte
            -azimuth:               0-360 vale 0 a nord e cresce verso est
        */

    public Posizione[] risultatiSole;
    public Posizione[] risultatiLuna;

    // lista di posizioni dove il sole appare e scompare.
    public List<Posizione> albe;
    public List<Posizione> tramonti;

    public String ID;
    public String citta;
    public Date data;
    //public Bitmap grafico;

    public int ore_sole;
    public  double lat = 0;
    public  double lon = 0;

    public Panorama(){
        risultatiMontagne = new double[7][360];
        risultatiSole = new Posizione[288];
        risultatiLuna = new Posizione[288];
        albe = new ArrayList<Posizione>();
        tramonti = new ArrayList<Posizione>();
        data = new Date();
    }
    //alba (prima apparizione), tramonto (ultima scomparsa)
    public Posizione getAlba(){
        if (tramonti.size() != 0){
            return albe.get(0);
        }else{
            return null;
        }

    }
    public Posizione getTramonto(){
        return tramonti.get(tramonti.size()-1);
    }
}
