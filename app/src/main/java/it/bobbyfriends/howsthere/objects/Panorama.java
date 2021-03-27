package it.bobbyfriends.howsthere.objects;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Panorama implements Serializable {
    public String ID;
    public String city;
    public Date date;

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
    public Position[] risultatiSole;
    public Position[] risultatiLuna;

    // lista di posizioni dove il sole appare e scompare.
    public List<Position> albe;
    public List<Position> tramonti;

    // lista di posizioni dove la Luna appare e scompare. ANCORA DA IMPLEMENTARE
    public List<Position> albeLuna;
    public List<Position> tramontiLuna;

    public int minutiSole = 0;
    public int minutiLuna = 0;
    public int ore_sole;
    public  double lat = 0;
    public  double lon = 0;

    public double percentualeLuna = 0;
    public double faseLuna = 0;
    public ZonedDateTime albaNoMontagne;
    public ZonedDateTime tramontoNoMontagne;
    public ZonedDateTime albaLunaNoMontagne;
    public ZonedDateTime tramontoLunaNoMontagne;

    public Date prossimaLunaPiena; // non ancora implementato
    public Date ultimaLunaPiena;

    public Panorama(){
        risultatiMontagne = new double[7][360];
        nomiPeak = new ArrayList<Peak>();
        risultatiSole = new Position[288];
        risultatiLuna = new Position[864];
        //risultatiLunaGiornoPrima = new Posizione[288];
        //risultatiLunaGiornoDopo = new Posizione[288];
        albe = new ArrayList<Position>();
        tramonti = new ArrayList<Position>();
        albeLuna = new ArrayList<Position>();
        tramontiLuna = new ArrayList<Position>();
        date = new Date();
    }
    //alba (prima apparizione), tramonto (ultima scomparsa)
    public Position getAlba(){
        if (albe.size() != 0){
            return albe.get(0);
        }else{
            return null;
        }

    }
    public Position getTramonto(){
        if (tramonti.size() != 0) {
            return tramonti.get(tramonti.size() - 1);
        }else{
            return null;
        }
    }

    public Position getAlbaLuna(){
        if (albeLuna.size() != 0){
            return albeLuna.get(0);
        }else{
            return null;
        }

    }

    public Position getTramontoLuna(){
        if (tramontiLuna.size() != 0) {
            return tramontiLuna.get(tramontiLuna.size() - 1);
        }else{
            return null;
        }
    }

    public void setCity(String city){
        this.city = city;
    }

    public void setDate(Date sel_date){
        this.date = sel_date;
    }

    public void setPosition(LatLng pos){
        this.lat = pos.latitude;
        this.lon = pos.longitude;
    }
}
