package it.unitn.simob.howsthere.Fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.unitn.simob.howsthere.Data;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback, DatePickerDialog.OnDateSetListener {
    private GoogleMap gm = null;
    private LatLng ln = null;
    private int zoomLevel = 0;
    public static MapsFragment contesto;
    private Date dataSelezionata = Calendar.getInstance().getTime();
    BottomSheetDialog dialog;
    View dialogView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contesto = this;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Quando ruoto il dispositivo o quando viene risvegliato salvo e recupero lo stato della mappa
        if(gm != null) {
            MapStateManager mgr = new MapStateManager(getActivity());
            mgr.saveMapState(gm);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        PanoramiStorage ps = new PanoramiStorage();

        //parte di test che popola
        Panorama p = new Panorama();
        p.città = "trento";
        p.data = new Date();
        ps.addPanorama(p);
        Panorama p1 = new Panorama();
        p1.città = "trento";
        p1.data = new Date();
        ps.addPanorama(p1);

        //end test

        final View rootview = inflater.inflate(R.layout.fragment_maps, container, false);
       //intanto creo il dialog che viene su quando clicco sulla mappa, poi lo aprirò
       dialog = new BottomSheetDialog(getActivity());
       dialogView = getActivity().getLayoutInflater().inflate(R.layout.bottomdialog, null);
       dialog.setContentView(dialogView);

       FloatingActionButton position = (FloatingActionButton) rootview.findViewById(R.id.position);
       position.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               getLocation(rootview);
           }
       });

       Button date = (Button) dialogView.findViewById(R.id.dataselect);
       date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getActivity().getSupportFragmentManager(), "Seleziona data");
            }
       });

       Button send = (Button) dialogView.findViewById(R.id.send);
       send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), Data.class);
                i.putExtra("lat", ln.latitude);
                i.putExtra("long", ln.longitude);
                i.putExtra("data", dataSelezionata.getTime());
                startActivity(i);
            }
       });

       //Carico il cerca di google
        SupportPlaceAutocompleteFragment placeAutoComplete = (SupportPlaceAutocompleteFragment)  this.getChildFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng pl = place.getLatLng();
                goToLoc(pl, 15);
            }

            @Override
            public void onError(Status status) {
                Log.d("MainActivity", "An error occurred: " + status);
            }
        });

        //Mappa di google
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       return rootview;
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    public void getLocation(View view) {
        LocationListener ll = new mylocationlistener();

        boolean gps_enabled = false;
        boolean network_enabled = false;
        Location net_loc = null, gps_loc = null, finalLoc = null;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            gm.setMyLocationEnabled(true);
            gm.getUiSettings().setMyLocationButtonEnabled(true);

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Snackbar mySnackbar = Snackbar.make(getActivity().findViewById(R.id.map_container), "Il GPS è disattivato", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            } else {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (gps_enabled) {
                    //Servizio che richiede la posizione con GPS se disponibile ogni secondo
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, ll);
                }
                if (network_enabled)
                    net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                        finalLoc = net_loc;
                    else
                        finalLoc = gps_loc;
                } else {
                    if (gps_loc != null) {
                        finalLoc = gps_loc;
                    } else if (net_loc != null) {
                        Toast.makeText(getActivity(), "No GPS", Toast.LENGTH_SHORT).show();
                        finalLoc = net_loc;
                    }
                }
                if (finalLoc != null) {
                    goToLoc(new LatLng(finalLoc.getLatitude(), finalLoc.getLongitude()), 10);
                }
            }
        } else {
            //Se non ci sono i permessi li richiedo all'utente
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    //Listener per la posizione GPS
    private class mylocationlistener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                goToLoc(new LatLng(location.getLatitude(), location.getLongitude()), 10);
            }
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    /**
     * Funzione da data la copia di posizionamento e lo zoom della mappa muove la mappa in quella direzione
     * Può essere chiamata ad esempio quando si chiede la propria posizione per posizionare la mappa
     * @param pos
     * @param zoom
     */
    private void goToLoc(LatLng pos, int zoom){
        ln = new LatLng(pos.latitude, pos.longitude);
        gm.clear();
        gm.addMarker(new MarkerOptions().position(ln));
        gm.moveCamera(CameraUpdateFactory.newLatLng(ln));
        gm.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    /**
     * Mini classe per la gestione del selezionatore della data
     */
    public static class DatePickerFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(contesto.getContext(), (DatePickerDialog.OnDateSetListener) contesto, year, month, day);
        }
    }

    /**
     * Gestisco l'evento della data selezionata e la salvo anche nella variabile globale
     * @param datePicker
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        dataSelezionata = c.getTime();

        String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        TextView date = (TextView) dialogView.findViewById(R.id.dateselected);
        date.setText(currentDate);
    }

    /**
     * Funzione principale per gestire la mappa che viene caricata quando la mappa è pronta
     * Qui viene gestito il click
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gm = googleMap;
        gm.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        gm.setPadding(0, 180, 0,0);

        MapStateManager mgr = new MapStateManager(getActivity());
        final CameraPosition position = mgr.getSavedCameraPosition();

        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            gm.moveCamera(update);
            goToLoc(gm.getCameraPosition().target, (int) gm.getCameraPosition().zoom);
        }

        gm.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                gm.clear();
                gm.addMarker(new MarkerOptions().position(point).draggable(true));
                positionAndDialog(point.latitude, point.longitude);
            }
        });

        gm.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) { }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                positionAndDialog(marker.getPosition().latitude, marker.getPosition().longitude);
            }
        });
    }

    public void positionAndDialog(Double lat, Double lon){
        ln = new LatLng(lat, lon);
        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(ln.latitude, ln.longitude, 1);
            if (addresses.size() > 0) {
                Toast.makeText(getActivity(), "Città: " + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                System.out.println(addresses.get(0).getLocality());
            }else{
                Log.d("Problema address", "Problema");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.show();
    }

    /**
     * Classe per gestire il salvataggio dello stato della mappa tra una sessione e l'altra o quando
     * il telefono cambia il suo stato
     */
    public class MapStateManager {
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String ZOOM = "zoom";
        private static final String BEARING = "bearing";
        private static final String TILT = "tilt";
        private static final String MAPTYPE = "MAPTYPE";

        private static final String PREFS_NAME ="mapCameraState";

        private SharedPreferences mapStatePrefs;

        public MapStateManager(Context context) {
            mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        public void saveMapState(GoogleMap mapMie) {
            SharedPreferences.Editor editor = mapStatePrefs.edit();
            CameraPosition position = mapMie.getCameraPosition();

            editor.putFloat(LATITUDE, (float) position.target.latitude);
            editor.putFloat(LONGITUDE, (float) position.target.longitude);
            editor.putFloat(ZOOM, position.zoom);
            editor.putFloat(TILT, position.tilt);
            editor.putFloat(BEARING, position.bearing);
            editor.putInt(MAPTYPE, mapMie.getMapType());
            editor.commit();
        }

        public CameraPosition getSavedCameraPosition() {
            double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
            if (latitude == 0) {
                return null;
            }
            double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
            LatLng target = new LatLng(latitude, longitude);

            float zoom = mapStatePrefs.getFloat(ZOOM, 0);
            float bearing = mapStatePrefs.getFloat(BEARING, 0);
            float tilt = mapStatePrefs.getFloat(TILT, 0);

            CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
            return position;
        }

        public int getSavedMapType() {
            return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
        }
    }
}
