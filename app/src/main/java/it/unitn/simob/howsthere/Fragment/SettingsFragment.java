package it.unitn.simob.howsthere.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import io.grpc.internal.SharedResourceHolder;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.R;

public class SettingsFragment extends PreferenceFragmentCompat{
    public SettingsFragment() {}

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);

        final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("SettingsPref", 0);
        Integer map_type = pref.getInt("maps_type", 2);
        Integer map_source = pref.getInt("map", 0);

        ListPreference maps = (ListPreference) findPreference("maps_type");
        if(map_source == 1 || !isGooglePlayServicesAvailable(getActivity())){
            maps.setEntries(R.array.pref_maps_list_title_osm);
            maps.setEntryValues(R.array.pref_maps_list_values_osm);
            maps.setDialogTitle(R.string.pref_maps_type_osm);
            maps.setTitle(R.string.pref_maps_type_osm);
            maps.setValueIndex(map_type);
        }else{
            maps.setEntries(R.array.pref_maps_list_title);
            maps.setEntryValues(R.array.pref_maps_list_values);
            maps.setDialogTitle(R.string.pref_maps_type);
        }

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
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
                ((MainActivity) getActivity()).finish();
                return true;
            }
        });

        maps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = pref.edit();
                System.out.println(newValue);
                editor.putInt("maps_type", Integer.parseInt(newValue.toString()));
                editor.apply();
                return true;
            }
        });

        ListPreference sorgente_map = (ListPreference) findPreference("map");
        sorgente_map.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Integer map_source = Integer.parseInt(newValue.toString());
                ListPreference maps = (ListPreference) findPreference("maps_type");
                SharedPreferences.Editor editor = pref.edit();

                if(map_source == 0){
                    maps.setEntries(R.array.pref_maps_list_title);
                    maps.setEntryValues(R.array.pref_maps_list_values);
                    maps.setTitle(R.string.pref_maps_type);
                    maps.setDialogTitle(R.string.pref_maps_type);
                    maps.setValueIndex(2);
                }else{
                    maps.setEntries(R.array.pref_maps_list_title_osm);
                    maps.setEntryValues(R.array.pref_maps_list_values_osm);
                    maps.setDialogTitle(R.string.pref_maps_type_osm);
                    maps.setTitle(R.string.pref_maps_type_osm);
                    maps.setValueIndex(2);
                }

                editor.putInt("maps_type", 2);
                editor.putInt("map", map_source);
                editor.apply();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }
}