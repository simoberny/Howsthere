package it.unitn.simob.howsthere.Fragment;


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

    TextView gradi_alba, gradi_tramonto, nord;

    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    public BussolaFragment() {
    }

    public static BussolaFragment newInstance(){
        BussolaFragment bs = new BussolaFragment();
        return bs;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        Panorama p = ((RisultatiActivity)getActivity()).p;

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

        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

}
