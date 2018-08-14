package it.unitn.simob.howsthere.Adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Helper.TempUnitConverter;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.Weather.models.WeatherCompleto;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherRecyclerAdapter.WeatherViewHolder> {
    private List<WeatherCompleto> itemList;
    private Context context;

    public WeatherRecyclerAdapter(Context context, List<WeatherCompleto> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder {
        public TextView itemDate;
        public TextView itemTemperature;
        public TextView itemDescription;
        public TextView itemyWind;
        public TextView itemPressure;
        public TextView itemHumidity;
        public TextView itemIcon;
        public View lineView;

        public WeatherViewHolder(View view) {
            super(view);
            this.itemDate = view.findViewById(R.id.itemDate);
            this.itemTemperature = view.findViewById(R.id.itemTemperature);
            this.itemDescription = view.findViewById(R.id.itemDescription);
            this.itemyWind = view.findViewById(R.id.itemWind);
            this.itemPressure = view.findViewById(R.id.itemPressure);
            this.itemHumidity = view.findViewById(R.id.itemHumidity);
            this.itemIcon = view.findViewById(R.id.itemIcon);
            this.lineView = view.findViewById(R.id.lineView);
        }
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.weather_line, null);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder customViewHolder, int i) {
        WeatherCompleto weatherItem = itemList.get(i);

        String desc = weatherItem.getWeather()[0].getDescription();
        desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);

        Resources res = context.getResources();

        customViewHolder.itemDescription.setText(desc);
        customViewHolder.itemHumidity.setText(res.getString(R.string.humidity) + ": " + weatherItem.getMain().getHumidity() + " %");
        customViewHolder.itemPressure.setText(res.getString(R.string.pressure) + ": " + weatherItem.getMain().getPressure() + " hPa");
        Double temperature = TempUnitConverter.convertToCelsius(weatherItem.getMain().getTemp());
        Long temp_abb = Math.round(temperature);
        customViewHolder.itemTemperature.setText(temp_abb + " °C");
        customViewHolder.itemyWind.setText(res.getString(R.string.wind) + ": " + weatherItem.getWind().getSpeed() + " m/s");

        DateFormat time = new SimpleDateFormat("HH:mm");
        customViewHolder.itemDate.setText(time.format(new Date(Long.parseLong(weatherItem.getDt()) * 1000)));

        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        customViewHolder.itemIcon.setTypeface(weatherFont);
        customViewHolder.itemIcon.setText(weatherItem.getIcon());
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}
