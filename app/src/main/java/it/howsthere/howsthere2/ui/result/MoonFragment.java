package it.howsthere.howsthere2.ui.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.howsthere.howsthere2.R;
import com.github.mikephil.charting.charts.LineChart;

import org.shredzone.commons.suncalc.MoonTimes;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.Position;
import it.howsthere.howsthere2.ui.timeline.TimelineAdapter;

public class MoonFragment extends Fragment {
    private Panorama p;
    private LineChart chart;

    public MoonFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_moon, container, false);
    }

    @Override
    public void onViewCreated(View current, Bundle savedInstanceState) {
        super.onViewCreated(current, savedInstanceState);
        current.findViewById(R.id.open_ar).setOnClickListener(v -> ArPanoramaFragment.open(this, true));
        chart = current.findViewById(R.id.chart_moon);
        ResultViewModel vm = new ViewModelProvider(requireActivity()).get(ResultViewModel.class);
        vm.getPanorama().observe(getViewLifecycleOwner(), data -> {
            p = data;
            if (p == null) {
                requireActivity().finish();
                return;
            }
            // Moon samples contain yesterday, the selected day and tomorrow.
            new PanoramaChartController(chart, p, current.findViewById(R.id.peaks_count),
                    MoonChartSamples.selectedDay(p.moon_data), R.string.moon, R.color.moon_color, false).render();
            if (current.findViewById(R.id.sunrise_time) != null) renderValues(current);
        });
        View riseButton = current.findViewById(R.id.moonriseMenu);
        if (riseButton != null) riseButton.setOnClickListener(v -> saveToCalendar(true));
        View setButton = current.findViewById(R.id.moonsetMenu);
        if (setButton != null) setButton.setOnClickListener(v -> saveToCalendar(false));
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
                rising ? p.getFirstMoonSunrise() : p.getLastMoonSunset(),
                rising ? R.string.moonrise_photo : R.string.moonset_photo);
    }

    private void renderValues(View view) {
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
            sunriseText.setText(ResultFormatting.time(p.getFirstMoonSunrise()));
        } else {
            sunriseText.setText("--");
            sunriseAzimutText.setText("--");
        }

        if (p.getLastMoonSunset() != null) {
            sunsetAzimutText.setText(new DecimalFormat("##.##").format(p.getLastMoonSunset().azimuth));
            sunsetText.setText(ResultFormatting.time(p.getLastMoonSunset()));
        } else {
            sunsetText.setText("--");
            sunsetAzimutText.setText("--");
        }

        sunriseHorizonText.setText(ResultFormatting.time(rise));
        sunsetHorizonText.setText(ResultFormatting.time(set));

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

        nextFullText.setText(p.next_fullmoon != null ? sdf.format(p.next_fullmoon) : "--");
        nextSuperText.setText(p.next_supermoon != null ? sdf.format(p.next_supermoon) : "--");

        renderTimeline(view, R.id.timeline_rises, p.moon_sunsire);
        renderTimeline(view, R.id.timeline_sets, p.moon_sunset);
    }

    private void renderTimeline(View view, int id, List<Position> positions) {
        RecyclerView list = view.findViewById(id);
        boolean visible = positions.size() > 1;
        list.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            list.setLayoutManager(new LinearLayoutManager(requireContext()));
            list.setAdapter(new TimelineAdapter(positions));
        } else {
            list.setAdapter(null);
        }
    }
}
