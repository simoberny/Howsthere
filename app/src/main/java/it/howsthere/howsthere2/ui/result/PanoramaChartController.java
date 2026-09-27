package it.howsthere.howsthere2.ui.result;

import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.Peak;
import it.howsthere.howsthere2.objects.Position;

/** Shared rendering and peak selection for the sun and moon panoramas. */
final class PanoramaChartController {
    private final LineChart chart;
    private final Panorama p;
    private final TextView peaksCount;
    private final List<Position> positions;
    private final int trajectoryLabel;
    private final int trajectoryColor;
    private final boolean showAllPeakNames;
    private RotatedLineChartRenderer peakRenderer;
    private Peak highestPeak;

    PanoramaChartController(LineChart chart, Panorama panorama, TextView selectedPeak,
                            List<Position> positions, int trajectoryLabel, int trajectoryColor,
                            boolean showAllPeakNames) {
        this.chart = chart;
        this.p = panorama;
        this.peaksCount = selectedPeak;
        this.positions = positions;
        this.trajectoryLabel = trajectoryLabel;
        this.trajectoryColor = trajectoryColor;
        this.showAllPeakNames = showAllPeakNames;
    }

    private void selectPeak(Peak peak) {
        peakRenderer.setSelectedPeak(peak);
        showSelectedPeakInfo(peak);
        chart.invalidate();
    }

    void render() {
        configureChart();
        if (chart instanceof PanoramaLineChart && chart.getParent() instanceof View) {
            ((PanoramaLineChart) chart).setDirectionStrip(
                    ((View) chart.getParent()).findViewById(R.id.direction_strip));
        }
        LineDataSet mountains = createMountainData();
        LineDataSet sun = createTrajectoryData();
        LineDataSet markers = createPeakMarkers();
        LineData data = new LineData();
        data.addDataSet(mountains);
        if (markers.getEntryCount() > 0) data.addDataSet(markers);
        data.addDataSet(sun);

        highestPeak = null;
        float highestAltitude = -Float.MAX_VALUE;
        for (Entry entry : markers.getValues()) {
            if (entry.getY() > highestAltitude) {
                highestAltitude = entry.getY();
                highestPeak = (Peak) entry.getData();
            }
        }
        peakRenderer = new RotatedLineChartRenderer(chart, chart.getAnimator(), chart.getViewPortHandler(), sun);
        peakRenderer.setShowAllPeakNames(showAllPeakNames);
        peakRenderer.setHighestPeak(highestPeak);
        peakRenderer.setSelectedPeak(highestPeak);
        chart.setRenderer(peakRenderer);
        chart.setData(data);
        chart.highlightValue(null);
        showSelectedPeakInfo(highestPeak);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                if (entry.getData() instanceof Peak) selectPeak((Peak) entry.getData());
            }

            @Override
            public void onNothingSelected() {
                selectPeak(highestPeak);
            }
        });
        chart.animateX(1000);
        chart.invalidate();
    }

    private void configureChart() {
        chart.setBackgroundColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_background));
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setSpaceTop(10f);
        chart.getAxisRight().setAxisMinimum(0);
        chart.setPadding(0, 0, 0, 0);
        chart.setViewPortOffsets(Utils.convertDpToPixel(10f), Utils.convertDpToPixel(24f),
                Utils.convertDpToPixel(10f), Utils.convertDpToPixel(8f));
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setMaxVisibleValueCount(Integer.MAX_VALUE);
        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setHighlightPerDragEnabled(true);

        chart.getLegend().setEnabled(false);
    }

    private LineDataSet createMountainData() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < p.peaks_data[0].length; i++) {
            entries.add(new Entry((float) p.peaks_data[0][i], (float) p.peaks_data[2][i]));
        }
        LineDataSet data = new LineDataSet(entries, chart.getContext().getString(R.string.mountain));
        data.setMode(LineDataSet.Mode.LINEAR);
        data.setColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_onBackground));
        data.setDrawCircles(false);
        data.setDrawValues(false);
        data.setHighlightEnabled(false);
        data.setDrawFilled(true);
        data.setFillDrawable(ContextCompat.getDrawable(chart.getContext(), R.drawable.fade_mountains));
        data.setLineWidth(1.5f);
        return data;
    }

    private LineDataSet createTrajectoryData() {
        List<Entry> entries = new ArrayList<>();
        for (Position position : positions) {
            if (position.minutes == 0) {
                entries.add(new Entry((float) position.azimuth,
                        (float) Math.max(-20, position.height), position.hour + ":00"));
            }
        }
        // MPAndroidChart requires ascending X values, also when the trajectory crosses north.
        entries.sort(java.util.Comparator.comparingDouble(Entry::getX));
        LineDataSet data = new LineDataSet(entries, chart.getContext().getString(trajectoryLabel));
        int color = ContextCompat.getColor(chart.getContext(), trajectoryColor);
        data.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        data.setColor(color);
        data.setLineWidth(3f);
        data.setCircleRadius(4f);
        data.setCircleColor(color);
        data.setCircleHoleColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_background));
        data.setDrawCircleHole(true);
        data.setDrawCircles(true);
        data.setDrawValues(true);
        data.setHighlightEnabled(false);
        data.setValueTextColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_onBackground));
        data.setValueTextSize(9f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return (String) entry.getData();
            }
        });
        return data;
    }

    private LineDataSet createPeakMarkers() {
        List<Entry> entries = new ArrayList<>();
        if (p.peaks_name != null) {
            for (Peak peak : p.peaks_name) {
                if (peak == null || peak.getName() == null || peak.getName().trim().isEmpty()) continue;
                int azimuth = Math.floorMod((int) Math.round(peak.getAzimuth()), 360);
                entries.add(new Entry((float) peak.getAzimuth(), (float) p.peaks_data[2][azimuth], peak));
            }
        }
        entries.sort(java.util.Comparator.comparingDouble(Entry::getX));
        LineDataSet data = new LineDataSet(entries, "markers");
        data.setDrawValues(false); // RotatedLineChartRenderer draws and spaces the names.
        data.setDrawCircles(true);
        data.setCircleRadius(3f);
        data.setDrawCircleHole(true);
        data.setCircleHoleColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_background));
        data.setCircleColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_onBackground));
        data.setColor(android.graphics.Color.TRANSPARENT);
        data.setHighlightEnabled(true);
        data.setDrawHighlightIndicators(true);
        data.setHighLightColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_secondary));
        data.setHighlightLineWidth(1f);
        data.enableDashedHighlightLine(6f, 4f, 0f);
        return data;
    }

    private void showSelectedPeakInfo(Peak peak) {
        if (peak == null) {
            peaksCount.setText(R.string.no_named_peaks);
            peaksCount.setVisibility(View.VISIBLE);
            return;
        }
        // derive elevation from peaks_data: last column (index 6)
        int azi = (int) Math.round(peak.getAzimuth()) % 360;
        if (azi < 0) azi += 360;
        String elev = "";
        try {
            double elevation = p.peaks_data[6][azi];
            elev = String.format("%d m", (int) Math.round(elevation));
        } catch (Exception ignored) {}

        String text = peak.getName() + (elev.isEmpty() ? "" : (" — " + elev));
        if (peak == highestPeak) text = chart.getContext().getString(R.string.highest_profile_peak, text);
        if (chart.getResources().getConfiguration().orientation
                == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            text = text.replace('\n', ' ');
        }
        peaksCount.setText(text);
        peaksCount.setTextColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_secondary));
        peaksCount.setVisibility(View.VISIBLE);
    }

}
