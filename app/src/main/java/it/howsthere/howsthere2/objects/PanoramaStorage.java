package it.howsthere.howsthere2.objects;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Objects;

public class PanoramaStorage {
    private static PanoramaStorage instance;
    private List<Panorama> panoramas = new ArrayList<>();
    private SharedPreferences pref;
    private final Gson gson = new Gson();
    private static final Type HISTORY_TYPE = new TypeToken<ArrayList<Panorama>>() { }.getType();

    private PanoramaStorage() {}

    public static synchronized PanoramaStorage getInstance() {
        if (instance == null) {
            instance = new PanoramaStorage();
        }

        return instance;
    }

    public synchronized void init(Activity context_) {
        pref = context_.getPreferences(Context.MODE_PRIVATE);
    }

    public synchronized Panorama getPanoramaByID(String id_) {
        loadPref();

        int index = indexOf(id_);
        return index < 0 ? null : panoramas.get(index);
    }

    public synchronized void addPanorama(Panorama p) {
        loadPref();

        int pos = indexOf(p.id);

        if(pos < 0){
            panoramas.add(0, p);
        } else {
            panoramas.set(pos, p);
        }

        saveToPref();
    }

    private int indexOf(String id){
        for (int i = 0; i < panoramas.size(); i++) {
            if (Objects.equals(panoramas.get(i).id, id)) {
                return i;
            }
        }

        return -1;
    }

    private void loadPref(){
        if(pref != null) {
            String json = pref.getString("history", "");

            panoramas = gson.fromJson(json, HISTORY_TYPE);

            if(panoramas == null)
                panoramas = new ArrayList<>();
        }
    }

    private void saveToPref(){
        SharedPreferences.Editor prefsEditor = pref.edit();
        String json = gson.toJson(panoramas, HISTORY_TYPE);

        prefsEditor.putString("history", json);
        prefsEditor.apply();
    }

    public synchronized void deleteAll() {
        loadPref();
        panoramas.clear();
        saveToPref();
    }

    /** Persist a multiple selection once, keeping reads and writes atomic. */
    public synchronized void deleteByIds(Collection<String> ids) {
        loadPref();
        if (panoramas.removeIf(panorama -> ids.contains(panorama.id))) {
            saveToPref();
        }
    }

    public synchronized List<Panorama> getAllPanorama() {
        loadPref();
        return new ArrayList<>(panoramas);
    }
}
