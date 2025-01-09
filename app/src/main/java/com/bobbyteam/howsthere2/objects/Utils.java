package com.bobbyteam.howsthere2.objects;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.bobbyteam.howsthere2.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static String getCity(Context context, Double latitude, Double longitude) {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        String city = null;

        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && !adr.getLocality().isEmpty()) {
                        city = adr.getLocality() + ", " + adr.getCountryName();
                    }else{
                        city = adr.getAdminArea() + ", " + adr.getCountryName();
                    }
                }
            }

            if(city == null)
                city = context.getResources().getString(R.string.unavailable);
        } catch (IOException e) {
            System.out.println("Cannot obtain city information: " + e.getMessage());
            Toast toast = Toast.makeText(context,
                    context.getResources().getString(R.string.nocity), Toast.LENGTH_SHORT);
            toast.show();
        }

        return city;
    }
}
