package it.howsthere.howsthere2.ui.map;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.databinding.FragmentMapBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import it.howsthere.howsthere2.BuildConfig;
import it.howsthere.howsthere2.Hwt;
import it.howsthere.howsthere2.objects.MapStateManager;
import it.howsthere.howsthere2.objects.Utils;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentMapBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap gMap = null;
    private Marker marker;
    private String city = null;
    private LatLng currentLocation = null;
    private MapStateManager mapManager;
    private BottomSheetDialog dialog;
    private View dialogView;
    private Date selectedDate = Calendar.getInstance().getTime();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mapManager = new MapStateManager(requireActivity());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(requireActivity()));

        // Position dialog
        dialog = new BottomSheetDialog(requireActivity());
        dialogView = requireActivity().getLayoutInflater().inflate(R.layout.map_bottom_sheet, null);
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

        // Event for date-picker on dialog
        Button date = dialogView.findViewById(R.id.action_date);
        date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();

                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year_,
                                                  int monthOfYear_, int dayOfMonth_) {
                                Calendar selected = Calendar.getInstance();
                                selected.set(Calendar.YEAR, year_);
                                selected.set(Calendar.MONTH, monthOfYear_);
                                selected.set(Calendar.DAY_OF_MONTH, dayOfMonth_);

                                selectedDate = Date.from(selected.toInstant());

                                String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selected.getTime());

                                TextView date = (TextView) dialogView.findViewById(R.id.selected_date);
                                date.setText(currentDate);
                            }
                        },
                        year, month, day);
                datePickerDialog.show();

                if (marker != null) marker.hideInfoWindow();
            }
        });

        // Proceed with the calculation event
        Button send = dialogView.findViewById(R.id.action_next);
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                launchProcessing();
            }
        });

        // Create a new Places client instance.
        Places.initialize(requireActivity().getApplicationContext(), BuildConfig.MAPS_API_KEY);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_search);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION));
        }

        // Set up a PlaceSelectionListener to handle the response.
        if (autocompleteFragment != null) {
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
        }

        /* Positioning button */
        FloatingActionButton position = root.findViewById(R.id.gps_position);
        position.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getLocation(root);
            }
        });

        /* Following button */
        ExtendedFloatingActionButton next = root.findViewById(R.id.next_action);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(marker != null)
                    positionAndDialog(marker);
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

    private void launchProcessing() {
        Hwt request = new Hwt(requireActivity());
        request.initializePanorama(currentLocation, city, selectedDate);
        request.requestData();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        gMap.setPadding(0, 180, 0,0);
        gMap.getUiSettings().setMapToolbarEnabled(false);

        CameraPosition position = mapManager.getSavedCameraPosition();

        if(position != null){
            currentLocation = position.target;
            animateLocation(position.target, (int) position.zoom);
        }else {
            // First start position to Trento
            animateLocation(new LatLng(46.080454, 11.0791564), 5);
        }

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(@NonNull LatLng point) {
                gMap.clear();
                currentLocation = point;
                marker = gMap.addMarker(new MarkerOptions().position(point).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray)));

                positionAndDialog(marker);
            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                positionAndDialog(marker);
                return false;
            }
        });

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) { }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                currentLocation = marker.getPosition();
                positionAndDialog(marker);
            }
        });
    }

    public void positionAndDialog(Marker marker){
        TextView textLocation = dialogView.findViewById(R.id.current_location);
        LinearLayout actionContainer = dialogView.findViewById(R.id.action_container);
        Button buttonNext = dialogView.findViewById(R.id.action_next);
        TextView textNotSupported = dialogView.findViewById(R.id.not_supported);

        city = Utils.getCity(requireActivity(), currentLocation.latitude, currentLocation.longitude);

        textLocation.setText(city);
        marker.setTitle(city);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
        marker.showInfoWindow();

        // Temporary limit based on latitude
        if(currentLocation.latitude < 17){
            actionContainer.setVisibility(View.GONE);
            textNotSupported.setVisibility(View.VISIBLE);
        }else{
            actionContainer.setVisibility(View.VISIBLE);
            textNotSupported.setVisibility(View.GONE);
        }

        dialog.show();
    }

    private void animateLocation(LatLng pos, int zoom){
        currentLocation = pos;
        gMap.clear();
        marker = gMap.addMarker(new MarkerOptions().position(pos).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gray)));
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, zoom, 0,0)));
    }

    public void getLocation(View view) {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                animateLocation(new LatLng(location.getLatitude(), location.getLongitude()), 15);
                            }
                        }
                    });

            LocationRequest mLocationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, // Priorità
                    500 // Intervallo in millisecondi
            )
                    .setMaxUpdates(1) // Numero massimo di aggiornamenti
                    .setDurationMillis(500) // Durata dell'operazione
                    .build();
        } else {
            // Se non ci sono i permessi li richiedo all'utente
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        // Quando ruoto il dispositivo o quando viene risvegliato salvo e recupero lo stato della mappa
        if(gMap != null) {
            mapManager.saveMapState(currentLocation, (int) gMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}