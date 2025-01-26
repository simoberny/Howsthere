package it.howsthere.howsthere2.ui.result;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

import org.shredzone.commons.suncalc.MoonTimes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.ui.timeline.TimelineAdapter;
import it.howsthere.howsthere2.ui.timeline.TimelineItem;

public class MoonFragment extends Fragment {
    private Panorama p;
    private LineChart chart;

    public MoonFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View current = inflater.inflate(R.layout.fragment_moon, container, false);
        ResultViewModel vm = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);

        vm.getPanorama().observe(getViewLifecycleOwner(), data -> {
            // Usa il dato aggiornato
            p = data;
            chart = current.findViewById(R.id.chart_moon);

            if (p != null) {
                renderChart();
                renderValues(current);
            } else {
                requireActivity().finish();
            }

            ImageButton saveSunrise = current.findViewById(R.id.moonriseMenu);
            saveSunrise.setOnClickListener(v -> {
                //showPopupMenu(v, "sunrise");
                saveToCalendar("sunrise");
            });

            ImageButton saveSunset = current.findViewById(R.id.moonsetMenu);
            saveSunset.setOnClickListener(v -> {
                //showPopupMenu(v, "sunset");
                saveToCalendar("sunset");
            });
        });

        return current;
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
                    p.getFirstMoonSunrise().hour, p.getFirstMoonSunrise().minutes);
            intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.moonrise_photo));
        } else {
            calendar.set(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(day),
                    p.getLastMoonSunset().hour, p.getLastMoonSunset().minutes);
            intent.putExtra(CalendarContract.Events.TITLE, requireActivity().getString(R.string.moonset_photo));
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
        List<Entry> moonVals = new ArrayList<Entry>();
        List<Entry> moonSorted = new ArrayList<Entry>();

        int a = 0;

        // Fill moon positions
        for (int i = 288; i < 576; i++) {
            if (p.moon_data.get(i).minutes == 0) {
                moonVals.add(new Entry((float) p.moon_data.get(i).azimuth, (float) p.moon_data.get(i).height));
                moonSorted.add(new Entry((float) p.moon_data.get(i).azimuth, (float) p.moon_data.get(i).height));
            }
        }

        //Collections.sort(moonSorted, new EntryXComparator());

        // Create mountain line
        for (int i = 0; i < 360; i++) {
            peaksVals.add(new Entry((float) p.peaks_data[0][i], (float) p.peaks_data[2][i]));
        }

        LineDataSet datasetPeaks = new LineDataSet(peaksVals, requireActivity().getResources().getString(R.string.mountain));
        LineDataSet datasetMoon = new LineDataSet(moonSorted, requireActivity().getResources().getString(R.string.moon));

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
        chart.setMaxVisibleValueCount(Integer.MAX_VALUE);
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

        // Sun line properties
        datasetMoon.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        datasetMoon.setColor(ContextCompat.getColor(requireActivity(), R.color.moon_color), 255);
        datasetMoon.setLineWidth(3f);
        datasetMoon.setCircleRadius(4f);
        datasetMoon.setDrawValues(true);
        datasetMoon.setDrawCircles(true);
        datasetMoon.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.moon_color));
        datasetMoon.setValueTextColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_onBackground));
        datasetMoon.setCircleHoleColor(ContextCompat.getColor(requireActivity(), R.color.md_theme_background));
        datasetMoon.setDrawCircleHole(true);
        datasetMoon.setDrawFilled(false);
        datasetMoon.setDrawValues(true);
        datasetMoon.setDrawHighlightIndicators(true);
        datasetMoon.setValueTextSize(9f);
        datasetMoon.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                int index = moonSorted.indexOf(entry);
                return index + ":00";
            }
        });

        LineData lineData = new LineData();
        lineData.addDataSet(datasetMoon);
        lineData.addDataSet(datasetPeaks);

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
            chart.notifyDataSetChanged();
        } catch (Exception e) {
            System.out.println("Error in generating chart!");
        }
    }

    private void renderValues(View view) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(p.date); // Imposta la data nel calendario

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        MoonTimes moonPos = MoonTimes.compute()
            .on(year, month, day)
            .at(p.lat, p.lon)
            .timezone(p.tz)
            .execute();

        ZonedDateTime rise = moonPos.getRise();
        ZonedDateTime set = moonPos.getSet();

        TextView sunriseText = view.findViewById(R.id.sunrise_time);
        TextView sunsetText = view.findViewById(R.id.sunset_time);
        TextView sunriseAzimutText = view.findViewById(R.id.azimut_sunrise);
        TextView sunsetAzimutText = view.findViewById(R.id.azimut_sunset);
        TextView sunriseHorizonText = view.findViewById(R.id.horizon_sunrise);
        TextView sunsetHorizonText = view.findViewById(R.id.horizon_sunset);

        TextView moonPhaseText = view.findViewById(R.id.moon_phase);
        TextView moonPercText = view.findViewById(R.id.moon_perc);

        TextView nextFullText = view.findViewById(R.id.next_full);
        TextView nextSuperText = view.findViewById(R.id.next_super);

        if (p.getFirstMoonSunrise() != null) {
            sunriseAzimutText.setText(new DecimalFormat("##.##").format(p.getFirstMoonSunrise().azimuth));
            sunriseText.setText(p.getFirstMoonSunrise().hour + ":" +
                    (p.getFirstMoonSunrise().minutes < 10 ? "0" + p.getFirstMoonSunrise().minutes : p.getFirstMoonSunrise().minutes));
        } else {
            sunriseText.setText("--");
        }

        if (p.getLastMoonSunset() != null) {
            sunsetAzimutText.setText(new DecimalFormat("##.##").format(p.getLastMoonSunset().azimuth));
            sunsetText.setText(p.getLastMoonSunset().hour + ":" +
                    (p.getLastMoonSunset().minutes < 10 ? "0" + p.getLastMoonSunset().minutes : p.getLastMoonSunset().minutes));
        } else {
            sunsetText.setText("--");
        }

        sunriseHorizonText.setText(Objects.requireNonNull(rise).format(timeFormatter));
        sunsetHorizonText.setText(Objects.requireNonNull(set).format(timeFormatter));

        /*   //determinazione stringa per fase lunare
            170 - 180 + -180 - -170     nuova luna
            -170 - -85                  luna crescente
            -85 - -95                   primo quarto
            -95 - -10                   Gibbosa crescente
            -10 - 10                    Luna piena
            10 - 85                     Gibbosa calante
            85 - 95                     ultimo quarto
            95 - 170                    luna calante
       */
        String phaseName = "";
        if ((p.moon_phase >= 175 && p.moon_phase < 181) || (p.moon_phase >= -180 && p.moon_phase < -170))
            phaseName = requireActivity().getString(R.string.newmoon);

        if (p.moon_phase >= -170 && p.moon_phase < -85)
            phaseName = requireActivity().getString(R.string.waxing_crescent);

        if (p.moon_phase >= -85 && p.moon_phase < -95)
            phaseName = requireActivity().getString(R.string.first_quarter);

        if (p.moon_phase >= -95 && p.moon_phase < -10)
            phaseName = requireActivity().getString(R.string.waxing_gibbous);

        if (p.moon_phase >= -10 && p.moon_phase < 10)
            phaseName = requireActivity().getString(R.string.full_moon);

        if (p.moon_phase >= 10 && p.moon_phase < 85)
            phaseName = requireActivity().getString(R.string.waning_gibbous);

        if (p.moon_phase >= 85 && p.moon_phase < 95)
            phaseName = requireActivity().getString(R.string.last_quarter);

        if (p.moon_phase >= 95 && p.moon_phase < 175)
            phaseName = requireActivity().getString(R.string.waning_crescent);

        moonPhaseText.setText(phaseName + " (" + Math.round(p.moon_phase * 100) / 100.0d + ") ");
        moonPercText.setText((int) p.moon_perc + "%");

        nextFullText.setText(sdf.format(p.next_fullmoon));
        nextSuperText.setText(sdf.format(p.next_supermoon));

        RecyclerView risesList = view.findViewById(R.id.timeline_rises);
        RecyclerView setsList = view.findViewById(R.id.timeline_sets);

        risesList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        setsList.setLayoutManager(new LinearLayoutManager(requireActivity()));

        if (p.moon_sunsire.size() > 1) {
            risesList.setVisibility(View.VISIBLE);
            List<TimelineItem> sunriseTimeline = new ArrayList<>();

            for (int i = 0; i < p.moon_sunsire.size(); i++) {
                String sunTime = p.moon_sunsire.get(i).hour + ":" +
                        (p.moon_sunsire.get(i).minutes < 10 ? "0" + p.moon_sunsire.get(i).minutes : p.moon_sunsire.get(i).minutes);
                sunriseTimeline.add(new TimelineItem(sunTime));
            }

            TimelineAdapter adapter = new TimelineAdapter(sunriseTimeline);
            risesList.setAdapter(adapter);
        }

        if (p.moon_sunset.size() > 1) {
            setsList.setVisibility(View.VISIBLE);

            List<TimelineItem> sunsetTimeline = new ArrayList<>();

            for (int i = 0; i < p.moon_sunset.size(); i++) {
                String sunTime = p.moon_sunset.get(i).hour + ":" +
                        (p.moon_sunset.get(i).minutes < 10 ? "0" + p.moon_sunset.get(i).minutes : p.moon_sunset.get(i).minutes);
                sunsetTimeline.add(new TimelineItem(sunTime));
            }

            // Configura l'adapter
            TimelineAdapter adapter = new TimelineAdapter(sunsetTimeline);
            setsList.setAdapter(adapter);
        }
    }
}