package it.howsthere.howsthere2.ui.result;

import org.junit.Test;
import java.time.ZonedDateTime;
import it.howsthere.howsthere2.objects.Position;
import static org.junit.Assert.assertEquals;

public class ResultFormattingTest {
    @Test public void formatsMidnightAndMinutes() {
        assertEquals("0:05", ResultFormatting.time(new Position(0, 5, 0, 0)));
        assertEquals("23:59", ResultFormatting.time(new Position(23, 59, 0, 0)));
        assertEquals("--", ResultFormatting.time((Position) null));
        assertEquals("--", ResultFormatting.time((ZonedDateTime) null));
        assertEquals("06:05", ResultFormatting.time(ZonedDateTime.parse("2026-06-01T06:05:00+02:00")));
    }

    @Test public void keepsFullDayDurations() {
        assertEquals("0h:00min", ResultFormatting.duration(0));
        assertEquals("1h:05min", ResultFormatting.duration(65));
        assertEquals("12h:59min", ResultFormatting.duration(779));
        assertEquals("24h:00min", ResultFormatting.duration(1440));
    }
}
