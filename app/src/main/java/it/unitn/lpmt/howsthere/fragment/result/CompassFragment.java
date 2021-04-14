package it.unitn.lpmt.howsthere.fragment.result;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.ArrayList;
import java.util.List;

import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.Results;
import it.unitn.lpmt.howsthere.objects.Panorama;

import static android.content.Context.SENSOR_SERVICE;

public class CompassFragment extends Fragment {
    ImageView compassAlba;
    ImageView compassTramonto;
    ImageView nord_compass;

    SensorManager mSensorManager;
    float azimut = 0.0f;

    Panorama p = null;
    private GoogleMap map = null;

    TextView gradi_alba, gradi_tramonto;
    float currentDegree = 0f;
    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

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

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
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
        List<Float> average_list = new ArrayList();
        float[] mGeomagnetic;
        float[] mGravity;
        Integer n_average = 10;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;

            if (mGravity != null && mGeomagnetic != null) {
                float Rot[] = new float[9];
                float I[] = new float[9];

                boolean success = SensorManager.getRotationMatrix(Rot, I, mGravity, mGeomagnetic);

                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(Rot, orientation);
                    average_list = roll(average_list, orientation[0]);

                    azimut = (float) (averageList(average_list) * 180 / Math.PI);

                    RotateAnimation nord = new RotateAnimation(
                            currentDegree,
                            -azimut,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);
                    nord.setDuration(200);
                    nord.setFillAfter(true);
                    nord_compass.startAnimation(nord);

                    //ALBA
                    if(compassAlba  != null) {
                        if ((currentDegree + angoloDaSotrarreAlba) < 4 && (currentDegree + angoloDaSotrarreAlba) > -4) {
                            gradi_alba.setText(getResources().getString(R.string.aligned));
                        } else {
                            gradi_alba.setText(angoloDaSotrarreAlba + "° N ");
                        }

                        RotateAnimation ra = new RotateAnimation(
                                currentDegree+angoloDaSotrarreAlba,
                                -azimut + angoloDaSotrarreTramonto,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        ra.setDuration(50);
                        ra.setFillAfter(true);
                        compassAlba.startAnimation(ra);
                    }

                    //Tramonto
                    if(compassTramonto != null) {
                        if ((currentDegree + angoloDaSotrarreTramonto) < 4 && (currentDegree + angoloDaSotrarreTramonto) > -4) {
                            gradi_tramonto.setText(getResources().getString(R.string.aligned));
                        } else {
                            gradi_tramonto.setText(angoloDaSotrarreTramonto + "° N");
                        }

                        RotateAnimation ra = new RotateAnimation(
                                currentDegree + angoloDaSotrarreTramonto,
                                -azimut + angoloDaSotrarreTramonto,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);
                        ra.setDuration(50);
                        ra.setFillAfter(true);
                        compassTramonto.startAnimation(ra);
                    }

                    updateCameraBearing(map, -currentDegree);
                    currentDegree = -azimut;
                }
            }
        }

        public List<Float> roll(List<Float> list, Float newMember) {
            if (list.size() == this.n_average.intValue()) {
                list.remove(0);
            }
            list.add(newMember);
            return list;
        }

        public float averageList(List<Float> list) {
            float total = 0.0f;
            for (Float item : list) {
                total += item.floatValue();
            }
            return total / ((float) list.size());
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