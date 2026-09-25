package it.howsthere.howsthere2.ui.result;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.howsthere.howsthere2.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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

import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.ui.timeline.TimelineAdapter;
import it.howsthere.howsthere2.ui.timeline.TimelineItem;

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

        vm.getPanorama().observe(getViewLifecycleOwner(), data -> {
            // Usa il dato aggiornato
            p = data;
            chart = current.findViewById(R.id.chart);

            if (p != null) {
                renderChart();
                renderValues(current);
                renderYearValues(current);
            } else {
                requireActivity().finish();
            }

            ImageButton saveSunrise = current.findViewById(R.id.sunriseMenu);
            saveSunrise.setOnClickListener(v -> {
                //showPopupMenu(v, "sunrise");
                saveToCalendar("sunrise");
            });

            ImageButton saveSunset = current.findViewById(R.id.sunsetMenu);
            saveSunset.setOnClickListener(v -> {
                //showPopupMenu(v, "sunset");
                saveToCalendar("sunset");
            });
        });

        // Inflate the layout for this fragment
        return current;
    }

    private void showPopupMenu(View view, String what) {
        ContextThemeWrapper ctw = new ContextThemeWrapper(requireActivity(), R.style.Widget_App_PopupMenu);
        PopupMenu popupMenu = new PopupMenu(ctw, view, Gravity.CENTER);
        popupMenu.inflate(R.menu.menu_calendar);
        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.save_to_calendar) {
                saveToCalendar(what); // Passa la zona come parametro
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void saveToCalendar(String what) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(p.date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if(Objects.equals(what, "sunrise")) {
            calendar.set(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day),
                    p.getFirstSunrise().hour, p.getFirstSunrise().minutes);
            intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.sunrise_photo));
        } else {
            calendar.set(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day),
                    p.getLastSunset().hour, p.getLastSunset().minutes);
            intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.sunset_photo));
        }

        long startmillis = calendar.getTimeInMillis();

        intent.setDataAndType(CalendarContract.Events.CONTENT_URI, "vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, p.lat + ", " + p.lon);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startmillis);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startmillis + 60 * 60 * 1000);
        startActivity(intent);
    }

    private void renderChart() {
        List<Entry> peaksVals = new ArrayList<Entry>();
        List<Entry> sunVals = new ArrayList<Entry>();

        // Fill sun positions
        //Arrays.sort(p.risultatiSole);
        for (int i = 0; i < Constants.SUN_SAMPLE; i++) {
            if (p.sun_data.get(i).minutes == 0) {
                // ogni tanto la libreria per il calcolo della traiettoria sbaglia
                // (bug noto che accade in posti lontani) in quel caso visto che l' errore
                // non lo possiamo gestire piùttosto stampiamo i valori validi che ci arrivano anche se sono a caso
                if (p.sun_data.get(i).height >= -20) {
                    sunVals.add(new Entry((float) p.sun_data.get(i).azimuth, (float) p.sun_data.get(i).height));
                } else {
                    sunVals.add(new Entry((float) p.sun_data.get(i).azimuth, (float) -20));
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
        chart.setScaleEnabled(true);
        chart.setScaleXEnabled(true); // Zoom sull'asse X
        chart.setScaleYEnabled(true); // Zoom sull'asse Y
        chart.setPinchZoom(true); // Usa due dita per zoomare contemporaneamente su X e Y
        chart.setDoubleTapToZoomEnabled(false);

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
        datasetPeaks.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                int azi = (int) entry.getX();

                if(p.peaks_name.get(azi) != null) {
                    return p.peaks_name.get(azi).getName();
                }

                return "";
            }
        });

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
                int index = sunVals.indexOf(entry);
                return index + ":00";
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
            chart.animateX(1000);
            chart.invalidate();
        } catch (Exception e) {
            System.out.println("Error in generating chart!");
        }
    }

    private void renderValues(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(p.date);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        SunTimes s = SunTimes.compute()
                .on(year, month, day)
                .at(p.lat, p.lon)
                .timezone(p.tz)
                .execute();

        SunTimes.Parameters base = SunTimes.compute()
                .on(year, month, day)
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
        ZonedDateTime now = ZonedDateTime.now();

        TextView sunriseText = view.findViewById(R.id.sunrise_time);
        TextView sunsetText = view.findViewById(R.id.sunset_time);

        FrameLayout frameTime = view.findViewById(R.id.frame_timeline);
        LinearLayout noPeak = view.findViewById(R.id.timeline_nopeak);
        LinearLayout labels = view.findViewById(R.id.timeline_text);

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

        if (p.getFirstSunrise() != null) {
            sunriseAzimutText.setText(new DecimalFormat("##.##").format(p.getFirstSunrise().azimuth));
            sunriseText.setText(p.getFirstSunrise().hour + ":" +
                    (p.getFirstSunrise().minutes < 10 ? "0" + p.getFirstSunrise().minutes : p.getFirstSunrise().minutes));
        } else {
            sunriseText.setText("--");
        }

        if (p.getLastSunset() != null) {
            sunsetAzimutText.setText(new DecimalFormat("##.##").format(p.getLastSunset().azimuth));
            sunsetText.setText(p.getLastSunset().hour + ":" +
                    (p.getLastSunset().minutes < 10 ? "0" + p.getLastSunset().minutes : p.getLastSunset().minutes));
        } else {
            sunsetText.setText("--");
        }

        Duration duration = Duration.between(rise, set);
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        sunTimeText.setText(hours + "h:" + ((minutes % 60) < 10 ? ("0" + (minutes % 60)) : minutes) + "min");
        sunTimeWithPeaksText.setText(p.sun_minutes / 60 + "h:" + ((p.sun_minutes % 60) < 10 ? ("0" + (p.sun_minutes % 60)) : (p.sun_minutes % 60)) + "min");

        // Render all the sunsire and sunset list
        RecyclerView risesList = view.findViewById(R.id.timeline_rises);
        RecyclerView setsList = view.findViewById(R.id.timeline_sets);
        risesList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        setsList.setLayoutManager(new LinearLayoutManager(requireActivity()));

        if (p.sunrise.size() > 1) {
            //risesList.setVisibility(View.VISIBLE);
            List<TimelineItem> sunriseTimeline = new ArrayList<>();

            for (int i = 0; i < p.sunrise.size(); i++) {
                String sunTime = p.sunrise.get(i).hour + ":" + (p.sunrise.get(i).minutes < 10 ? "0" + p.sunrise.get(i).minutes : p.sunrise.get(i).minutes);
                sunriseTimeline.add(new TimelineItem(sunTime));
            }

            TimelineAdapter adapter = new TimelineAdapter(sunriseTimeline);
            risesList.setAdapter(adapter);
        }

        if (p.sunset.size() > 1) {
            //setsList.setVisibility(View.VISIBLE);
            List<TimelineItem> sunsetTimeline = new ArrayList<>();

            for (int i = 0; i < p.sunset.size(); i++) {
                String sunTime = p.sunset.get(i).hour + ":" + (p.sunset.get(i).minutes < 10 ? "0" + p.sunset.get(i).minutes : p.sunset.get(i).minutes);
                sunsetTimeline.add(new TimelineItem(sunTime));
            }

            // Configura l'adapter
            TimelineAdapter adapter = new TimelineAdapter(sunsetTimeline);
            setsList.setAdapter(adapter);
        }

        if (p.getFirstSunrise() != null && p.getLastSunset() != null) {
            labels.setVisibility(View.VISIBLE);
            frameTime.setVisibility(View.VISIBLE);
            noPeak.setVisibility(View.VISIBLE);

            long startTwilight = 0;
            long sunrisePeak = p.getFirstSunrise().minutes * 60 * 1000 + p.getFirstSunrise().hour * 60 * 60 * 1000;
            long sunsetPeak = p.getLastSunset().minutes * 60 * 1000 + p.getLastSunset().hour * 60 * 60 * 1000;
            long sunriseHorizon = rise.getMinute() * 60 * 1000 + rise.getHour() * 60 * 60 * 1000;
            long sunsetHorizon = set.getMinute() * 60 * 1000 + set.getHour() * 60 * 60 * 1000;
            long endTwilight = 24 * 60 * 60 * 1000;
            long daySpace = now.getMinute() * 60 * 1000 + now.getHour() * 60 * 60 * 1000;

            long totalDuration = endTwilight - startTwilight;
            float twilightWeight = (float) (sunriseHorizon - startTwilight) / totalDuration;
            float sunriseDiffWeight = (float) (sunrisePeak - sunriseHorizon) / totalDuration;
            float dayHourWeight = (float) (sunsetPeak - sunrisePeak) / totalDuration;
            float sunsetDiffWeight = (float) (sunsetHorizon - sunsetPeak) / totalDuration;
            float nightWeight = (float) (endTwilight - sunsetHorizon) / totalDuration;

            float leftSpace = (float) (daySpace - startTwilight) / totalDuration;
            float rightSpace = (float) (endTwilight - daySpace) / totalDuration;

            // Barra colorata
            View twilightView = view.findViewById(R.id.twilight);
            View sunriseDiffView = view.findViewById(R.id.sunrise_lost);
            View dayLightView = view.findViewById(R.id.day_hour);
            View sunsetDiffView = view.findViewById(R.id.sunset_lost);
            View eveningTwilightView = view.findViewById(R.id.evening_twilight);

            setWeight(twilightView, twilightWeight);
            setWeight(sunriseDiffView, sunriseDiffWeight);
            setWeight(dayLightView, dayHourWeight);
            setWeight(sunsetDiffView, sunsetDiffWeight);
            setWeight(eveningTwilightView, nightWeight);

            // Puntatore orario
            View dayLeft = view.findViewById(R.id.left_now);
            View dayRight = view.findViewById(R.id.right_now);

            setWeight(dayLeft, leftSpace);
            setWeight(dayRight, rightSpace);

            // Orari con picchi
            TextView sunrisePeakLabel = view.findViewById(R.id.sunrise_timeline);
            TextView sunsetPeakLabel = view.findViewById(R.id.sunset_timeline);
            View morningPadding = view.findViewById(R.id.morning_padding);
            View dayPadding = view.findViewById(R.id.day_padding);
            View eveningPadding = view.findViewById(R.id.evening_padding);

            setWeight(morningPadding, twilightWeight + sunriseDiffWeight + 0.1f);
            setWeight(dayPadding, dayHourWeight);
            setWeight(eveningPadding, nightWeight + sunsetDiffWeight + 0.1f);

            // Orari orizzonte
            TextView sunriseLabel = view.findViewById(R.id.sunrise_nopeak_timeline);
            TextView sunsetLabel = view.findViewById(R.id.sunset_nopeak_timeline);
            View morningHorizonPadding = view.findViewById(R.id.morning_nopeak);
            View dayHorizonPadding = view.findViewById(R.id.day_nopeak);
            View eveningHorizonPadding = view.findViewById(R.id.evening_nopeak);

            setWeight(morningHorizonPadding, twilightWeight - 0.1f);
            setWeight(dayHorizonPadding, dayHourWeight);
            setWeight(eveningHorizonPadding, nightWeight - 0.1f);

            sunrisePeakLabel.setText(p.getFirstSunrise().hour + ":" +
                    (p.getFirstSunrise().minutes < 10 ? "0" + p.getFirstSunrise().minutes : p.getFirstSunrise().minutes));

            sunsetPeakLabel.setText(p.getLastSunset().hour + ":" +
                    (p.getLastSunset().minutes < 10 ? "0" + p.getLastSunset().minutes : p.getLastSunset().minutes));

            sunriseLabel.setText(rise.getHour() + ":" +
                    (rise.getMinute() < 10 ? "0" + rise.getMinute() : rise.getMinute()));

            sunsetLabel.setText(set.getHour() + ":" +
                    (set.getMinute() < 10 ? "0" + set.getMinute() : set.getMinute()));
        } else {
            labels.setVisibility(View.GONE);
            frameTime.setVisibility(View.GONE);
            noPeak.setVisibility(View.GONE);
        }
    }

    private void renderYearValues(View view) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        LinearProgressIndicator loading_shorter = view.findViewById(R.id.loading_shorter);
        LinearProgressIndicator loading_longer = view.findViewById(R.id.loading_longer);

        TextView shortestPeak = view.findViewById(R.id.shortest_peak);
        TextView shortestPeakTime = view.findViewById(R.id.shortest_peak_time);
        TextView longestPeak = view.findViewById(R.id.longest_peak);
        TextView longestPeakTime = view.findViewById(R.id.longest_peak_time);

        // Render shorter and longest day
        TextView shortestHorizon = view.findViewById(R.id.shortest_horizon);
        TextView shortestHorizonTime = view.findViewById(R.id.shortest_horizon_time);
        TextView longestHorizon = view.findViewById(R.id.longest_horizon);
        TextView longestHorizonTime = view.findViewById(R.id.longest_horizon_time);

        if(p.shortest_day != null && p.longest_day != null) {
            shortestHorizon.setText(sdf.format(p.shortest_day));
            longestHorizon.setText(sdf.format(p.longest_day));

            shortestHorizonTime.setText(p.shortest_minutes / 60 + "h:" + ((p.shortest_minutes % 60) < 10 ? ("0" + (p.shortest_minutes % 60)) : (p.shortest_minutes % 60)) + "min");
            longestHorizonTime.setText(p.longest_minutes / 60 + "h:" + ((p.longest_minutes % 60) < 10 ? ("0" + (p.longest_minutes % 60)) : (p.longest_minutes % 60)) + "min");
        }

        if(p.shortest_peak != null && p.longest_peak != null) {
            shortestPeak.setText(sdf.format(p.shortest_peak));
            longestPeak.setText(sdf.format(p.longest_peak));

            shortestPeakTime.setText(p.shortest_peak_minutes / 60 + "h:" + ((p.shortest_peak_minutes % 60) < 10 ? ("0" + (p.shortest_peak_minutes % 60)) : (p.shortest_peak_minutes % 60)) + "min");
            longestPeakTime.setText(p.longest_peak_minutes / 60 + "h:" + ((p.longest_peak_minutes % 60) < 10 ? ("0" + (p.longest_peak_minutes % 60)) : (p.longest_peak_minutes % 60)) + "min");
        }

        if(p.processedYearData) {
            loading_shorter.setVisibility(View.GONE);
            loading_longer.setVisibility(View.GONE);
        } else {
            loading_shorter.setVisibility(View.VISIBLE);
            loading_longer.setVisibility(View.VISIBLE);
        }
    }

    private void setWeight(View view, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }
}