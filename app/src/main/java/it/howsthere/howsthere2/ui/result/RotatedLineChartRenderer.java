package it.howsthere.howsthere2.ui.result;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.objects.Peak;

public class RotatedLineChartRenderer extends LineChartRenderer {
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint anchorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int textColor;
    private final int selectedColor;
    private Peak selectedPeak;
    private Peak highestPeak;
    private boolean showAllPeakNames = true;
    private final LineChartRenderer trajectoryRenderer;

    public RotatedLineChartRenderer(LineChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler, ILineDataSet trajectoryData) {
        super(chart, animator, viewPortHandler);
        trajectoryRenderer = new LineChartRenderer(chart, animator, viewPortHandler) {
            @Override
            protected void drawDataSet(Canvas canvas, ILineDataSet dataSet) {
                if (dataSet == trajectoryData) super.drawDataSet(canvas, dataSet);
            }
        };
        labelPaint.setTextSize(11f * chart.getResources().getDisplayMetrics().scaledDensity);
        labelPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textColor = ContextCompat.getColor(chart.getContext(), R.color.md_theme_onBackground);
        selectedColor = ContextCompat.getColor(chart.getContext(), R.color.md_theme_secondary);
        backgroundPaint.setColor(ContextCompat.getColor(chart.getContext(), R.color.md_theme_background));
        anchorPaint.setStrokeWidth(Utils.convertDpToPixel(1f));
    }

    public void setShowAllPeakNames(boolean showAllPeakNames) {
        this.showAllPeakNames = showAllPeakNames;
    }

    @Override
    public void drawValue(Canvas canvas, String text, float x, float y, int color) {
        super.drawValue(canvas, text, x, y - Utils.convertDpToPixel(8f), color);
    }

    public void setHighestPeak(Peak peak) {
        highestPeak = peak;
    }

    public void setSelectedPeak(Peak peak) {
        selectedPeak = peak;
    }

    @Override
    public void drawValues(Canvas c) {
        drawPeakLabels(c);
        // Restore the trajectory above label backgrounds without changing its coordinates.
        int saved = c.save();
        c.clipRect(mViewPortHandler.getContentRect());
        trajectoryRenderer.drawData(c);
        super.drawExtras(c);
        drawHighestPeak(c);
        // Draw the library crosshair above label backgrounds and the foreground trajectory.
        if (mChart instanceof LineChart) {
            LineChart chart = (LineChart) mChart;
            if (chart.valuesToHighlight()) super.drawHighlighted(c, chart.getHighlighted());
        }
        c.restoreToCount(saved);
        super.drawValues(c);
    }

    @Override
    public void releaseBitmap() {
        super.releaseBitmap();
        trajectoryRenderer.releaseBitmap();
    }

    private void drawHighestPeak(Canvas canvas) {
        if (highestPeak == null || mChart.getLineData() == null) return;
        for (ILineDataSet dataSet : mChart.getLineData().getDataSets()) {
            if (!"markers".equals(dataSet.getLabel())) continue;
            mXBounds.set(mChart, dataSet);
            for (int i = mXBounds.min; i <= mXBounds.min + mXBounds.range; i++) {
                Entry entry = dataSet.getEntryForIndex(i);
                if (entry.getData() != highestPeak) continue;
                float[] point = {entry.getX(), entry.getY() * mAnimator.getPhaseY()};
                mChart.getTransformer(dataSet.getAxisDependency()).pointValuesToPixel(point);
                anchorPaint.setColor(selectedColor);
                anchorPaint.setStyle(Paint.Style.STROKE);
                anchorPaint.setStrokeWidth(Utils.convertDpToPixel(2f));
                canvas.drawCircle(point[0], point[1], Utils.convertDpToPixel(7f), anchorPaint);
                anchorPaint.setStyle(Paint.Style.FILL);
                anchorPaint.setStrokeWidth(Utils.convertDpToPixel(1f));
                return;
            }
        }
    }

    private void drawPeakLabels(Canvas c) {
        LineData lineData = mChart.getLineData();
        if (lineData == null) return;

        ILineDataSet terrain = lineData.getDataSetByIndex(0);
        float[] profile = new float[terrain.getEntryCount() * 2];
        for (int i = 0; i < terrain.getEntryCount(); i++) {
            Entry point = terrain.getEntryForIndex(i);
            profile[2 * i] = point.getX();
            profile[2 * i + 1] = point.getY() * mAnimator.getPhaseY();
        }
        mChart.getTransformer(terrain.getAxisDependency()).pointValuesToPixel(profile);
        List<Label> candidates = new ArrayList<>();
        float[] point = new float[2];
        for (int i = 0; i < lineData.getDataSetCount(); i++) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(i);
            if (dataSet == null) continue;
            String label = dataSet.getLabel();
            if (label == null) continue;
            if (!label.equals("markers") || !dataSet.isVisible()) continue;

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mXBounds.set(mChart, dataSet);

            for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {
                Entry entry = dataSet.getEntryForIndex(j);
                if (entry == null || !(entry.getData() instanceof Peak)) continue;
                if (!showAllPeakNames && entry.getData() != highestPeak) continue;
                point[0] = entry.getX();
                point[1] = entry.getY() * mAnimator.getPhaseY();
                trans.pointValuesToPixel(point);
                if (!Float.isFinite(point[0]) || !Float.isFinite(point[1])
                        || !mViewPortHandler.isInBoundsX(point[0])
                        || !mViewPortHandler.isInBoundsY(point[1])) continue;
                candidates.add(new Label(entry, point[0], point[1]));
            }
        }

        // Apparent angular height prioritizes peaks in this panorama, not absolute altitude.
        // The selected peak always wins. Azimuth breaks ties deterministically.
        candidates.sort((a, b) -> {
            int selected = Boolean.compare(b.peak == selectedPeak, a.peak == selectedPeak);
            if (selected != 0) return selected;
            int highest = Boolean.compare(b.peak == highestPeak, a.peak == highestPeak);
            if (highest != 0) return highest;
            int height = Float.compare(b.entry.getY(), a.entry.getY());
            return height != 0 ? height : Float.compare(a.entry.getX(), b.entry.getX());
        });

        float padding = Utils.convertDpToPixel(3f);
        float gap = Utils.convertDpToPixel(10f);
        Paint.FontMetrics metrics = labelPaint.getFontMetrics();
        float width = metrics.descent - metrics.ascent + padding * 2;
        float top = mViewPortHandler.contentTop() + padding;
        float bottom = mViewPortHandler.contentBottom() - padding;
        if (bottom - top < labelPaint.getTextSize() * 2
                || mViewPortHandler.contentWidth() < width) return;

        List<Float> occupiedColumns = new ArrayList<>();
        for (Label candidate : candidates) {
            float x = Math.max(mViewPortHandler.contentLeft() + width / 2,
                    Math.min(candidate.x, mViewPortHandler.contentRight() - width / 2));
            boolean overlaps = false;
            for (float occupied : occupiedColumns) {
                if (Math.abs(x - occupied) < width + gap) {
                    overlaps = true;
                    break;
                }
            }
            if (overlaps) continue;
            String name = candidate.peak.getName();
            if (name == null || name.trim().isEmpty()) continue;
            if (candidate.peak == highestPeak) name = "★ " + name;
            float labelBottom = Math.min(bottom, PeakLabelSpace.bottom(candidate.y, profile,
                    x - width / 2, x + width / 2, gap));
            float available = labelBottom - top - padding * 2;
            // Never push a label down across the skyline to make it fit.
            // The selected peak's full name remains in the header when space is too tight.
            if (available < labelPaint.measureText("M…")) continue;
            String text = TextUtils.ellipsize(name.trim(), labelPaint,
                    available, TextUtils.TruncateAt.END).toString();
            float length = labelPaint.measureText(text) + padding * 2;
            boolean isSelected = candidate.peak == selectedPeak;
            int color = isSelected || candidate.peak == highestPeak ? selectedColor : textColor;
            labelPaint.setColor(color);
            anchorPaint.setColor(color);
            c.drawLine(candidate.x, candidate.y, x, labelBottom, anchorPaint);
            c.drawCircle(candidate.x, candidate.y,
                    Utils.convertDpToPixel(isSelected ? 5f : 2.5f), anchorPaint);
            c.save();
            c.translate(x, labelBottom);
            c.rotate(-90f);
            c.drawRoundRect(0, -width / 2, length, width / 2, padding, padding, backgroundPaint);
            c.drawText(text, padding, -(metrics.ascent + metrics.descent) / 2, labelPaint);
            c.restore();
            occupiedColumns.add(x);
        }
    }

    private static class Label {
        final Entry entry;
        final Peak peak;
        final float x;
        final float y;

        Label(Entry entry, float x, float y) {
            this.entry = entry;
            this.peak = (Peak) entry.getData();
            this.x = x;
            this.y = y;
        }
    }
}
