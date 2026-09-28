package it.howsthere.howsthere2;

import android.content.Context;
import android.widget.FrameLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import it.howsthere.howsthere2.ui.SystemInsets;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class EdgeToEdgeTest {
    @Test public void safeAreasIncludeKeyboardAndDoNotAccumulate() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout root = new FrameLayout(context);
            root.setPadding(2, 3, 4, 5);
            SystemInsets.apply(root);
            WindowInsetsCompat insets = new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 24, 0, 48))
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(32, 0, 0, 0))
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300)).build();
            for (int i = 0; i < 2; i++) {
                assertTrue(ViewCompat.dispatchApplyWindowInsets(root, insets).isConsumed());
                assertEquals(34, root.getPaddingLeft());
                assertEquals(27, root.getPaddingTop());
                assertEquals(4, root.getPaddingRight());
                assertEquals(305, root.getPaddingBottom());
            }
            ViewCompat.dispatchApplyWindowInsets(root, new WindowInsetsCompat.Builder().build());
            assertEquals(2, root.getPaddingLeft());
            assertEquals(3, root.getPaddingTop());
            assertEquals(5, root.getPaddingBottom());
        });
    }

    @Test public void searchOpensAndClosesWithoutLegacyWidget() {
        assumeTrue(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getPackageName().endsWith(".artest"));
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.autocomplete_search)).perform(click());
            onView(withId(R.id.place_query)).check(matches(isDisplayed()));
            onView(withId(R.id.places_attribution)).check(matches(isDisplayed()));
            scenario.recreate();
            onView(withId(R.id.place_query)).check(matches(isDisplayed()));
            onView(withId(R.id.place_search_close)).perform(click());
            onView(withId(R.id.autocomplete_search)).check(matches(isDisplayed()));
        }
    }
}
