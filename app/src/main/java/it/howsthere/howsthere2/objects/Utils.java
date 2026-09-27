package it.howsthere.howsthere2.objects;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.howsthere.howsthere2.R;

public final class Utils {
    private Utils() { }

    public static String getCity(Context context, double latitude, double longitude) {
        try {
            List<Address> addresses = new Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality();
                if (locality == null || locality.isEmpty()) locality = address.getAdminArea();
                String country = address.getCountryName();
                if (locality != null && !locality.isEmpty()) {
                    return country == null || country.isEmpty() ? locality : locality + ", " + country;
                }
                if (country != null && !country.isEmpty()) return country;
            }
        } catch (IOException error) {
            Log.w("Utils", "Cannot obtain city information", error);
            Toast.makeText(context, R.string.nocity, Toast.LENGTH_SHORT).show();
        }
        return context.getString(R.string.unavailable);
    }
}
