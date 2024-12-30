package com.bobbyteam.howsthere2.ui.map;

import android.app.DatePickerDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bobbyteam.howsthere2.BuildConfig;
import com.bobbyteam.howsthere2.R;
import com.bobbyteam.howsthere2.databinding.FragmentMapBinding;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MapFragment extends Fragment implements OnMapReadyCallback, DatePickerDialog.OnDateSetListener {
    private FragmentMapBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap gMap = null;
    private Marker marker;
    private LatLng currentLocation = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MapViewModel homeViewModel =
                new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Create a new Places client instance.
        Places.initialize(getActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_search);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng pl = place.getLocation();
                animateLocation(pl, 15);
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
                animateLocation(new LatLng(location.getLatitude(), location.getLongitude()), 10);
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

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Quando ruoto il dispositivo o quando viene risvegliato salvo e recupero lo stato della mappa
        if(gMap != null) {
            /*MapStateManager mgr = new MapStateManager(getActivity());
            mgr.saveMapState(location, (int) gm.getCameraPosition().zoom);*/
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        gMap.setPadding(0, 180, 0,0);
        gMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void animateLocation(LatLng pos, int zoom){
        currentLocation = pos;
        gMap.clear();
        marker = gMap.addMarker(new MarkerOptions().position(pos).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray)));
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, zoom, 0,0)));
    }
}