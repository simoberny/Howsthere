package it.howsthere.howsthere2.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PanoramaStorage {
    private static PanoramaStorage instance;
    private List<Panorama> panoramas = new ArrayList<>();
    private SharedPreferences pref = null;

    private PanoramaStorage() {}

    public static synchronized PanoramaStorage getInstance() {
        if (instance == null) {
            instance = new PanoramaStorage();
        }

        return instance;
    }

    public void init(Activity context_) {
        pref = context_.getPreferences(Context.MODE_PRIVATE);
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

        int pos = panoramaExist(p);

        if(pos < 0){
            panoramas.add(0, p);
        } else {
            panoramas.set(pos, p);
        }

        saveToPref();
    }

    public int panoramaExist(Panorama p){
        for (int i = 0; i < panoramas.size(); i++) {
            if (Objects.equals(panoramas.get(i).id, p.id)) {
                return i;
            }
        }

        return -1;
    }

    public void loadPref(){
        if(pref != null) {
            Gson gson = new Gson();
            String json = pref.getString("history", "");

            panoramas = gson.fromJson(json, new TypeToken<ArrayList<Panorama>>() {}.getType());

            if(panoramas == null)
                panoramas = new ArrayList<>();
        }
    }

    private void saveToPref(){
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();

        Type type = new TypeToken<ArrayList<Panorama>>() {}.getType();
        String json = gson.toJson(panoramas, type);

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
}
