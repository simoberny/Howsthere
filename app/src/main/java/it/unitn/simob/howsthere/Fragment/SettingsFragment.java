package it.unitn.simob.howsthere.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.unitn.simob.howsthere.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    private FirebaseAuth mAuth;
    FirebaseUser user;
    SharedPreferences prefG;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        prefG = getActivity().getPreferences(Context.MODE_PRIVATE);

        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.pref_general);

        user = mAuth.getCurrentUser();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Gestire i cambi di impostazioni
        /*if (key.equals("pref_name")) {
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, (user != null) ? user.getDisplayName() : "Non loggato!"));
        }else if(key.equals("maps_type")){
            ListPreference pref = (ListPreference) findPreference(key);
            pref.setValue(sharedPreferences.getString(key, "-1"));
        }*/

    }


}
