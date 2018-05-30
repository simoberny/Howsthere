package it.unitn.simob.howsthere;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import java.util.Date;

import it.unitn.simob.howsthere.Fragment.FeedFragment;
import it.unitn.simob.howsthere.Fragment.HistoryFragment;
import it.unitn.simob.howsthere.Fragment.MapsFragment;
import it.unitn.simob.howsthere.Fragment.UserFragment;
import it.unitn.simob.howsthere.Fragment.UserProfile;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;

public class MainActivity extends AppCompatActivity{

    private FeedFragment ff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        PanoramiStorage.context = this;
        PanoramiStorage.panorami_storage = new PanoramiStorage();
        PanoramiStorage.panorami_storage.initial_load();

        for(int i=0; i<0;i++){
            Panorama p = new Panorama();
            p.data = new Date();
            p.citta = "ciao"+ "  "+i;
            p.ID = ""+i;
            PanoramiStorage.panorami_storage.addPanorama(p);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("SettingsPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        Integer night_mode = pref.getInt("night_mode", 0);
        Boolean modify = pref.getBoolean("modify", false);

        if(night_mode == 2){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        Bundle extra = i.getExtras();

        ff = FeedFragment.newInstance(extra);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, MapsFragment.newInstance());
        transaction.commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if(modify){
            navigation.setSelectedItemId(R.id.navigation_user);
            editor.remove("modify");
            editor.commit();
        }else if(extra != null){
            navigation.setSelectedItemId(R.id.navigation_feed);
        }else{
            navigation.setSelectedItemId(R.id.navigation_home);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = MapsFragment.newInstance();
                    break;
                case R.id.navigation_storico:
                    selectedFragment = HistoryFragment.newInstance();
                    break;
                case R.id.navigation_feed:
                    selectedFragment = ff;
                    break;
                case R.id.navigation_user:
                    selectedFragment = UserProfile.newInstance();
                    break;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();
            return true;
        }
    };
}
