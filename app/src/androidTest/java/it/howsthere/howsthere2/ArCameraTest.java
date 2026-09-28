package it.howsthere.howsthere2;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.camera.view.PreviewView;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.slider.Slider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.objects.Peak;
import it.howsthere.howsthere2.objects.Position;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/** Run only in the isolated .artest build; uses a synthetic skyline, never the user's history. */
@RunWith(AndroidJUnit4.class)
public class ArCameraTest {
    @Test public void sunAndMoonCameraOpenAlignRotateAndClose() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assumeTrue(context.getPackageName().endsWith(".artest"));
        try (ActivityScenario<MainActivity> main = ActivityScenario.launch(MainActivity.class)) {
            main.onActivity(activity -> PanoramaStorage.getInstance().addPanorama(fixture()));
            Intent intent = new Intent(context, Result.class).putExtra("id", "ar-camera-test");
            try (ActivityScenario<Result> result = ActivityScenario.launch(intent)) {
                onView(withId(R.id.open_ar)).perform(click());
                awaitCamera();
                onView(withId(R.id.ar_align)).perform(click());
                onView(withId(R.id.ar_heading)).check(matches(isDisplayed()));
                onView(withId(R.id.ar_heading)).check((view, error) -> ((Slider) view).setValue(7));
                result.recreate();
                awaitCamera();
                onView(withId(R.id.ar_heading)).check((view, error) ->
                        assertEquals(7f, ((Slider) view).getValue(), 0.01f));
                result.onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
                SystemClock.sleep(1000);
                awaitCamera();
                result.onActivity(activity -> assertEquals(Configuration.ORIENTATION_LANDSCAPE,
                        activity.getResources().getConfiguration().orientation));
                onView(withId(R.id.ar_heading)).check((view, error) ->
                        assertEquals(7f, ((Slider) view).getValue(), 0.01f));
                onView(withId(R.id.ar_close)).perform(click());
                result.onActivity(activity -> Navigation.findNavController(activity, R.id.nav_host_activity_result)
                        .navigate(R.id.navigation_moon));
                onView(withId(R.id.open_ar)).perform(click());
                awaitCamera();
                onView(withId(R.id.ar_trajectory_legend)).check((view, error) ->
                        assertEquals(context.getString(R.string.ar_moon_legend),
                                ((android.widget.TextView) view).getText().toString()));
                onView(withId(R.id.ar_close)).perform(click());
                onView(withId(R.id.open_ar)).check(matches(isDisplayed()));
            }
        }
    }

    private void awaitCamera() {
        AtomicBoolean streaming = new AtomicBoolean();
        long deadline = SystemClock.elapsedRealtime() + 15000;
        while (!streaming.get() && SystemClock.elapsedRealtime() < deadline) {
            onView(withId(R.id.ar_camera)).check((view, error) -> {
                if (error != null) throw error;
                PreviewView preview = (PreviewView) view;
                streaming.set(preview.getPreviewStreamState().getValue() == PreviewView.StreamState.STREAMING
                        && preview.getSensorToViewTransform() != null);
            });
            if (!streaming.get()) SystemClock.sleep(200);
        }
        assertTrue("Camera should stream with a valid sensor-to-screen transform", streaming.get());
    }

    private Panorama fixture() {
        Panorama panorama = new Panorama();
        panorama.id = "ar-camera-test";
        panorama.city = "AR test panorama";
        panorama.lat = 46;
        panorama.lon = 11;
        panorama.tz = "Europe/Rome";
        panorama.processedYearData = true;
        for (int i = 0; i < 360; i++) {
            panorama.peaks_data[0][i] = i;
            panorama.peaks_data[2][i] = 8 + 4 * Math.sin(Math.toRadians(i * 3));
            if (i % 30 == 0) panorama.peaks_name.set(i, new Peak("Peak " + i, (double) i, 2000.0));
        }
        for (int i = 0; i < Constants.SUN_SAMPLE; i++) {
            panorama.sun_data.add(new Position(i / 60, i % 60, 30 * Math.sin(i * Math.PI / 720), i / 4.0));
        }
        for (int i = 0; i < 3 * Constants.MOON_SAMPLES_PER_DAY; i++) {
            int minute = i % Constants.MOON_SAMPLES_PER_DAY * Constants.MOON_RESOLUTION;
            panorama.moon_data.add(new Position(minute / 60, minute % 60, 20, minute / 4.0));
        }
        return panorama;
    }
}
