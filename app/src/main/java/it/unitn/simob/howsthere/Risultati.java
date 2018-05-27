package it.unitn.simob.howsthere;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;

public class Risultati extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultati);
        Intent i = getIntent();
        String id = i.getStringExtra("ID");
        System.out.println("id: " +id);
        PanoramiStorage panoramiStorage = new PanoramiStorage();
        Panorama p = panoramiStorage.getPanoramabyID(id);
        System.out.println("panorama NULL? " + p);
        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + p.ID);



    }
}
