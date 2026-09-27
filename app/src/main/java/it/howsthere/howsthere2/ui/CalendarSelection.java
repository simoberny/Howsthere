package it.howsthere.howsthere2.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/** MaterialDatePicker represents calendar dates at UTC midnight, not local instants. */
public final class CalendarSelection {
    private CalendarSelection() { }

    public static long toPicker(Date date, ZoneId zone) {
        return date.toInstant().atZone(zone).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static Date fromPicker(long selection, ZoneId zone) {
        return Date.from(Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()
                .atStartOfDay(zone).toInstant());
    }
}
