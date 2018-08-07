package it.unitn.simob.howsthere.Fragment;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.RisultatiActivity;

import static android.content.Context.SENSOR_SERVICE;

public class BussolaFragment extends Fragment implements SensorEventListener {
    ImageView compassAlba;
    ImageView compassTramonto;
    ImageView nord_compass;
    float currentDegree = 0f;
    SensorManager mSensorManager;
    Double lat, lon;
    Location currentLoc = null;

    private GoogleMap mapTramonto = null;
    private GoogleMap mapAlba = null;

    TextView gradi_alba, gradi_tramonto, nord;

    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    public BussolaFragment() {
    }

    public static BussolaFragment newInstance() {
        BussolaFragment bs = new BussolaFragment();
        return bs;
    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        Panorama p = ((RisultatiActivity) getActivity()).p;

        lat = p.lat;
        lon = p.lon;

        angoloDaSotrarreTramonto = (float) Math.floor((float) p.getAlba().azimuth * 100) / 100;
        angoloDaSotrarreAlba = (float) Math.floor((float) p.getTramonto().azimuth * 100) / 100;

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bussola, container, false);
        //((RisultatiActivity)getActivity()).getSupportActionBar().setTitle("Alba e tramonto sole");
        compassAlba = (ImageView) view.findViewById(R.id.compassAlba);
        compassTramonto = (ImageView) view.findViewById(R.id.compassTramonto);
        nord = view.findViewById(R.id.nord);
        nord_compass = view.findViewById(R.id.nord_compass);

        gradi_alba = view.findViewById(R.id.gradi_alba);
        gradi_tramonto = view.findViewById(R.id.gradi_tramonto);

        if (isGooglePlayServicesAvailable(getContext())) {
            SupportMapFragment mapFragmentAlba = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_alba);
            mapFragmentAlba.getMapAsync(onMapReadyCallbackAlba());
            SupportMapFragment mapFragmentTramonto = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_tram);
            mapFragmentTramonto.getMapAsync(onMapReadyCallbackTramonto());
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {
        //float degree = Math.round(event.values[0])

        LocationListener ll = new mylocationlistener();
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, ll);
        }

        if (network_enabled) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            currentLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        float azimuth = event.values[0];

        if(currentLoc != null) {
            azimuth = azimuth * 180 / (float) Math.PI;
            GeomagneticField geoField = new GeomagneticField(
                    (float) currentLoc.getLatitude(),
                    (float) currentLoc.getLongitude(),
                    (float) currentLoc.getAltitude(),
                    System.currentTimeMillis());
            azimuth += geoField.getDeclination();
        }


        nord.setText((int)azimuth + "°");
        RotateAnimation nord = new RotateAnimation(
                currentDegree,
                -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        nord.setDuration(210);
        nord.setFillAfter(true);
        nord_compass.startAnimation(nord);

        //ALBA
        if(compassAlba  != null) {
            if ((currentDegree + angoloDaSotrarreAlba) < 3 && (currentDegree + angoloDaSotrarreAlba) > -3) {
                gradi_alba.setText("Allineato");
            } else {
                gradi_alba.setText(angoloDaSotrarreAlba + "° N");
            }

            RotateAnimation ra = new RotateAnimation(
                    currentDegree+angoloDaSotrarreAlba,
                    -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);
            compassAlba.startAnimation(ra);

        }
        
        //Tramonto
        if(compassTramonto != null) {
            if ((currentDegree + angoloDaSotrarreTramonto) < 3 && (currentDegree + angoloDaSotrarreTramonto) > -3) {
                gradi_tramonto.setText("Allineato");
            } else {
                gradi_tramonto.setText(angoloDaSotrarreTramonto + "° N");
            }

            RotateAnimation ra = new RotateAnimation(
                    currentDegree+angoloDaSotrarreTramonto,
                    -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);
            compassTramonto.startAnimation(ra);
        }

        updateCameraBearing(mapAlba, currentDegree);
        updateCameraBearing(mapTramonto, currentDegree);

        currentDegree = -azimuth;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private class mylocationlistener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLoc = location;
            }
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lon))             // Sets the center of the map to current location
                .zoom(13)                   // Sets the zoom
                .bearing(bearing) // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 0 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public OnMapReadyCallback onMapReadyCallbackAlba(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapAlba = googleMap;
                mapAlba.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng pos = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, 13, 0,0)));
                mapAlba.getUiSettings().setRotateGesturesEnabled(false);
                mapAlba.getUiSettings().setTiltGesturesEnabled(false);
                mapAlba.getUiSettings().setAllGesturesEnabled(false);
                mapAlba.getUiSettings().setCompassEnabled(false);
            }
        };
    }

    public OnMapReadyCallback onMapReadyCallbackTramonto(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapTramonto = googleMap;
                mapTramonto.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng pos = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, 13, 0,0)));
                mapTramonto.getUiSettings().setRotateGesturesEnabled(false);
                mapTramonto.getUiSettings().setTiltGesturesEnabled(false);
                mapTramonto.getUiSettings().setAllGesturesEnabled(false);
                mapTramonto.getUiSettings().setCompassEnabled(false);
            }
        };
    }
}
