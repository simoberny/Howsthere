package it.howsthere.howsthere2.ui.result;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.github.mikephil.charting.charts.LineChart;

/** Keeps horizontal exploration in the chart while allowing vertical page scrolling. */
public class PanoramaLineChart extends LineChart {
    private DirectionStrip directionStrip;

    void setDirectionStrip(DirectionStrip strip) {
        directionStrip = strip;
        if (strip != null) strip.bind(this);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);
        if (directionStrip != null) directionStrip.invalidate();
    }

    private final int touchSlop;
    private float startX;
    private float startY;
    private boolean chartGesture;
    private boolean pageGesture;

    public PanoramaLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                chartGesture = false;
                pageGesture = false;
                chartGesture = getResources().getConfiguration().orientation
                        == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                chartGesture = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!chartGesture && !pageGesture) {
                    float dx = Math.abs(event.getX() - startX);
                    float dy = Math.abs(event.getY() - startY);
                    if (Math.max(dx, dy) > touchSlop) {
                        chartGesture = dx >= dy;
                        pageGesture = !chartGesture;
                        if (pageGesture) {
                            MotionEvent cancel = MotionEvent.obtain(event);
                            cancel.setAction(MotionEvent.ACTION_CANCEL);
                            super.onTouchEvent(cancel);
                            cancel.recycle();
                        }
                    }
                }
                break;
        }
        boolean handled = pageGesture || super.onTouchEvent(event);
        boolean finished = event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL;
        getParent().requestDisallowInterceptTouchEvent(!finished && !pageGesture);
        return handled;
    }
}
