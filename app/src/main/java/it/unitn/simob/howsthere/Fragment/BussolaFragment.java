package it.unitn.simob.howsthere.Fragment;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import it.unitn.simob.howsthere.R;

import static android.content.Context.SENSOR_SERVICE;

public class BussolaFragment extends Fragment implements SensorEventListener{
    ImageView compassAlba;
    ImageView compassTramonto;
    float currentDegree = 0f;
    SensorManager mSensorManager;

    TextView azimuthCompass;

    float angoloDaSotrarreAlba = 0;
    float angoloDaSotrarreTramonto = 0;

    public BussolaFragment() {
    }

    public static BussolaFragment newInstance(){
        BussolaFragment bs = new BussolaFragment();
        return bs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bussola, container, false);

        angoloDaSotrarreAlba = 25;
        angoloDaSotrarreTramonto = 170;

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        azimuthCompass = view.findViewById(R.id.azimuthCompass);

        compassAlba = (ImageView) view.findViewById(R.id.compassAlba);
        compassTramonto = (ImageView) view.findViewById(R.id.compassTramonto);

        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

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

        //ALBA
        if(azimuthCompass != null) {
            if ((currentDegree + angoloDaSotrarreAlba) < 3 && (currentDegree + angoloDaSotrarreAlba) > -3) {
                azimuthCompass.setText("Azimuth Alba: " + (degree) + " degrees, Direzione Corretta!");
            } else {
                azimuthCompass.setText("Azimuth: " + (degree) + " degrees");
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
        if(azimuthCompass != null) {
            if ((currentDegree + angoloDaSotrarreTramonto) < 3 && (currentDegree + angoloDaSotrarreTramonto) > -3) {
                azimuthCompass.setText("Azimuth Tramonto: " + (degree) + " degrees, Direzione Corretta!");
            } else {
                azimuthCompass.setText("Azimuth: " + (degree) + " degrees");
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
