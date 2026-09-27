package it.howsthere.howsthere2.ui;

import org.junit.Test;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import static org.junit.Assert.*;

public class CalendarSelectionTest {
    @Test public void keepsCalendarDayAcrossTimeZonesAndDaylightSaving() {
        LocalDate day = LocalDate.of(2026, 3, 29);
        for (String zoneName : new String[]{"Europe/Rome", "America/Los_Angeles", "Pacific/Auckland"}) {
            ZoneId zone = ZoneId.of(zoneName);
            Date input = Date.from(day.atTime(23, 30).atZone(zone).toInstant());
            long selected = CalendarSelection.toPicker(input, zone);
            assertEquals(day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), selected);
            assertEquals(day, CalendarSelection.fromPicker(selected, zone).toInstant().atZone(zone).toLocalDate());
        }
    }
}
