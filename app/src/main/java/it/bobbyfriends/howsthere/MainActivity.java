package it.bobbyfriends.howsthere;

import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Date;
import java.util.Locale;

import it.bobbyfriends.howsthere.objects.PanoramaStorage;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);

        PanoramaStorage.context = this;
        PanoramaStorage.persistent_storage = new PanoramaStorage();
        PanoramaStorage.persistent_storage.init();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        // Check if opened link
        Intent i = getIntent();
        Bundle extra = i.getExtras();
        String action = i.getAction();
        Uri appLinkData = i.getData();

        if (Intent.ACTION_VIEW.equals(action) && appLinkData != null){
            String date = appLinkData.getQueryParameter("date");
            String lat_query = appLinkData.getQueryParameter("lat").toString();
            String lon_query = appLinkData.getQueryParameter("lon").toString();
            String city = appLinkData.getQueryParameter("city").toString();

            long date_query = Long.parseLong(date);

            Hwt hwt_data = new Hwt(this);
            hwt_data.initializePanorama(new LatLng(Double.parseDouble(lat_query), Double.parseDouble(lon_query)), city, new Date(date_query));
            hwt_data.requestData();
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}