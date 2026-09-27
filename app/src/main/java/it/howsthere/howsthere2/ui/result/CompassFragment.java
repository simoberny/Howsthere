package it.howsthere.howsthere2.ui.result;

import static android.content.Context.SENSOR_SERVICE;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.howsthere.howsthere2.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import it.howsthere.howsthere2.objects.Panorama;

public class CompassFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {
    private Panorama p;
    private SensorManager sensorManager;

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView nordImage;
    private ImageView sunriseImage;
    private ImageView sunsetImage;

    private TextView sunriseAzimut;
    private TextView sunsetAzimut;

    private float currentDegree = 0f;

    public CompassFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View current = inflater.inflate(R.layout.fragment_compass, container, false);

        mapView = current.findViewById(R.id.map_bussola);
        nordImage = current.findViewById(R.id.compass_nord);
        sunriseImage = current.findViewById(R.id.compass_sunrise);
        sunsetImage = current.findViewById(R.id.compass_sunset);

        // Inizializza la mappa
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        sensorManager = (SensorManager) requireActivity().getSystemService(SENSOR_SERVICE);

        return current;
    }

    @Override
    public void onViewCreated(View current, Bundle savedInstanceState) {
        super.onViewCreated(current, savedInstanceState);
        sunriseAzimut = current.findViewById(R.id.sunrise_azimut);
        sunsetAzimut = current.findViewById(R.id.sunset_azimut);
        View noRise = current.findViewById(R.id.no_rise);
        new ViewModelProvider(requireActivity()).get(ResultViewModel.class)
                .getPanorama().observe(getViewLifecycleOwner(), data -> {
                    p = data;
                    boolean hasEvents = p != null && p.getFirstSunrise() != null
                            && p.getLastSunset() != null;
                    noRise.setVisibility(hasEvents ? View.GONE : View.VISIBLE);
                    centerMap();
                });
    }

    private void centerMap() {
        if (googleMap == null || p == null) return;
        LatLng position = new LatLng(p.lat, p.lon);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition(position, 12, 0, 0)));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mapView == null) return;
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(true);
        centerMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mapView != null) mapView.onSaveInstanceState(state);
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
        if (p == null || p.getFirstSunrise() == null || p.getLastSunset() == null) return;

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

            rotate(nordImage, currentDegree, -azimuth);
            updateEvent(sunriseImage, sunriseAzimut, p.getFirstSunrise().azimuth, azimuth);
            updateEvent(sunsetImage, sunsetAzimut, p.getLastSunset().azimuth, azimuth);

            currentDegree = -azimuth;
        }
    }

    private void updateEvent(ImageView image, TextView label, double eventAzimuth, float azimuth) {
        double difference = (eventAzimuth - azimuth + 540) % 360 - 180;
        label.setText(Math.abs(difference) < 4 ? getString(R.string.aligned)
                : Math.round(eventAzimuth) + "° N");
        rotate(image, (float) (currentDegree + eventAzimuth), (float) (eventAzimuth - azimuth));
    }

    private void rotate(ImageView image, float from, float to) {
        RotateAnimation animation = new RotateAnimation(from, to,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(50);
        animation.setFillAfter(true);
        image.startAnimation(animation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onDestroyView() {
        sensorManager.unregisterListener(this);
        mapView.onDestroy();
        mapView = null;
        googleMap = null;
        nordImage = null;
        sunriseImage = null;
        sunsetImage = null;
        sunriseAzimut = null;
        sunsetAzimut = null;
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }
}
