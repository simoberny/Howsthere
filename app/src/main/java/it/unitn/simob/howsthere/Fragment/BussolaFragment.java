package it.unitn.simob.howsthere.Fragment;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

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

public class BussolaFragment extends Fragment implements SensorEventListener{
    ImageView compassAlba;
    ImageView compassTramonto;
    ImageView nord_compass;
    float currentDegree = 0f;
    SensorManager mSensorManager;
    Double lat, lon;

    private GoogleMap mapTramonto = null;
    private GoogleMap mapAlba = null;

    TextView gradi_alba, gradi_tramonto, nord;

    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    public BussolaFragment() { }

    public static BussolaFragment newInstance(){
        BussolaFragment bs = new BussolaFragment();
        return bs;
    }

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        Panorama p = ((RisultatiActivity)getActivity()).p;

        lat = p.lat;
        lon = p.lon;

        angoloDaSotrarreTramonto = (float)Math.floor((float) p.getAlba().azimuth * 100)/ 100;
        angoloDaSotrarreAlba = (float)Math.floor((float) p.getTramonto().azimuth * 100)/100;

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

        if(isGooglePlayServicesAvailable(getContext())){
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

    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        nord.setText((int)degree + "°");
        RotateAnimation nord = new RotateAnimation(
                currentDegree,
                -degree,
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
                    -degree,
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
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);
            compassTramonto.startAnimation(ra);
        }

        updateCameraBearing(mapAlba, currentDegree);
        updateCameraBearing(mapTramonto, currentDegree);

        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void updateCameraBearing(GoogleMap googleMap, float bearing) {
        if ( googleMap == null) return;
        CameraPosition currentPlace = new CameraPosition.Builder()
                .target(googleMap.getCameraPosition().target)
                .bearing(bearing).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
    }

    public OnMapReadyCallback onMapReadyCallbackAlba(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapAlba = googleMap;
                mapAlba.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng vannes = new LatLng(lat, lon);
                mapAlba.addMarker(new MarkerOptions().position(vannes).title("Posizione selezionata"));
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(vannes, 20);
                mapAlba.animateCamera(yourLocation);
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
                LatLng vannes = new LatLng(lat, lon);
                mapTramonto.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mapTramonto.addMarker(new MarkerOptions().position(vannes).title("Posizione selezionata"));
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(vannes, 20);
                mapTramonto.animateCamera(yourLocation);
                mapTramonto.getUiSettings().setRotateGesturesEnabled(false);
                mapTramonto.getUiSettings().setTiltGesturesEnabled(false);
                mapTramonto.getUiSettings().setAllGesturesEnabled(false);
                mapTramonto.getUiSettings().setCompassEnabled(false);
            }
        };
    }
}
