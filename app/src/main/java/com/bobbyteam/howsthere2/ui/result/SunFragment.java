package com.bobbyteam.howsthere2.ui.result;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bobbyteam.howsthere2.BuildConfig;
import com.bobbyteam.howsthere2.R;
import com.bobbyteam.howsthere2.objects.Panorama;
import com.bobbyteam.howsthere2.objects.PanoramaStorage;
import com.bobbyteam.howsthere2.ui.timeline.TimelineAdapter;
import com.bobbyteam.howsthere2.ui.timeline.TimelineItem;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.formatter.ValueFormatter;
import org.shredzone.commons.suncalc.SunTimes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class SunFragment extends Fragment {
    private Panorama p;
    private LineChart chart;

    public SunFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View current = inflater.inflate(R.layout.fragment_sun, container, false);
        ResultViewModel vm = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);

        vm.getData().observe(getViewLifecycleOwner(), data -> {
            // Usa il dato aggiornato
            p = PanoramaStorage.getInstance().getPanoramaByID(data);
            RecyclerView risesList = current.findViewById(R.id.timeline_rises);
            RecyclerView setsList = current.findViewById(R.id.timeline_sets);
            chart = current.findViewById(R.id.chart);

            if (p != null) {
                renderChart();
                renderValues(current);

                risesList.setLayoutManager(new LinearLayoutManager(requireActivity()));
                setsList.setLayoutManager(new LinearLayoutManager(requireActivity()));

                if (p.sunrise.size() > 1) {
                    risesList.setVisibility(View.VISIBLE);
                    List<TimelineItem> sunriseTimeline = new ArrayList<>();

                    for (int i = 0; i < p.sunrise.size(); i++) {
                        String sunTime = p.sunrise.get(i).hour + ":" + (p.sunrise.get(i).minutes < 10 ? "0" + p.sunrise.get(i).minutes : p.sunrise.get(i).minutes);
                        sunriseTimeline.add(new TimelineItem(sunTime));
                    }

                    TimelineAdapter adapter = new TimelineAdapter(sunriseTimeline);
                    risesList.setAdapter(adapter);
                }

                if (p.sunset.size() > 1) {
                    setsList.setVisibility(View.VISIBLE);

                    List<TimelineItem> sunsetTimeline = new ArrayList<>();

                    for (int i = 0; i < p.sunset.size(); i++) {
                        String sunTime = p.sunset.get(i).hour + ":" + (p.sunset.get(i).minutes < 10 ? "0" + p.sunset.get(i).minutes : p.sunset.get(i).minutes);
                        sunsetTimeline.add(new TimelineItem(sunTime));
                    }

                    // Configura l'adapter
                    TimelineAdapter adapter = new TimelineAdapter(sunsetTimeline);
                    setsList.setAdapter(adapter);
                }
            } else {
                requireActivity().finish();
            }

            Button saveSunrise = current.findViewById(R.id.save_sunrise);
            saveSunrise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(p.date); // Imposta la data nel calendario

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    calendar.set(Integer.valueOf(year), Integer.valueOf(month + 1 ), Integer.valueOf(day),
                            p.getFirstSunrise().hour, p.getFirstSunrise().minutes);

                    long startmillis = calendar.getTimeInMillis();
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + ", " + p.lon);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startmillis);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startmillis + 60 * 60 * 1000);
                    intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.sunrise_photo));
                    startActivity(intent);
                }
            });

            Button saveSunset = current.findViewById(R.id.save_sunset);
            saveSunset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(p.date); // Imposta la data nel calendario

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    calendar.set(Integer.valueOf(year), Integer.valueOf(month + 1 ), Integer.valueOf(day),
                            p.getLastSunset().hour, p.getLastSunset().minutes);

                    long startmillis = calendar.getTimeInMillis();
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + ", " + p.lon);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startmillis);
                    intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startmillis + 60 * 60 * 1000);
                    intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.sunset_photo));
                    startActivity(intent);
                }
            });
        });

        // Inflate the layout for this fragment
        return current;
    }

    private void renderChart() {
        List<Entry> peaksVals = new ArrayList<Entry>();
        List<Entry> sunVals = new ArrayList<Entry>();

        // Fill sun positions
        //Arrays.sort(p.risultatiSole);
        for (int i = 0; i < 288; i++) {
            if (p.sun_data[i].minutes == 0) {
                // ogni tanto la libreria per il calcolo della traiettoria sbaglia
                // (bug noto che accade in posti lontani) in quel caso visto che l' errore
                // non lo possiamo gestire piùttosto stampiamo i valori validi che ci arrivano anche se sono a caso
                if (p.sun_data[i].height >= -20) {
                    sunVals.add(new Entry((float) p.sun_data[i].azimuth, (float) p.sun_data[i].height));
                } else {
                    sunVals.add(new Entry((float) p.sun_data[i].azimuth, (float) -20));
                }
            }
        }

        // Create mountain line
        for (int i = 0; i < 360; i++) {
            peaksVals.add(new Entry((float) p.peaks_data[0][i], (float) p.peaks_data[2][i]));
        }

        LineDataSet datasetPeaks = new LineDataSet(peaksVals, requireActivity().getResources().getString(R.string.mountain));
        LineDataSet datasetSun = new LineDataSet(sunVals, requireActivity().getResources().getString(R.string.sole));

        // Chart properties
        chart.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_background));
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisRight().setAxisMinimum(0);
        chart.setPadding(0,0,0,0);
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setMaxVisibleValueCount(Integer.MAX_VALUE);//mostrami tutti i label
        chart.setScaleEnabled(false);
        chart.getDescription().setText("");

        // Peaks line properties
        datasetPeaks.setMode(LineDataSet.Mode.LINEAR);
        datasetPeaks.setColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_onBackground), 255);
        datasetPeaks.setDrawValues(false);
        datasetPeaks.setDrawCircles(false);
        datasetPeaks.setDrawCircleHole(false);
        datasetPeaks.setDrawValues(false);
        datasetPeaks.setDrawFilled(true);
        datasetPeaks.setLineWidth(1.5f);

        Drawable drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.fade_mountains);
        datasetPeaks.setFillDrawable(drawable);
        datasetPeaks.setDrawHighlightIndicators(true);

        // Sun line properties
        datasetSun.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        datasetSun.setColor(ContextCompat.getColor(requireActivity(), R.color.sun_color), 255);
        datasetSun.setLineWidth(3f);
        datasetSun.setCircleRadius(4f);
        datasetSun.setDrawValues(true);
        datasetSun.setDrawCircles(true);
        datasetSun.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.sun_color));
        datasetSun.setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_onBackground));
        datasetSun.setCircleHoleColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_background));
        datasetSun.setDrawCircleHole(true);
        datasetSun.setDrawFilled(false);
        datasetSun.setDrawValues(true);
        datasetSun.setDrawHighlightIndicators(true);
        datasetSun.setValueTextSize(9f);
        datasetSun.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                int idx = chart.getLineData().getDataSetForEntry(entry).getEntryIndex(entry);
                return String.valueOf(idx) + ":00";
            }
        });

        LineData lineData = new LineData();
        lineData.addDataSet(datasetPeaks);
        lineData.addDataSet(datasetSun);

        Legend l = chart.getLegend();
        l.setFormSize(10f);
        l.setTextSize(14f);
        l.setTextColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_onBackground));
        l.setXEntrySpace(10f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(6f); // set the space between the legend entries on the y-axis
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);

        try {
            chart.setData(lineData);
            chart.animateX(2500);
            chart.invalidate();
        } catch (Exception e) {
            System.out.println("Error in generating chart!");
        }
    }

    private void renderValues(View view) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        SunTimes s = SunTimes.compute()
                .on(p.date)
                .at(p.lat, p.lon)
                .timezone(p.tz)
                .execute();

        SunTimes.Parameters base = SunTimes.compute()
                .on(p.date)
                .at(p.lat, p.lon)
                .timezone(p.tz);

        SunTimes golden = base
                .copy()
                .twilight(SunTimes.Twilight.GOLDEN_HOUR)    // Golden Hour, 6°
                .execute();

        SunTimes blue = base
                .copy()
                .twilight(SunTimes.Twilight.BLUE_HOUR)      // Blue Hour, -4°
                .execute();

        SunTimes night = base
                .copy()
                .twilight(SunTimes.Twilight.NIGHT_HOUR)      // Night Hour, -8°
                .execute();

        ZonedDateTime nightRise = night.getRise();
        ZonedDateTime blueRise = blue.getRise();
        ZonedDateTime goldenRise = golden.getRise();

        ZonedDateTime goldenSet = golden.getSet();
        ZonedDateTime blueSet = blue.getSet();
        ZonedDateTime nightSet = night.getSet();

        ZonedDateTime rise = s.getRise();
        ZonedDateTime set = s.getSet();

        TextView sunriseText = view.findViewById(R.id.sunrise_time);
        TextView sunsetText = view.findViewById(R.id.sunset_time);
        TextView sunriseAzimutText = view.findViewById(R.id.azimut_sunrise);
        TextView sunsetAzimutText = view.findViewById(R.id.azimut_sunset);
        TextView sunriseHorizonText = view.findViewById(R.id.horizon_sunrise);
        TextView sunsetHorizonText = view.findViewById(R.id.horizon_sunset);
        TextView sunTimeText = view.findViewById(R.id.sun_time);
        TextView sunTimeWithPeaksText = view.findViewById(R.id.sun_time_mountains);

        TextView blueRiseStart = view.findViewById(R.id.blue_rise_start);
        TextView blueRiseEnd = view.findViewById(R.id.blue_rise_end);
        TextView goldenRiseStart = view.findViewById(R.id.golden_rise_start);
        TextView goldenRiseEnd = view.findViewById(R.id.golden_rise_end);
        TextView blueSetStart = view.findViewById(R.id.blue_set_start);
        TextView blueSetEnd = view.findViewById(R.id.blue_set_end);
        TextView goldenSetStart = view.findViewById(R.id.golden_set_start);
        TextView goldenSetEnd = view.findViewById(R.id.golden_set_end);

        blueRiseStart.setText(Objects.requireNonNull(nightRise).format(formatter));
        blueRiseEnd.setText(Objects.requireNonNull(blueRise).format(formatter));
        goldenRiseStart.setText(Objects.requireNonNull(blueRise).format(formatter));
        goldenRiseEnd.setText(Objects.requireNonNull(goldenRise).format(formatter));

        goldenSetStart.setText(Objects.requireNonNull(goldenSet).format(formatter));
        goldenSetEnd.setText(Objects.requireNonNull(blueSet).format(formatter));
        blueSetStart.setText(Objects.requireNonNull(blueSet).format(formatter));
        blueSetEnd.setText(Objects.requireNonNull(nightSet).format(formatter));

        sunriseHorizonText.setText(Objects.requireNonNull(rise).format(formatter));
        sunsetHorizonText.setText(Objects.requireNonNull(set).format(formatter));

        if(p.getFirstSunrise() != null) {
            sunriseAzimutText.setText(new DecimalFormat("##.##").format(p.getFirstSunrise().azimuth));
        }

        if(p.getLastSunset() != null) {
            sunsetAzimutText.setText(new DecimalFormat("##.##").format(p.getLastSunset().azimuth));
        }

        if (p.getFirstSunrise() != null) {
            sunriseText.setText(p.getFirstSunrise().hour + ":" +
                    (p.getFirstSunrise().minutes < 10 ? "0" + p.getFirstSunrise().minutes : p.getFirstSunrise().minutes));
        } else {
            sunriseText.setText("--");
        }

        if (p.getLastSunset() != null) {
            sunsetText.setText(p.getLastSunset().hour + ":" +
                    (p.getLastSunset().minutes < 10 ? "0" + p.getLastSunset().minutes : p.getLastSunset().minutes));
        } else {
            sunsetText.setText("--");
        }

        Duration duration = Duration.between(rise, set);
        sunTimeText.setText(duration.toHours() + ":" + duration.toMinutes() % 60);
        sunTimeWithPeaksText.setText(p.sun_minutes / 60 + ":" + ((p.sun_minutes % 60) < 10 ? ("0" + (p.sun_minutes % 60)) : (p.sun_minutes % 60)));

        Duration totalDuration = Duration.between(nightRise, nightSet);
        Duration blueDuration = Duration.between(nightRise, blueRise);
        Duration goldenDuration = Duration.between(blueRise, goldenRise);
        Duration dayDuration = Duration.between(goldenRise, goldenSet);
        Duration goldenSetDuration = Duration.between(goldenSet, blueSet);
        Duration blueSetDuration = Duration.between(blueSet, nightSet);

        long totalMillis = totalDuration.getSeconds() * 1000;
        float blueMillis = (float) (blueDuration.getSeconds() * 1000) / totalMillis;
        float goldenMillis = (float) (goldenDuration.getSeconds() * 1000) / totalMillis;
        float dayMillis = (float) (dayDuration.getSeconds() * 1000) / totalMillis;
        float goldenSetMillis = (float) (goldenSetDuration.getSeconds() * 1000) / totalMillis;
        float blueSetMillis = (float) (blueSetDuration.getSeconds() * 1000) / totalMillis;
    }

    private void setWeight(View view, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }
}