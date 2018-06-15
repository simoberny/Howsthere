package it.unitn.simob.howsthere;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import it.unitn.simob.howsthere.Fragment.FeedFragment;
import it.unitn.simob.howsthere.Fragment.HistoryFragment;
import it.unitn.simob.howsthere.Fragment.MapsFragment;
import it.unitn.simob.howsthere.Fragment.UserProfile;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;

public class MainActivity extends AppCompatActivity{

    private FeedFragment ff;
    public static final String PREF_USER_FIRST_TIME = "user_first_time";
    boolean isUserFirstTime;

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isUserFirstTime = Boolean.valueOf(readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));

        Intent introIntent = new Intent(MainActivity.this, Presentation.class);
        introIntent.putExtra(PREF_USER_FIRST_TIME, isUserFirstTime);

        if (isUserFirstTime)
            startActivity(introIntent);

        PanoramiStorage.context = this;
        PanoramiStorage.panorami_storage = new PanoramiStorage();
        PanoramiStorage.panorami_storage.initial_load();

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
        if(extra != null)
            System.out.println("EXTRA: " + extra.getBoolean("addFeed"));
        i.replaceExtras(new Bundle());
        i.setAction("");
        i.setData(null);
        i.setFlags(0);

        ff = FeedFragment.newInstance(extra);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if(modify) {
            navigation.setSelectedItemId(R.id.navigation_user);
            editor.remove("modify");
            editor.commit();
        }else if(extra != null && extra.getInt("login") == 1){
            navigation.setSelectedItemId(R.id.navigation_user);
        }else if(extra != null && extra.getBoolean("addFeed")){
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
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
    };

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(Presentation.FIRST_TIME_STORAGE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }
}
