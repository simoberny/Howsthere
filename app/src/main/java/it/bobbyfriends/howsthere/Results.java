package it.bobbyfriends.howsthere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import it.bobbyfriends.howsthere.objects.Panorama;
import it.bobbyfriends.howsthere.objects.PanoramaStorage;

public class Results extends AppCompatActivity {
    private Panorama p = null;
    private String id_pan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String id = (String) extras.get("ID");

        Toolbar toolbar = (Toolbar) findViewById(R.id.risultati_tool);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        PanoramaStorage panoramiStorage = PanoramaStorage.persistent_storage;
        p = panoramiStorage.getPanoramabyID(id);

        if (p.city != null) {
            getSupportActionBar().setTitle(p.city);
            System.out.println("Città:" + p.city);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        NavController navController = Navigation.findNavController(this, R.id.result_host);
        NavigationUI.setupWithNavController(navigation, navController);
    }
}