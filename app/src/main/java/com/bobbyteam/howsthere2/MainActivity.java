package com.bobbyteam.howsthere2;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.bobbyteam.howsthere2.objects.PanoramaStorage;
import com.bobbyteam.howsthere2.objects.Utils;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bobbyteam.howsthere2.databinding.ActivityMainBinding;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load saved storage
        PanoramaStorage.getInstance().init(this);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Manage opened link to application
        Intent i = getIntent();
        Bundle extra = i.getExtras();
        String action = i.getAction();
        Uri appLinkData = i.getData();

        if (Intent.ACTION_VIEW.equals(action) && appLinkData != null){
            String date = appLinkData.getQueryParameter("date");
            String lat_query = appLinkData.getQueryParameter("lat");
            String lon_query = appLinkData.getQueryParameter("lon");
            String city = Utils.getCity(this, Double.parseDouble(lat_query), Double.parseDouble(lon_query));

            long date_query = Long.parseLong(date);

            Hwt hwt_data = new Hwt(this);
            hwt_data.initializePanorama(new LatLng(Double.parseDouble(lat_query), Double.parseDouble(lon_query)), city, new Date(date_query));
            hwt_data.requestData();
        }
    }
}