package it.unitn.simob.howsthere;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassActivity extends AppCompatActivity implements SensorEventListener{


    //componenti popup bussola
    ImageView image;
    float currentDegree = 0f;
    SensorManager mSensorManager;
    TextView azimuthCompass;
    TextView titoloCompass;
    float angoloDaSotrarre = 0;
    String titolo = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        Intent i = getIntent();
        titolo = i.getStringExtra("titolo");
        angoloDaSotrarre = i.getFloatExtra("angoloDaSottrarre",0);
        System.out.println("angloloDa  Compass: " + angoloDaSotrarre);
        TextView titoloTv = (TextView)findViewById(R.id.titoloCompass);
        titoloTv.setText(titolo);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAA  onsensor changed!!");
        float degree = Math.round(event.values[0]);
        if(findViewById(R.id.azimuthCompass) != null) { //controllo che sia stato creato il popup
            azimuthCompass = (TextView) findViewById(R.id.azimuthCompass);
            if((currentDegree+angoloDaSotrarre)<3 && (currentDegree+angoloDaSotrarre)>-3){
                azimuthCompass.setText("Azimuth: " + (degree) + " degrees, Direzione Corretta!");
            }else{
                azimuthCompass.setText("Azimuth: " + (degree) + " degrees");
            }
        }
        // create a rotation animation (reverse turn degree degrees)
        if(findViewById(R.id.imageViewCompass) != null) {
            RotateAnimation ra = new RotateAnimation(
                    currentDegree+angoloDaSotrarre,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            // how long the animation will take place
            ra.setDuration(210);
            // set the animation after the end of the reservation status
            ra.setFillAfter(true);
            // Start the animation
            image = (ImageView) findViewById(R.id.imageViewCompass);
            image.startAnimation(ra);
        }

        //ROTAZIONE ICONA DIRETTAMENTE NELL' imageButton per mettere l' animazione direttamente in result
        /*ImageButton ib = (ImageButton) findViewById(R.id.provaImmagine);
        Matrix matrix = new Matrix();

        matrix.postRotate(currentDegree+angoloDaSotrarre);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.compass_icon);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(icon,60,60,true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        BitmapDrawable bdrawable = new BitmapDrawable(getResources(),rotatedBitmap);
        ib.setBackground(bdrawable);*/

        currentDegree = -degree;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //niente da fare
    }



    public void chiudiPopup(View v) {
        System.out.println("chiudi compass");
        finish();
        //pwindo.dismiss();
    }
}
