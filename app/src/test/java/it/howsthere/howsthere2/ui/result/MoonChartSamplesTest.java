package it.howsthere.howsthere2.ui.result;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import it.howsthere.howsthere2.objects.Position;

public class MoonChartSamplesTest {
    @Test public void selects24HoursFromMiddleDayOfFiveMinuteSamples() {
        List<Position> samples = new ArrayList<>();
        for (int day = 0; day < 3; day++) {
            for (int minute = 0; minute < 1440; minute += 5) {
                samples.add(new Position(minute / 60, minute % 60, day, minute / 4.0));
            }
        }
        List<Position> selected = MoonChartSamples.selectedDay(samples);
        assertEquals(288, selected.size());
        assertSame(samples.get(288), selected.get(0));
        assertSame(samples.get(575), selected.get(287));
        assertEquals(24, selected.stream().filter(p -> p.minutes == 0).count());
        assertTrue(selected.stream().allMatch(p -> p.height == 1));
    }

    @Test public void handlesMissingSamplesWithoutIndexErrors() {
        assertTrue(MoonChartSamples.selectedDay(new ArrayList<>()).isEmpty());
    }
}
