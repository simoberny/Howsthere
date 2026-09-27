package it.howsthere.howsthere2.ui.result;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.camera.view.PreviewView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.Peak;
import it.howsthere.howsthere2.objects.Position;

/** Transparent angular overlay; CameraX supplies preview rotation and crop transforms. */
public class ArOverlayView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final float[] rotation = new float[9];
    private final float[] point = new float[2];
    private final List<RectF> labels = new ArrayList<>();
    private final float density;
    private PreviewView preview;
    private Panorama panorama;
    private List<Position> trajectory;
    private boolean oriented;
    private boolean cameraReady;
    private int sensorOrientation;
    private int trajectoryColor;
    private double fx, fy, cx, cy, declination;
    private double headingOffset, altitudeOffset, scale = 1;
    private float topInset, bottomInset;

    public ArOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setPanorama(Panorama panorama, boolean moon) {
        this.panorama = panorama;
        trajectory = moon ? MoonChartSamples.selectedDay(panorama.moon_data) : panorama.sun_data;
        trajectoryColor = moon ? Color.rgb(215, 202, 255) : Color.rgb(255, 212, 90);
        invalidate();
    }

    void setCamera(PreviewView preview, int orientation, double fx, double fy, double cx, double cy) {
        this.preview = preview;
        sensorOrientation = orientation;
        this.fx = fx;
        this.fy = fy;
        this.cx = cx;
        this.cy = cy;
        invalidate();
    }

    void setCameraReady(boolean ready) {
        cameraReady = ready;
        invalidate();
    }

    void clearOrientation() {
        oriented = false;
        invalidate();
    }

    void setOrientation(float[] matrix, double declination) {
        System.arraycopy(matrix, 0, rotation, 0, 9);
        this.declination = declination;
        oriented = true;
        postInvalidateOnAnimation();
    }

    void setLabelInsets(float top, float bottom) {
        topInset = top;
        bottomInset = bottom;
    }

    void setAlignment(float heading, float altitude, float scale) {
        headingOffset = heading;
        altitudeOffset = altitude;
        this.scale = scale;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!cameraReady || !oriented || panorama == null || preview == null) return;
        Matrix transform = preview.getSensorToViewTransform();
        if (transform == null) return;
        labels.clear();
        drawProfile(canvas, transform);
        drawTrajectory(canvas, transform);
        drawPeaks(canvas, transform);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(dp(1));
        float x = getWidth() / 2f, y = getHeight() / 2f;
        canvas.drawLine(x - dp(10), y, x + dp(10), y, paint);
        canvas.drawLine(x, y - dp(10), x, y + dp(10), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(dp(13));
        paint.setShadowLayer(dp(3), 0, 0, Color.BLACK);
        String bearing = String.format(Locale.getDefault(), "%.0f°", ArProjection.bearing(rotation, declination));
        canvas.drawText(bearing, x + dp(16), y + dp(5), paint);
        paint.clearShadowLayer();
    }

    private boolean project(double azimuth, double altitude, Matrix transform) {
        if (!ArProjection.project(azimuth, altitude, rotation, declination, headingOffset,
                altitudeOffset, sensorOrientation, fx, fy, cx, cy, scale, point)) return false;
        transform.mapPoints(point);
        // Break paths behind the camera and far outside the frustum, including the north seam.
        return point[0] > -getWidth() && point[0] < 2 * getWidth()
                && point[1] > -getHeight() && point[1] < 2 * getHeight();
    }

    private void drawProfile(Canvas canvas, Matrix transform) {
        double[][] profile = panorama.peaks_data;
        if (profile == null || profile.length < 3 || profile[0].length == 0) return;
        path.reset();
        boolean previous = false;
        for (int i = 0; i <= profile[0].length; i++) {
            int index = i % profile[0].length;
            boolean visible = project(profile[0][index], profile[2][index], transform);
            if (visible) {
                if (previous) path.lineTo(point[0], point[1]);
                else path.moveTo(point[0], point[1]);
            }
            previous = visible;
        }
        stroke(canvas, Color.rgb(100, 240, 228));
    }

    private void drawTrajectory(Canvas canvas, Matrix transform) {
        path.reset();
        boolean previous = false;
        for (Position position : trajectory) {
            boolean visible = project(position.azimuth, position.height, transform);
            if (visible) {
                if (previous) path.lineTo(point[0], point[1]);
                else path.moveTo(point[0], point[1]);
            }
            previous = visible;
        }
        stroke(canvas, trajectoryColor);
        for (Position position : trajectory) {
            if (position.minutes != 0 || !project(position.azimuth, position.height, transform)) continue;
            dot(canvas, point[0], point[1], trajectoryColor);
            label(canvas, position.hour + ":00", point[0], point[1] - dp(17), trajectoryColor);
        }
    }

    private void drawPeaks(Canvas canvas, Matrix transform) {
        if (panorama.peaks_name == null) return;
        int count = 0;
        for (Peak peak : panorama.peaks_name) {
            if (peak == null) continue;
            int index = Math.floorMod((int) Math.round(peak.getAzimuth()), panorama.peaks_data[0].length);
            if (!project(peak.getAzimuth(), panorama.peaks_data[2][index], transform)) continue;
            dot(canvas, point[0], point[1], Color.WHITE);
            if (count < 4 && label(canvas, peak.getName(), point[0], point[1] + dp(25), Color.WHITE)) count++;
        }
    }

    private void stroke(Canvas canvas, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xB0000000);
        paint.setStrokeWidth(dp(5));
        canvas.drawPath(path, paint);
        paint.setColor(color);
        paint.setStrokeWidth(dp(2.5f));
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void dot(Canvas canvas, float x, float y, int color) {
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x, y, dp(4), paint);
        paint.setColor(color);
        canvas.drawCircle(x, y, dp(2.5f), paint);
    }

    private boolean label(Canvas canvas, String text, float x, float baseline, int color) {
        if (text == null) return false;
        paint.setTextSize(dp(13));
        float width = paint.measureText(text);
        float left = x - width / 2;
        RectF rect = new RectF(left - dp(5), baseline - dp(16), left + width + dp(5), baseline + dp(5));
        if (rect.left < dp(8) || rect.right > getWidth() - dp(8)
                || rect.top < topInset || rect.bottom > getHeight() - bottomInset) return false;
        for (RectF used : labels) if (RectF.intersects(used, rect)) return false;
        labels.add(rect);
        paint.setColor(0xB0000000);
        canvas.drawRoundRect(rect, dp(5), dp(5), paint);
        paint.setColor(color);
        canvas.drawText(text, left, baseline, paint);
        return true;
    }

    private float dp(float value) { return value * density; }
}
