package it.howsthere.howsthere2.objects;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String PREFS_NAME ="mapCameraState";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMapState(LatLng saveLn, int zoom) {
        SharedPreferences.Editor editor = mapStatePrefs.edit();

        editor.putLong(LATITUDE, Double.doubleToRawLongBits(saveLn.latitude));
        editor.putLong(LONGITUDE, Double.doubleToRawLongBits(saveLn.longitude));
        editor.putInt(ZOOM, zoom);
        editor.apply();
    }

    public CameraPosition getSavedCameraPosition() {
        double latitude = Double.longBitsToDouble(mapStatePrefs.getLong(LATITUDE, 0));
        double longitude = Double.longBitsToDouble(mapStatePrefs.getLong(LONGITUDE, 0));

        LatLng target = new LatLng(latitude, longitude);
        int zoom = mapStatePrefs.getInt(ZOOM, 5);

        CameraPosition position = null;

        if(latitude != 0 && longitude != 0){
            position = new CameraPosition(target, zoom, 0,0);
        }
        return position;
    }
}