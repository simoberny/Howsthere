package it.howsthere.howsthere2.ui.result;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import it.howsthere.howsthere2.R;

/** Bearings use the chart transform, so they remain aligned during zoom and pan. */
public class DirectionStrip extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] point = new float[2];
    private final float density;
    private final float textScale;
    private final String[] directions;
    private LineChart chart;

    public DirectionStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        textScale = getResources().getDisplayMetrics().scaledDensity;
        directions = new String[]{context.getString(R.string.bearing_north),
                context.getString(R.string.bearing_east), context.getString(R.string.bearing_south),
                context.getString(R.string.bearing_west), context.getString(R.string.bearing_north)};
        setContentDescription(context.getString(R.string.bearing_description));
    }

    void bind(LineChart chart) {
        this.chart = chart;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (chart == null || chart.getData() == null) return;
        paint.setColor(ContextCompat.getColor(getContext(), R.color.md_theme_surfaceContainerLow));
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 12 * density, 12 * density, paint);
        paint.setStrokeWidth(density);
        for (int bearing = 0; bearing <= 360; bearing += 15) {
            point[0] = bearing;
            point[1] = 0;
            chart.getTransformer(YAxis.AxisDependency.LEFT).pointValuesToPixel(point);
            float x = point[0];
            if (x < -density || x > getWidth() + density) continue;
            boolean cardinal = bearing % 90 == 0;
            boolean north = bearing == 0 || bearing == 360;
            paint.setColor(ContextCompat.getColor(getContext(), north ? R.color.md_theme_primary
                    : R.color.md_theme_outlineVariant));
            canvas.drawLine(x, 0, x, (cardinal ? 7 : 4) * density, paint);
            if (!cardinal) continue;
            paint.setColor(ContextCompat.getColor(getContext(), north ? R.color.md_theme_primary
                    : R.color.md_theme_onSurfaceVariant));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            paint.setTextSize(12 * textScale);
            String label = directions[bearing / 90];
            float half = Math.max(paint.measureText(label) / 2, 14 * density);
            float labelX = Math.max(half, Math.min(x, getWidth() - half));
            canvas.drawText(label, labelX, 22 * density, paint);
            paint.setFakeBoldText(false);
            paint.setTextSize(9 * textScale);
            canvas.drawText(bearing + "°", labelX, 35 * density, paint);
        }
    }
}
