package it.howsthere.howsthere2.ui.result;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import it.howsthere.howsthere2.objects.Position;

/** Shared display formats for the Sun and Moon details. */
public final class ResultFormatting {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private ResultFormatting() { }

    public static String time(Position position) {
        return position == null ? "--" : position.hour + ":" + twoDigits(position.minutes);
    }

    public static String time(ZonedDateTime time) {
        return time == null ? "--" : time.format(TIME);
    }

    public static String duration(long minutes) {
        return minutes / 60 + "h:" + twoDigits(minutes % 60) + "min";
    }

    private static String twoDigits(long value) {
        return value < 10 ? "0" + value : Long.toString(value);
    }
}
