package it.unitn.lpmt.howsthere.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import it.unitn.lpmt.howsthere.MainActivity;
import it.unitn.lpmt.howsthere.R;

public class SettingsFragment extends PreferenceFragmentCompat{
    public SettingsFragment() {}

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);
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

        /*CheckBoxPreference nightmode = (CheckBoxPreference) findPreference("night_mode");
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
        });*/

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

        Preference info = (Preference) findPreference("info");
        info.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openHWTinfo();
                return false;
            }
        });
    }

    private void openHWTinfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.hwt_dialog, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) { }

    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }
}