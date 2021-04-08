package it.bobbyfriends.howsthere.fragment.result;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import it.bobbyfriends.howsthere.R;
import it.bobbyfriends.howsthere.Results;
import it.bobbyfriends.howsthere.objects.Panorama;
import it.bobbyfriends.howsthere.objects.PanoramaStorage;

import static android.content.Context.SENSOR_SERVICE;

public class CompassFragment extends Fragment {
    ImageView compassAlba;
    ImageView compassTramonto;
    ImageView nord_compass;

    SensorManager mSensorManager;
    private Sensor mAccelerometer, mGyroscope, mMagnetometer;

    Panorama p = null;
    private GoogleMap map = null;

    TextView gradi_alba, gradi_tramonto;
    float currentDegree = 0f;
    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    public CompassFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        p = ((Results) getActivity()).getPanorama();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        if(p.albe.size() > 0 && p.tramonti.size() > 0) {
            angoloDaSotrarreAlba = (float) Math.floor((float) p.getAlba().azimuth * 100) / 100;
            angoloDaSotrarreTramonto = (float) Math.floor((float) p.getTramonto().azimuth * 100) / 100;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);

        RelativeLayout norise = view.findViewById(R.id.norise);

        if(p.albe.size() > 0 && p.tramonti.size() > 0){
            norise.setVisibility(View.GONE);

            compassAlba = view.findViewById(R.id.compassAlba);
            compassTramonto = view.findViewById(R.id.compassTramonto);

            nord_compass = view.findViewById(R.id.nord);

            gradi_alba = view.findViewById(R.id.gradi_alba);
            gradi_tramonto = view.findViewById(R.id.gradi_tramonto);

            SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_bussola);
            mapFragment.getMapAsync(onMapReadyCallback());
        }

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(mSensorEventListener, mGyroscope, SensorManager.SENSOR_DELAY_GAME);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSensorManager != null) mSensorManager.unregisterListener(mSensorEventListener);
    }

    public OnMapReadyCallback onMapReadyCallback(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng pos = new LatLng(p.lat, p.lon);
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, 12, 0,0)));
                map.getUiSettings().setRotateGesturesEnabled(false);
                map.getUiSettings().setTiltGesturesEnabled(false);
                map.getUiSettings().setAllGesturesEnabled(false);
                map.getUiSettings().setCompassEnabled(false);
            }
        };
    }

    /**
     * Listener that handles sensor events
     */
    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float azimuth = event.values[0];
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
                    gradi_alba.setText(getResources().getString(R.string.aligned));
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
                    gradi_tramonto.setText(getResources().getString(R.string.aligned));
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

            updateCameraBearing(map, -currentDegree);
            currentDegree = -azimuth;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(p.lat, p.lon))
                .zoom(12)
                .bearing(bearing)
                .tilt(0)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}