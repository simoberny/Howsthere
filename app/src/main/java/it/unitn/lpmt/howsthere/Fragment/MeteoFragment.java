package it.unitn.lpmt.howsthere.Fragment;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import it.unitn.lpmt.howsthere.Helper.ForecastCallback;
import it.unitn.lpmt.howsthere.Helper.TempUnitConverter;
import it.unitn.lpmt.howsthere.Helper.WeatherCallback;
import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.RisultatiActivity;
import it.unitn.lpmt.howsthere.Weather.models.ForecastResponseModel;
import it.unitn.lpmt.howsthere.Weather.models.Weather;
import it.unitn.lpmt.howsthere.Weather.models.WeatherCompleto;
import it.unitn.lpmt.howsthere.Weather.models.WeatherResponseModel;
import it.unitn.lpmt.howsthere.WeatherMap;

import static it.unitn.lpmt.howsthere.BuildConfig.OWM_API_KEY;

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
    private String locale = "en";
    Panorama p = null;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private WeatherMap weatherMap;

    private List<WeatherCompleto> forecast = new ArrayList<>();
    ForecastFragment ff;

    public MeteoFragment() {
        ff = ForecastFragment.newInstance(forecast);
    }

    public static MeteoFragment newInstance(){
        MeteoFragment mf = new MeteoFragment();
        return mf;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lat = String.valueOf(p.lat);
        lon = String.valueOf(p.lon);

        updateMeteo();
    }

    @Override
    public void onResume() {
        super.onResume();
        forecast.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meteo, container, false);

        //Prendo ID e panorama (Uno dei due sarà null dipendentemente da che posto arriva
        p = ((RisultatiActivity) Objects.requireNonNull(getActivity())).p;

        todayTemperature = view.findViewById(R.id.todayTemperature);
        todayDescription = view.findViewById(R.id.todayDescription);
        todayWind = view.findViewById(R.id.todayWind);
        todayPressure = view.findViewById(R.id.todayPressure);
        todayHumidity = view.findViewById(R.id.todayHumidity);
        todaySunrise = view.findViewById(R.id.todaySunrise);
        todaySunset = view.findViewById(R.id.todaySunset);
        todayIcon = view.findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        barL = view.findViewById(R.id.barlayout);
        tabLayout = view.findViewById(R.id.tabs);

        String d = (String) android.text.format.DateFormat.format("dd",p.data)+"/"+ (String) android.text.format.DateFormat.format("MM",p.data)+"/"+ (String) android.text.format.DateFormat.format("yyyy",p.data);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        long diff = 0;
        try {
            Date date = format.parse(d);
            long diffInMillies = Math.abs(new Date().getTime() - date.getTime());
            diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tabLayout.getTabAt(0).setText((diff == 0) ? getResources().getString(R.string.today) : d);

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

        locale = getResources().getConfiguration().locale.getLanguage();
        weatherMap = new WeatherMap(getActivity(), OWM_API_KEY);

        return view;
    }

    private void updateMeteo(){
        weatherMap.getLocationWeather(lat, lon, locale, new WeatherCallback() {
            @Override
            public void success(WeatherResponseModel response) {
                updateUI(response);
            }

            @Override
            public void failure(String message) {
            }
        });

        weatherMap.getLocationForecast(lat, lon,locale, new ForecastCallback() {
            @Override
            public void success(ForecastResponseModel response) {
                for(int i = 0; i < response.getList().length; i++){
                    final String dateMsString = response.getList()[i].getDt() + "000";
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(dateMsString));
                    Calendar today = Calendar.getInstance();

                    String d = (String) android.text.format.DateFormat.format("dd",p.data)+"/"+ (String) android.text.format.DateFormat.format("MM",p.data)+"/"+ (String) android.text.format.DateFormat.format("yyyy",p.data);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

                    Date date = null;
                    try {
                        date = format.parse(d);
                        today.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)){
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
        String humidity= response.getMain().getHumidity();
        String pressure = response.getMain().getPressure();
        String windSpeed = response.getWind().getSpeed();

        Double temperature = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
        Long temp_abb = Math.round(temperature);

        String desc = weather[0].getDescription();
        desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);

        Resources res = getContext().getResources();
        todayTemperature.setText(temp_abb.toString() + " °C");
        todayDescription.setText(desc);
        todayHumidity.setText(res.getString(R.string.humidity) + ": " + humidity + " %");
        todayPressure.setText(res.getString(R.string.pressure) + ": " + pressure + " hPa");
        todayWind.setText(res.getString(R.string.wind) + ": " + windSpeed + " m/s");
        todaySunrise.setText(res.getString(R.string.alba) + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunrise()) * 1000)));
        todaySunset.setText(res.getString(R.string.tramonto) + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunset()) * 1000)));
        todayIcon.setText(setWeatherIcon(Integer.parseInt(weather[0].getId()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
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
        SectionsPagerAdapter(FragmentManager fm) {
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
