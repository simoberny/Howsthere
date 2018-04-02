package it.unitn.simob.howsthere;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class MapsFragment extends Fragment implements OnMapReadyCallback, DatePickerDialog.OnDateSetListener {

    private GoogleMap gm = null;
    private LatLng ln = null;
    private int zoomLevel = 0;
    public static MapsFragment contesto;
    private Date dataSelezionata = Calendar.getInstance().getTime();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contesto = this;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        MapStateManager mgr = new MapStateManager(getActivity());
        mgr.saveMapState(gm);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       final View rootview = inflater.inflate(R.layout.fragment_maps, container, false);

       FloatingActionButton position = (FloatingActionButton) rootview.findViewById(R.id.position);
       position.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               getLocation(rootview);
           }
       });

        FloatingActionButton date = (FloatingActionButton) rootview.findViewById(R.id.dateopen);
        date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getActivity().getSupportFragmentManager(), "Seleziona data");
            }
        });

        FloatingActionButton send = (FloatingActionButton) rootview.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), Data.class);
                i.putExtra("lat", ln.latitude);
                i.putExtra("long", ln.longitude);
                i.putExtra("data", dataSelezionata.toString());
                startActivity(i);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       return rootview;
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    public void getLocation(View view) {
        double longi = 0, lat = 0;
        LocationListener ll = new mylocationlistener();

        final TextView tlat = (TextView) getActivity().findViewById(R.id.lat);
        final TextView tlog = (TextView) getActivity().findViewById(R.id.log);

        boolean gps_enabled = false;
        boolean network_enabled = false;
        Location net_loc = null, gps_loc = null, finalLoc = null;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            gm.setMyLocationEnabled(true);
            gm.getUiSettings().setMyLocationButtonEnabled(false);

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getActivity(), "GPS is disabled!", Toast.LENGTH_LONG).show();
            } else {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (gps_enabled) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, ll);
                }

                if (network_enabled)
                    net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getAccuracy() > net_loc.getAccuracy())
                        finalLoc = net_loc;
                    else
                        finalLoc = gps_loc;
                } else {

                    if (gps_loc != null) {
                        finalLoc = gps_loc;
                    } else if (net_loc != null) {
                        Toast.makeText(getActivity(), "No GPS", Toast.LENGTH_SHORT).show();
                        finalLoc = net_loc;
                    }
                }

                if (finalLoc != null) {
                    goToLoc(new LatLng(finalLoc.getLatitude(), finalLoc.getLongitude()), 5);
                }
            }

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    private void goToLoc(LatLng pos, int zoom){
        ln = new LatLng(pos.latitude, pos.longitude);

        final EditText lat = (EditText) getActivity().findViewById(R.id.latedit);
        final EditText log = (EditText) getActivity().findViewById(R.id.longedit);
        lat.setText("" + pos.latitude);
        log.setText("" + pos.longitude);

        gm.clear();
        gm.addMarker(new MarkerOptions().position(ln));
        gm.moveCamera(CameraUpdateFactory.newLatLng(ln));
        gm.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    public static class DatePickerFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(contesto.getContext(), (DatePickerDialog.OnDateSetListener) contesto, year, month, day);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        dataSelezionata = c.getTime();

        String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        TextView date = (TextView) getActivity().findViewById(R.id.datetext);
        date.setText(currentDate);
    }

    private class mylocationlistener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                goToLoc(new LatLng(location.getLatitude(), location.getLongitude()), 5);
            }
        }
        @Override
        public void onProviderDisabled(String provider) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        gm = googleMap;
        gm.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        MapStateManager mgr = new MapStateManager(getActivity());
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            gm.moveCamera(update);
            goToLoc(gm.getCameraPosition().target, (int) gm.getCameraPosition().zoom);
        }

        final EditText lat = (EditText) getActivity().findViewById(R.id.latedit);
        final EditText log = (EditText) getActivity().findViewById(R.id.longedit);
        LatLng ll = null;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                lat.setText("" + point.latitude);
                log.setText("" + point.longitude);
                gm.clear();
                gm.addMarker(new MarkerOptions().position(point));
            }
        });
    }

    public class MapStateManager {
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String ZOOM = "zoom";
        private static final String BEARING = "bearing";
        private static final String TILT = "tilt";
        private static final String MAPTYPE = "MAPTYPE";

        private static final String PREFS_NAME ="mapCameraState";

        private SharedPreferences mapStatePrefs;

        public MapStateManager(Context context) {
            mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        public void saveMapState(GoogleMap mapMie) {
            SharedPreferences.Editor editor = mapStatePrefs.edit();
            CameraPosition position = mapMie.getCameraPosition();

            editor.putFloat(LATITUDE, (float) position.target.latitude);
            editor.putFloat(LONGITUDE, (float) position.target.longitude);
            editor.putFloat(ZOOM, position.zoom);
            editor.putFloat(TILT, position.tilt);
            editor.putFloat(BEARING, position.bearing);
            editor.putInt(MAPTYPE, mapMie.getMapType());
            editor.commit();
        }

        public CameraPosition getSavedCameraPosition() {
            double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
            if (latitude == 0) {
                return null;
            }
            double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
            LatLng target = new LatLng(latitude, longitude);

            float zoom = mapStatePrefs.getFloat(ZOOM, 0);
            float bearing = mapStatePrefs.getFloat(BEARING, 0);
            float tilt = mapStatePrefs.getFloat(TILT, 0);

            CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
            return position;
        }

        public int getSavedMapType() {
            return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
        }
    }
}
