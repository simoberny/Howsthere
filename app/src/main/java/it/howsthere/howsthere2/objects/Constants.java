package it.howsthere.howsthere2.objects;

public final class Constants {
    private Constants() { }
    // Minutes granularity of Sun position
    public static final int RESOLUTION = 1;

    public static final int MOON_RESOLUTION = 5;
    public static final int MOON_SAMPLES_PER_DAY = 24 * 60 / MOON_RESOLUTION;

    // Numbers of sun position
    public static final int SUN_SAMPLE = 24 * 60 / RESOLUTION;
}