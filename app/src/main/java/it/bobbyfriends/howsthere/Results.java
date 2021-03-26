package it.bobbyfriends.howsthere;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import it.bobbyfriends.howsthere.objects.Panorama;

public class Results extends AppCompatActivity {

    /*private MeteoFragment mt = null;
        private SunFragment sf = null;
        private BussolaFragment bf = null;
        private MoonFragment mf = null;
        private PeakFragment pf = null;*/

    private Panorama p = null;
    private String id_pan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String id = (String) extras.get("ID");

        TextView idtext = findViewById(R.id.id_result);
        idtext.setText(id);
    }
}