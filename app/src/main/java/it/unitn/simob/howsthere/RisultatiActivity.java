package it.unitn.simob.howsthere;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import it.unitn.simob.howsthere.Fragment.BussolaFragment;
import it.unitn.simob.howsthere.Fragment.MapsFragment;
import it.unitn.simob.howsthere.Fragment.MeteoFragment;
import it.unitn.simob.howsthere.Fragment.SunFragment;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;

public class RisultatiActivity extends AppCompatActivity {
    private MeteoFragment mt = null;
    private SunFragment sf = null;
    private BussolaFragment bf = null;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultati_nav);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        mt = MeteoFragment.newInstance(extras);
        sf = SunFragment.newInstance(extras);
        bf = BussolaFragment.newInstance(extras);

        String id = (String) extras.get("ID");
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        if(id != null) {
            Panorama p = panoramiStorage.getPanoramabyID(id);
            getSupportActionBar().setTitle(getLocation(p.lat, p.lon));
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        BottomNavigationMenuView menuview = (BottomNavigationMenuView) navigation.getChildAt(0);
        try {
            Field shiftingMode = menuview.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuview, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuview.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuview.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
        }

        navigation.setSelectedItemId(R.id.navigation_risultati);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        Fragment selectedFragment = null;
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_risultati:
                    selectedFragment = sf;
                    break;
                case R.id.navigation_luna:
                    selectedFragment = sf;
                    break;
                case R.id.navigation_bussola:
                    selectedFragment = bf;
                    break;
                case R.id.navigation_meteo:
                    selectedFragment = mt;
                    break;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.frame_layout_risultati, selectedFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
    };

    private String getLocation(double lat, double lon){
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String citta = "";
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                if(addresses.get(0).getLocality() != null){
                    citta = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                }else{
                    citta = lat + ", " + lon;
                }
            }else{
                citta = lat + ", " + lon;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return citta;
    }

}
