package com.bobbyteam.howsthere2.ui.result;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bobbyteam.howsthere2.R;
import com.bobbyteam.howsthere2.objects.Panorama;
import com.bobbyteam.howsthere2.objects.PanoramaStorage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class CompassFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {
    Panorama p = null;
    private SensorManager sensorManager;

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView nordImage;
    private ImageView sunriseImage;
    private ImageView sunsetImage;

    TextView sunriseAzimut;
    TextView sunsetAzimut;

    float currentDegree = 0f;

    public CompassFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View current = inflater.inflate(R.layout.fragment_compass, container, false);
        ResultViewModel vm = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);

        mapView = current.findViewById(R.id.map_bussola);
        nordImage = current.findViewById(R.id.compass_nord);
        sunriseImage = current.findViewById(R.id.compass_sunrise);
        sunsetImage = current.findViewById(R.id.compass_sunset);

        // Inizializza la mappa
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        sensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);

        vm.getPanorama().observe(getViewLifecycleOwner(), data -> {
            p = data;

            RelativeLayout noRise = current.findViewById(R.id.no_rise);
            sunriseAzimut = current.findViewById(R.id.sunrise_azimut);
            sunsetAzimut = current.findViewById(R.id.sunset_azimut);

            if (p != null) {
                if(!p.sunrise.isEmpty() && !p.sunset.isEmpty()) {
                    noRise.setVisibility(View.GONE);
                } else {
                    noRise.setVisibility(View.VISIBLE);
                }
            }
        });

        return current;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Imposta la mappa per seguire sempre il nord (modalità non tiltata)
        googleMap.getUiSettings().setTiltGesturesEnabled(false);

        LatLng pos = new LatLng(p.lat, p.lon);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, 12, 0,0)));
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        // Registra il listener per il sensore di orientamento
        Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (p != null) {
            if (p.sunrise.isEmpty() && p.sunset.isEmpty()) {
                return;
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && googleMap != null) {
            // Ottieni la matrice di rotazione dal sensore
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            // Calcola l'azimuth (orientamento rispetto al nord)
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);

            // Normalizza l'azimuth tra 0 e 360
            azimuth = (azimuth + 360) % 360;

            // Aggiorna la posizione della fotocamera sulla mappa
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(googleMap.getCameraPosition().target) // Mantieni la posizione attuale
                    .zoom(googleMap.getCameraPosition().zoom)     // Mantieni lo zoom attuale
                    .bearing(azimuth)                            // Ruota la mappa
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            RotateAnimation nordAni = new RotateAnimation(
                    currentDegree,
                    -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            nordAni.setDuration(50);
            nordAni.setFillAfter(true);
            nordImage.startAnimation(nordAni);

            if ((currentDegree + p.getFirstSunrise().azimuth) < 4 && (currentDegree + p.getFirstSunrise().azimuth) > -4) {
                sunriseAzimut.setText(getResources().getString(R.string.aligned));
            } else {
                sunriseAzimut.setText((int) Math.round(p.getFirstSunrise().azimuth) + "° N ");
            }

            if ((currentDegree + p.getLastSunset().azimuth) < 4 && (currentDegree + p.getLastSunset().azimuth) > -4) {
                sunsetAzimut.setText(getResources().getString(R.string.aligned));
            } else {
                sunsetAzimut.setText((int) Math.round(p.getLastSunset().azimuth) + "° N ");
            }

            RotateAnimation sunriseAni = new RotateAnimation(
                    (float) (currentDegree + p.getFirstSunrise().azimuth),
                    (float) (-azimuth + p.getFirstSunrise().azimuth),
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            sunriseAni.setDuration(50);
            sunriseAni.setFillAfter(true);
            sunriseImage.startAnimation(sunriseAni);

            RotateAnimation sunsetAni = new RotateAnimation(
                    (float) (currentDegree + p.getLastSunset().azimuth),
                    (float) (-azimuth + p.getLastSunset().azimuth),
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            sunsetAni.setDuration(50);
            sunsetAni.setFillAfter(true);
            sunsetImage.startAnimation(sunsetAni);

            currentDegree = -azimuth;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}