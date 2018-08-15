package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import it.unitn.simob.howsthere.R;

import static it.unitn.simob.howsthere.BuildConfig.OWM_API_KEY;

public class PrecipitazioniFragment extends Fragment {
    public PrecipitazioniFragment() {}

    public static PrecipitazioniFragment newInstance(){
        PrecipitazioniFragment pc = new PrecipitazioniFragment();
        return pc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_precipitazioni, container, false);

        final WebView webView = view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/map.html?lat=" + MeteoFragment.lon + "&lon=" + MeteoFragment.lat + "&appid=" + OWM_API_KEY);
        webView.loadUrl("javascript:map.removeLayer(windLayer);map.removeLayer(tempLayer);map.addLayer(rainLayer);");

        BottomNavigationView bv = view.findViewById(R.id.bv);
        bv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.map_rain) {
                    webView.loadUrl("javascript:map.removeLayer(windLayer);map.removeLayer(tempLayer);map.addLayer(rainLayer);");
                } else if (item.getItemId() == R.id.map_wind) {
                    webView.loadUrl("javascript:map.removeLayer(rainLayer);map.removeLayer(tempLayer);map.addLayer(windLayer);");
                } else if (item.getItemId() == R.id.map_temperature) {
                    webView.loadUrl("javascript:map.removeLayer(windLayer);map.removeLayer(rainLayer);map.addLayer(tempLayer);");
                }
                return true;
            }
        });

        return view;
    }

}
