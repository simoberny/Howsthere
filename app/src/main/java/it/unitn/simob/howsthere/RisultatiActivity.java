package it.unitn.simob.howsthere;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.reflect.Field;

import it.unitn.simob.howsthere.Fragment.BussolaFragment;
import it.unitn.simob.howsthere.Fragment.MapsFragment;
import it.unitn.simob.howsthere.Fragment.MeteoFragment;

public class RisultatiActivity extends AppCompatActivity {
    MeteoFragment mt = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        Fragment selectedFragment = null;
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_risultati:
                    selectedFragment = mt;
                    break;
                case R.id.navigation_luna:
                    selectedFragment = mt;
                    break;
                case R.id.navigation_bussola:
                    selectedFragment = BussolaFragment.newInstance();
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

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultati_nav);

        mt = new MeteoFragment();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
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

}
