package it.bobbyfriends.howsthere.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.bobbyfriends.howsthere.BuildConfig;
//import it.bobbyfriends.howsthere.Data;
import it.bobbyfriends.howsthere.Hwt;
import it.bobbyfriends.howsthere.MainActivity;
import it.bobbyfriends.howsthere.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback, DatePickerDialog.OnDateSetListener{
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest mLocationRequest;

    private BottomSheetDialog dialog;
    private View dialogView;
    private GoogleMap gm = null;
    private Marker marker;

    private LatLng location = null;
    private String city = null;
    private Date current_date = Calendar.getInstance().getTime();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getWindow().setStatusBarColor(Color.TRANSPARENT);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        /* Bottom dialog */
        dialog = new BottomSheetDialog(getActivity());
        dialogView = getActivity().getLayoutInflater().inflate(R.layout.position_dialog, null);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray));
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray));
            }
        });

        Button date = dialogView.findViewById(R.id.action_date);
        date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.setTargetFragment(HomeFragment.this, 0);
                datePicker.show(getParentFragmentManager(), "Seleziona data");
                if (marker != null) marker.hideInfoWindow();
            }
        });

        Button send = dialogView.findViewById(R.id.action_next);
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                process_HWT();
            }
        });
        /* ************** */

        /* Positioning button */
        FloatingActionButton position = root.findViewById(R.id.position);
        position.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getLocation(root);
            }
        });

        /* Following button */
        FloatingActionButton next = root.findViewById(R.id.go_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(marker != null) positionAndDialog(marker);
            }
        });

        // Create a new Places client instance.
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_search);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng pl = place.getLatLng();
                goToLoc(pl, 15);
            }

            @Override
            public void onError(@NotNull Status status) {
                Log.i("ERROR", "An error occurred: " + status);
            }
        });

        /* GPS Positioning callback */
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    goToLoc(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                }
            }
        };

        //Mappa di google
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Quando ruoto il dispositivo o quando viene risvegliato salvo e recupero lo stato della mappa
        if(gm != null) {
            MapStateManager mgr = new MapStateManager(getActivity());
            mgr.saveMapState(location, (int) gm.getCameraPosition().zoom);
        }
    }

    public void process_HWT(){
        Hwt hwt_data = new Hwt(getActivity());
        hwt_data.initializePanorama(location, city, current_date);
        hwt_data.requestData();
    }

    public void getLocation(View view) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gm.setMyLocationEnabled(true);
            gm.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                goToLoc(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                            }
                        }
                    });

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setExpirationDuration(500);
            mLocationRequest.setNumUpdates(1);
        } else {
            // Se non ci sono i permessi li richiedo all'utente
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void goToLoc(LatLng pos, int zoom){
        location = pos;
        gm.clear();
        marker = gm.addMarker(new MarkerOptions().position(pos).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray)));
        gm.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, zoom, 0,0)));
    }

    /**
     * Funzione principale per gestire la mappa che viene caricata quando la mappa è pronta
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gm = googleMap;

        gm.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        gm.setPadding(0, 180, 0,0);
        gm.getUiSettings().setMapToolbarEnabled(false);

        MapStateManager mgr = new MapStateManager(getActivity());
        final CameraPosition position = mgr.getSavedCameraPosition();

        if(position != null){
            location = position.target;
            goToLoc(position.target, (int) position.zoom);
        }else {
            goToLoc(new LatLng(45.627245, 9.316333), 5); //Arcore
        }

        gm.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                gm.clear();
                location = point;
                marker = gm.addMarker(new MarkerOptions().position(point).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray)));
                positionAndDialog(marker);
            }
        });

        gm.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                positionAndDialog(marker);
                return false;
            }
        });

        gm.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) { }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                location = marker.getPosition();
                positionAndDialog(marker);
            }
        });
    }

    public void positionAndDialog(Marker marker){
        TextView tx = dialogView.findViewById(R.id.current_location);
        LinearLayout date_container = dialogView.findViewById(R.id.main_container);
        Button send = dialogView.findViewById(R.id.action_next);
        TextView not_supported = dialogView.findViewById(R.id.not_supported);

        Geocoder gcd = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                        city = adr.getLocality() + ", " + adr.getCountryName();
                    }else{
                        city = adr.getAdminArea() + ", " + adr.getCountryName();
                    }
                }
            }

            if(city == null) city = "Not available!";
            tx.setText(getResources().getString(R.string.position) + ": " + city);
        } catch (IOException e) {
            e.printStackTrace();
        }

        marker.setTitle(city);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
        marker.showInfoWindow();

        if(location.latitude < 17){
            send.setVisibility(View.GONE);
            not_supported.setVisibility(View.VISIBLE);
            date_container.setVisibility(View.GONE);
        }else{
            send.setVisibility(View.VISIBLE);
            not_supported.setVisibility(View.GONE);
            date_container.setVisibility(View.VISIBLE);
        }

        dialog.show();
    }

    /**
     * Classe per gestire il salvataggio dello stato della mappa tra una sessione e l'altra o quando il telefono cambia il suo stato
     */
    public class MapStateManager {
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String ZOOM = "zoom";
        private static final String PREFS_NAME ="mapCameraState";

        private SharedPreferences mapStatePrefs;

        private MapStateManager(Context context) {
            mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        private void saveMapState(LatLng saveLn, int zoom) {
            SharedPreferences.Editor editor = mapStatePrefs.edit();

            editor.putLong(LATITUDE, Double.doubleToRawLongBits(saveLn.latitude));
            editor.putLong(LONGITUDE, Double.doubleToRawLongBits(saveLn.longitude));
            editor.putInt(ZOOM, zoom);
            editor.commit();
        }

        private CameraPosition getSavedCameraPosition() {
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
            return new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getTargetFragment(), year, month, day);
        }
    }

    /**
     * Gestisco l'evento della data selezionata e la salvo anche nella variabile globale
     */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        current_date = c.getTime();

        String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());

        TextView date = (TextView) dialogView.findViewById(R.id.selected_date);
        date.setText(currentDate);
    }
}