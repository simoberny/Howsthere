package it.unitn.simob.howsthere.Fragment;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.WeatherRecyclerAdapter;
import it.unitn.simob.howsthere.CustomViewPager;
import it.unitn.simob.howsthere.Helper.ForecastCallback;
import it.unitn.simob.howsthere.Helper.TempUnitConverter;
import it.unitn.simob.howsthere.Helper.WeatherCallback;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.RisultatiActivity;
import it.unitn.simob.howsthere.WeatherMap;
import it.unitn.simob.howsthere.retrofit.models.ForecastResponseModel;
import it.unitn.simob.howsthere.retrofit.models.Weather;
import it.unitn.simob.howsthere.retrofit.models.WeatherCompleto;
import it.unitn.simob.howsthere.retrofit.models.WeatherResponseModel;

import static it.unitn.simob.howsthere.BuildConfig.OWM_API_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeteoFragment extends Fragment {
    Typeface weatherFont;
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView todayIcon;
    TabLayout tabLayout;

    public static String lat;
    public static String lon;

    private AppBarLayout barL;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private WeatherMap weatherMap;

    private List<WeatherCompleto> forecast = new ArrayList<>();

    ForecastFragment ff;

    public MeteoFragment() {
        // Required empty public constructor
        ff = ForecastFragment.newInstance(forecast);
    }

    public static MeteoFragment newInstance(){
        MeteoFragment mf = new MeteoFragment();
        return mf;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        Panorama p = ((RisultatiActivity)getActivity()).p;

        lat = String.valueOf(p.lat);
        lon = String.valueOf(p.lon);

        updateMeteo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meteo, container, false);

        todayTemperature = (TextView) view.findViewById(R.id.todayTemperature);
        todayDescription = (TextView) view.findViewById(R.id.todayDescription);
        todayWind = (TextView) view.findViewById(R.id.todayWind);
        todayPressure = (TextView) view.findViewById(R.id.todayPressure);
        todayHumidity = (TextView) view.findViewById(R.id.todayHumidity);
        todaySunrise = (TextView) view.findViewById(R.id.todaySunrise);
        todaySunset = (TextView) view.findViewById(R.id.todaySunset);
        todayIcon = (TextView) view.findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        barL = view.findViewById(R.id.barlayout);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 1){
                    barL.setExpanded(false);
                }else{
                    barL.setExpanded(true);
                }

                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        weatherMap = new WeatherMap(getActivity(), OWM_API_KEY);

        return view;
    }

    private void updateMeteo(){
        weatherMap.getLocationWeather(lat, lon, new WeatherCallback() {
            @Override
            public void success(WeatherResponseModel response) {
                updateUI(response);
            }

            @Override
            public void failure(String message) {
            }
        });

        weatherMap.getLocationForecast(lat, lon, new ForecastCallback() {
            @Override
            public void success(ForecastResponseModel response) {
                for(int i = 0; i < response.getList().length; i++){
                    final String dateMsString = response.getList()[i].getDt() + "000";
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(dateMsString));
                    Calendar today = Calendar.getInstance();
                    if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                        WeatherCompleto temp = response.getList()[i];
                        temp.setIcon(setWeatherIcon(Integer.parseInt(temp.getWeather()[0].getId()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
                        forecast.add(temp);
                    }
                }
                ff.update(forecast);
            }

            @Override
            public void failure(String message) {}
        });
    }

    private void updateUI(WeatherResponseModel response){
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

        Weather weather[] = response.getWeather();

        String location = response.getName();
        String humidity= response.getMain().getHumidity();
        String pressure = response.getMain().getPressure();
        String windSpeed = response.getWind().getSpeed();

        Double temperature = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
        Long temp_abb = Math.round(temperature);

        String desc = weather[0].getDescription();
        desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);

        todayTemperature.setText(temp_abb.toString() + " °C");
        todayDescription.setText(desc);
        todayHumidity.setText("Umidità: " + humidity + " %");
        todayPressure.setText("Pressione: " + pressure + " hPa");
        todayWind.setText("Vento: " + windSpeed + " m/s");
        todaySunrise.setText("Alba" + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunrise()) * 1000)));
        todaySunset.setText("Tramonto" + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunset()) * 1000)));
        todayIcon.setText(setWeatherIcon(Integer.parseInt(weather[0].getId()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

        ((RisultatiActivity)getActivity()).getSupportActionBar().setTitle(location + ", " + response.getSys().getCountry());
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = this.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = null;
            switch(position){
                case 0:
                    frag = ff;
                    break;
                case 1:
                    frag = PrecipitazioniFragment.newInstance();
                    break;
            }

            return frag;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }
}
