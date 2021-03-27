package it.bobbyfriends.howsthere.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PanoramaStorage {
    public static PanoramaStorage persistent_storage;
    public static Activity context = null;
    public List<Panorama> panorami = new ArrayList();
    private SharedPreferences pref = null;

    public void init(){
        pref = context.getPreferences(Context.MODE_PRIVATE);
    }

    public Panorama getPanoramabyID(String id) {
        loadPref();

        for (int i = 0; i < panorami.size(); i++) {
            if (panorami.get(i).ID.equals(id)) {
                return panorami.get(i);
            }
        }

        return null;
    }

    public void addPanorama(Panorama p) {
        loadPref();
        panorami.add(0, p);
        saveToPref();
    }

    public void loadPref(){
        if(panorami.size() == 0) {
            Gson gson = new Gson();
            String json = pref.getString("history", "");
            panorami = gson.fromJson(json, new TypeToken<List<Panorama>>() {}.getType());
        }
    }

    private void saveToPref(){
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(panorami);
        prefsEditor.putString("history", json);
        prefsEditor.commit();
    }

    public void delete(int pos) {

    }

    public void delete_all() {

    }

    public void delete_by_id(String id){

    }

    public List<Panorama> getAllPanorama() {
        return null;
    }

    public void setContext(Activity in){
        this.context = in;
    }
}
