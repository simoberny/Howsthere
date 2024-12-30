package com.bobbyteam.howsthere2.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PanoramaStorage {
    private Activity context = null;
    private List<Panorama> panoramas = new ArrayList<Panorama>();
    private SharedPreferences pref = null;

    public PanoramaStorage(Activity context) {
        this.context = context;
    }

    public void init() {
        pref = context.getPreferences(Context.MODE_PRIVATE);
    }

    public Panorama getPanoramaByID(String id_) {
        loadPref();

        for (int i = 0; i < panoramas.size(); i++) {
            if (panoramas.get(i).id.equals(id_)) {
                return panoramas.get(i);
            }
        }

        return null;
    }

    public void addPanorama(Panorama p) {
        loadPref();

        if(!panoramaExist(p)){
            panoramas.add(0, p);
        }

        saveToPref();
    }

    public boolean panoramaExist(Panorama p){
        for (Panorama mp : panoramas) {
            if (mp.lat == p.lat && mp.lon == p.lon && mp.date == p.date) {
                return true;
            }
        }

        return false;
    }

    public void loadPref(){
        if(panoramas.isEmpty()) {
            Gson gson = new Gson();
            String json = pref.getString("history", "");
            panoramas = gson.fromJson(json, new TypeToken<List<Panorama>>() {}.getType());

            if(panoramas == null)
                panoramas = new ArrayList<Panorama>();
        }
    }

    private void saveToPref(){
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(panoramas);
        prefsEditor.putString("history", json);
        prefsEditor.apply();
    }

    public void deleteAll() {
        loadPref();

        panoramas.clear();

        saveToPref();
    }

    public void deleteById(String id_){
        loadPref();

        for (int i = 0; i < panoramas.size(); i++) {
            if (panoramas.get(i).id.equals(id_)) {
                panoramas.remove(i);
                saveToPref();
                break;
            }
        }
    }

    public List<Panorama> getAllPanorama() {
        loadPref();
        return panoramas;
    }

    public void setContext(Activity in){
        this.context = in;
    }
}
