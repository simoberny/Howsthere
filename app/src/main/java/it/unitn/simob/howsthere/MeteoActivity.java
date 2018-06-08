package it.unitn.simob.howsthere;

import android.graphics.Typeface;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.WeatherRecyclerAdapter;
import it.unitn.simob.howsthere.Fragment.ForecastFragment;
import it.unitn.simob.howsthere.Fragment.MioFeedFragment;
import it.unitn.simob.howsthere.Fragment.PrecipitazioniFragment;
import it.unitn.simob.howsthere.Fragment.UserFragment;
import it.unitn.simob.howsthere.Fragment.UserProfile;
import it.unitn.simob.howsthere.Helper.ForecastCallback;
import it.unitn.simob.howsthere.Helper.TempUnitConverter;
import it.unitn.simob.howsthere.Helper.WeatherCallback;
import it.unitn.simob.howsthere.retrofit.models.ForecastResponseModel;
import it.unitn.simob.howsthere.retrofit.models.Weather;
import it.unitn.simob.howsthere.retrofit.models.WeatherCompleto;
import it.unitn.simob.howsthere.retrofit.models.WeatherResponseModel;

import static it.unitn.simob.howsthere.BuildConfig.OWM_API_KEY;

public class MeteoActivity extends AppCompatActivity {
    Typeface weatherFont;
    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView todayIcon;
    CustomViewPager viewPager;
    TabLayout tabLayout;

    public static String lat;
    public static String lon;

    AppBarLayout barL;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    public WeatherRecyclerAdapter adapter;
    private WeatherMap weatherMap;

    private List<WeatherCompleto> forecast = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meteo);

        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
        todayIcon = (TextView) findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        barL = findViewById(R.id.barlayout);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

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

        adapter = new WeatherRecyclerAdapter(this, forecast);
        weatherMap = new WeatherMap(this, OWM_API_KEY);

        /* VALORI TEMPORANEI SU TRENTO E ADESSO */
        lat = "46.071666";
        lon = "11.1158428";

        updateMeteo();
    }

    private void updateMeteo(){
        Date data = Calendar.getInstance().getTime();

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

                adapter.notifyDataSetChanged();
            }

            @Override
            public void failure(String message) {

            }
        });
    }

    private void updateUI(WeatherResponseModel response){
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        Weather weather[] = response.getWeather();

        String location = response.getName();
        String humidity= response.getMain().getHumidity();
        String pressure = response.getMain().getPressure();
        String windSpeed = response.getWind().getSpeed();

        Double temperature = TempUnitConverter.convertToCelsius(response.getMain().getTemp());
        Long temp_abb = Math.round(temperature);

        String desc = weather[0].getDescription();
        desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);

        getSupportActionBar().setTitle(location + (response.getSys().getCountry().isEmpty() ? "" : ", " + response.getSys().getCountry()));
        todayTemperature.setText(temp_abb.toString() + " °C");
        todayDescription.setText(desc);
        todayHumidity.setText("Umidità: " + humidity + " %");
        todayPressure.setText("Pressione: " + pressure + " hPa");
        todayWind.setText("Vento: " + windSpeed + " m/s");
        todaySunrise.setText("Alba" + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunrise()) * 1000)));
        todaySunset.setText("Tramonto" + ": " + timeFormat.format(new Date(Long.parseLong(response.getSys().getSunset()) * 1000)));
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
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = null;
            switch(position){
                case 0:
                    frag = ForecastFragment.newInstance();
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
