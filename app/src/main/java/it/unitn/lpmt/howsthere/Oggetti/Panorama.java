package it.unitn.lpmt.howsthere.Oggetti;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
     public List<Peak> nomiPeak;
    public Posizione[] risultatiSole;
    public Posizione[] risultatiLuna;
    //public Posizione[] risultatiLunaGiornoPrima;
    //public Posizione[] risultatiLunaGiornoDopo;

    // lista di posizioni dove il sole appare e scompare.
    public List<Posizione> albe;
    public List<Posizione> tramonti;

    // lista di posizioni dove la Luna appare e scompare. ANCORA DA IMPLEMENTARE
    public List<Posizione> albeLuna;
    public List<Posizione> tramontiLuna;

    public String ID;
    public String citta;
    public Date data;
    //public Bitmap grafico;
    public int minutiSole = 0;
    public int minutiLuna = 0;
    public int ore_sole;
    public  double lat = 0;
    public  double lon = 0;
    public Date prossimaLunaPiena; //non ancora implementato
    public Date ultimaLunaPiena;
    public double percentualeLuna = 0;
    public  double faseLuna = 0;
    public Date albaNoMontagne;
    public Date tramontoNoMontagne;
    public Date albaLunaNoMontagne;
    public Date tramontoLunaNoMontagne;
    //public

    public Panorama(){
        risultatiMontagne = new double[7][360];
        nomiPeak = new ArrayList<Peak>();
        risultatiSole = new Posizione[288];
        risultatiLuna = new Posizione[864];
        //risultatiLunaGiornoPrima = new Posizione[288];
        //risultatiLunaGiornoDopo = new Posizione[288];
        albe = new ArrayList<Posizione>();
        tramonti = new ArrayList<Posizione>();
        albeLuna = new ArrayList<Posizione>();
        tramontiLuna = new ArrayList<Posizione>();
        data = new Date();
    }
    //alba (prima apparizione), tramonto (ultima scomparsa)
    public Posizione getAlba(){
        if (albe.size() != 0){
            return albe.get(0);
        }else{
            return null;
        }

    }
    public Posizione getTramonto(){
        if (tramonti.size() != 0) {
            return tramonti.get(tramonti.size() - 1);
        }else{
            return null;
        }
    }

    public Posizione getAlbaLuna(){
        if (albeLuna.size() != 0){
            return albeLuna.get(0);
        }else{
            return null;
        }

    }
    public Posizione getTramontoLuna(){
        if (tramontiLuna.size() != 0) {
            return tramontiLuna.get(tramontiLuna.size() - 1);
        }else{
            return null;
        }
    }
}
