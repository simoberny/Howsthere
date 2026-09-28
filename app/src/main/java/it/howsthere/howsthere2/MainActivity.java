package it.howsthere.howsthere2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import it.howsthere.howsthere2.ui.SystemInsets;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import it.howsthere.howsthere2.databinding.ActivityMainBinding;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.objects.Utils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load saved storage
        PanoramaStorage.getInstance().init(this);

        SystemInsets.applyWithStatusBarBackground(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Manage opened link to application
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String path = data.getPath();

            // Esegui azioni in base ai dati ricevuti
            if ("/share".equals(path)) {
                String date = data.getQueryParameter("date");
                String lat_query = data.getQueryParameter("lat");
                String lon_query = data.getQueryParameter("lon");

                if (date != null && lat_query != null && lon_query != null) {
                    try {
                        double lat = Double.parseDouble(lat_query);
                        double lon = Double.parseDouble(lon_query);
                        long date_query = Long.parseLong(date);
                        String city = Utils.getCity(this, lat, lon);

                        Hwt hwt_data = new Hwt(this);
                        hwt_data.initializePanorama(new LatLng(lat, lon), city, new Date(date_query));
                        hwt_data.requestData();
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }
}
