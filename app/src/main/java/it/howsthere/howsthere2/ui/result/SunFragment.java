package it.howsthere.howsthere2.ui.result;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.shredzone.commons.suncalc.SunTimes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Calendar;

import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.objects.Panorama;

public class SunFragment extends Fragment {
    private Panorama p;
    private LineChart chart;

    public SunFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sun, container, false);
    }

    @Override
    public void onViewCreated(View current, Bundle savedInstanceState) {
        super.onViewCreated(current, savedInstanceState);
        current.findViewById(R.id.open_ar).setOnClickListener(v -> ArPanoramaFragment.open(this, false));
        chart = current.findViewById(R.id.chart);
        ResultViewModel vm = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);

        vm.getPanorama().observe(getViewLifecycleOwner(), data -> {
            // Usa il dato aggiornato
            p = data;

            if (p != null) {
                new PanoramaChartController(chart, p, current.findViewById(R.id.peaks_count),
                        p.sun_data, R.string.sole, R.color.sun_color, true).render();
                if (current.findViewById(R.id.sunrise_time) != null) {
                    renderValues(current);
                    renderYearValues(current);
                }
            } else {
                requireActivity().finish();
            }

        });

        View saveSunrise = current.findViewById(R.id.sunriseMenu);
        if (saveSunrise != null) saveSunrise.setOnClickListener(v -> saveToCalendar(true));
        View saveSunset = current.findViewById(R.id.sunsetMenu);
        if (saveSunset != null) saveSunset.setOnClickListener(v -> saveToCalendar(false));
    }

    @Override
    public void onDestroyView() {
        chart.setOnChartValueSelectedListener(null);
        chart = null;
        super.onDestroyView();
    }

    private void saveToCalendar(boolean rising) {
        if (p == null) return;
        CalendarEvent.insert(requireContext(), p,
                rising ? p.getFirstSunrise() : p.getLastSunset(),
                rising ? R.string.sunrise_photo : R.string.sunset_photo);
    }

    private void renderValues(View view) {
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

        blueRiseStart.setText(ResultFormatting.time(nightRise));
        blueRiseEnd.setText(ResultFormatting.time(blueRise));
        goldenRiseStart.setText(ResultFormatting.time(blueRise));
        goldenRiseEnd.setText(ResultFormatting.time(goldenRise));
        goldenSetStart.setText(ResultFormatting.time(goldenSet));
        goldenSetEnd.setText(ResultFormatting.time(blueSet));
        blueSetStart.setText(ResultFormatting.time(blueSet));
        blueSetEnd.setText(ResultFormatting.time(nightSet));

        sunriseHorizonText.setText(ResultFormatting.time(rise));
        sunsetHorizonText.setText(ResultFormatting.time(set));

        if (p.getFirstSunrise() != null) {
            sunriseAzimutText.setText(new DecimalFormat("##.##").format(p.getFirstSunrise().azimuth));
            sunriseText.setText(ResultFormatting.time(p.getFirstSunrise()));
        } else {
            sunriseText.setText("--");
            sunriseAzimutText.setText("--");
        }

        if (p.getLastSunset() != null) {
            sunsetAzimutText.setText(new DecimalFormat("##.##").format(p.getLastSunset().azimuth));
            sunsetText.setText(ResultFormatting.time(p.getLastSunset()));
        } else {
            sunsetText.setText("--");
            sunsetAzimutText.setText("--");
        }

        if (rise != null && set != null) {
            sunTimeText.setText(ResultFormatting.duration(Duration.between(rise, set).toMinutes()));
        } else {
            sunTimeText.setText("--");
        }
        sunTimeWithPeaksText.setText(ResultFormatting.duration(p.sun_minutes));

        if (p.getFirstSunrise() != null && p.getLastSunset() != null && rise != null && set != null) {
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

            sunrisePeakLabel.setText(ResultFormatting.time(p.getFirstSunrise()));

            sunsetPeakLabel.setText(ResultFormatting.time(p.getLastSunset()));

            sunriseLabel.setText(ResultFormatting.time(rise));

            sunsetLabel.setText(ResultFormatting.time(set));
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

            shortestHorizonTime.setText(ResultFormatting.duration(p.shortest_minutes));
            longestHorizonTime.setText(ResultFormatting.duration(p.longest_minutes));
        }

        if(p.shortest_peak != null && p.longest_peak != null) {
            shortestPeak.setText(sdf.format(p.shortest_peak));
            longestPeak.setText(sdf.format(p.longest_peak));

            shortestPeakTime.setText(ResultFormatting.duration(p.shortest_peak_minutes));
            longestPeakTime.setText(ResultFormatting.duration(p.longest_peak_minutes));
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
        if (view == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        if (params != null) {
            params.weight = Math.max(0f, weight);
            view.setLayoutParams(params);
        }
    }
}