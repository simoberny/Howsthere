package com.bobbyteam.howsthere2.objects;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Panorama implements Serializable {
    public String id;
    public String city;
    public Date date;
    public String tz;

    // Formato dati montagne (7 colonne, e 361 righe di cui una di descrizione)
    //   - Azimuth:               0 - 360 vale 0 a nord e cresce verso est
    //   - Altitude:              Inclinazione all' orizzonte già calcolata
    //   - Distance (m):          Distanza montagna
    //   - Latitude:              Posizione
    //   - Longitude              Posizione
    //   - Elevation (m amsl):    Altitudine montagna dal mare
    public double[][] peaks_data;

    public List<Peak> peaks_name;

    // Formati dati sole/luna (288 righe di istanze della classe "Position") viene eseguito un calcolo ogni 5 minuti 24*(60/5)
    //   - Ora:
    //   - Minuto:
    //   - Altezza (gradi):       inclinazione all' orizzonte
    //   - Azimuth:               0-360 vale 0 a nord e cresce verso est
    public Position[] sun_data;
    public Position[] moon_data;

    // Lista posizioni dove sole compare/scompare
    public List<Position> sunrise;
    public List<Position> sunset;

    // Lista posizioni dove luna compare/scompare
    // TODO
    public List<Position> moon_sunsire;
    public List<Position> moon_sunset;

    public int sun_minutes = 0;
    public int sun_hours = 0;
    public int moon_minutes = 0;

    public double lat = 0;
    public double lon = 0;

    public double moon_perc = 0;
    public double moon_phase = 0;

    public Date next_fullmoon; // TODO
    public Date last_fullmoon; //TODO

    public Panorama(){
        peaks_data = new double[7][360];
        peaks_name = new ArrayList<Peak>();

        sun_data = new Position[288];
        moon_data = new Position[864];

        sunrise = new ArrayList<Position>();
        sunset = new ArrayList<Position>();
        moon_sunsire = new ArrayList<Position>();
        moon_sunset = new ArrayList<Position>();

        date = Calendar.getInstance().getTime();
    }

    // Alba (prima apparizione)
    public Position getFirstSunrise(){
        if (!sunrise.isEmpty()){
            return sunrise.get(0);
        }

        return null;
    }

    // Tramonto (ultima scomparsa)
    public Position getLastSunset(){
        if (!sunset.isEmpty()) {
            return sunset.get(sunset.size() - 1);
        }

        return null;
    }

    public Position getFirstMoonSunrise(){
        if (!moon_sunsire.isEmpty()){
            return moon_sunsire.get(0);
        }

        return null;
    }

    public Position getLastMoonSunset(){
        if (!moon_sunset.isEmpty()) {
            return moon_sunset.get(moon_sunset.size() - 1);
        }

        return null;
    }

    public void setCity(String city_){
        city = city_;
    }

    public void setDate(Date date_){
        date = date_;
    }

    public void setPosition(LatLng pos_){
        lat = pos_.latitude;
        lon = pos_.longitude;
    }
}
