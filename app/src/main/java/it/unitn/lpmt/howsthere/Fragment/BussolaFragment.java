package it.unitn.lpmt.howsthere.Fragment;


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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;
import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.RisultatiActivity;

import static android.content.Context.SENSOR_SERVICE;

public class BussolaFragment extends Fragment implements SensorEventListener {
    ImageView compassAlba;
    ImageView compassTramonto;
    ImageView nord_compass;
    float currentDegree = 0f;
    SensorManager mSensorManager;
    Double lat, lon;
    Location currentLoc = null;
    Panorama p = null;

    private GoogleMap map = null;

    TextView gradi_alba, gradi_tramonto;

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
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        p = ((RisultatiActivity) getActivity()).p;

        lat = p.lat;
        lon = p.lon;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        if(p.albe.size() > 0 && p.tramonti.size() > 0) {
            angoloDaSotrarreAlba = (float) Math.floor((float) p.getAlba().azimuth * 100) / 100;
            angoloDaSotrarreTramonto = (float) Math.floor((float) p.getTramonto().azimuth * 100) / 100;
            mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bussola, container, false);
        //((RisultatiActivity)getActivity()).getSupportActionBar().setTitle("Alba e tramonto sole");

        RelativeLayout norise = view.findViewById(R.id.norise);

        if(p.albe.size() > 0 && p.tramonti.size() > 0){
            norise.setVisibility(View.GONE);

            compassAlba = view.findViewById(R.id.compassAlba);
            compassTramonto = view.findViewById(R.id.compassTramonto);

            //nord = view.findViewById(R.id.nord);
            nord_compass = view.findViewById(R.id.nord);

            gradi_alba = view.findViewById(R.id.gradi_alba);
            gradi_tramonto = view.findViewById(R.id.gradi_tramonto);

            if (isGooglePlayServicesAvailable(getContext())) {
                SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_bussola);
                mapFragment.getMapAsync(onMapReadyCallback());
            }

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSensorManager != null) mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSensorManager != null) mSensorManager.unregisterListener(this);
    }

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
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lon))
                .zoom(12)
                .bearing(bearing)
                .tilt(0)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public OnMapReadyCallback onMapReadyCallback(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng pos = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(pos, 12, 0,0)));
                map.getUiSettings().setRotateGesturesEnabled(false);
                map.getUiSettings().setTiltGesturesEnabled(false);
                map.getUiSettings().setAllGesturesEnabled(false);
                map.getUiSettings().setCompassEnabled(false);
            }
        };
    }

}
