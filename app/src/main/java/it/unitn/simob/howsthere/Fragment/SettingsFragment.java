package it.unitn.simob.howsthere.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat{
    private FirebaseAuth mAuth;
    FirebaseUser user;

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

        addPreferencesFromResource(R.xml.pref_general);

        final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("SettingsPref", 0);

        CheckBoxPreference nightmode = (CheckBoxPreference) findPreference("night_mode");
        nightmode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("modify", true);
                if ((Boolean) newValue) {
                    editor.putInt("night_mode", AppCompatDelegate.MODE_NIGHT_YES);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    ((MainActivity) getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    editor.putInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO);
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    ((MainActivity) getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                ((MainActivity) getActivity()).getDelegate().applyDayNight();
                ((MainActivity) getActivity()).recreate();
                return true;
            }
        });

        ListPreference maps = (ListPreference) findPreference("maps_type");
        maps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("maps_type", Integer.parseInt(newValue.toString()));
                editor.apply();
                return true;
            }
        });

        ListPreference sorgente_map = (ListPreference) findPreference("map");
        sorgente_map.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("map", Integer.parseInt(newValue.toString()));
                editor.apply();
                //((MainActivity)getActivity()).recreate();
                return true;
            }
        });


        user = mAuth.getCurrentUser();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
}