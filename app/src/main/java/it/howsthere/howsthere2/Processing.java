package it.howsthere.howsthere2;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.objects.Peak;
import it.howsthere.howsthere2.objects.Position;
import it.howsthere.howsthere2.objects.TimezoneMapper;

public class Processing {
    private final String peak;
    private final String namePeak;
    private final Panorama panorama;

    public Processing(Panorama panorama) {
        this("", "", panorama);
    }

    public Processing(String peak, String namePeak, Panorama panorama) {
        this.peak = peak;
        this.namePeak = namePeak;
        this.panorama = panorama;
    }

    public void execute() {
        panorama.tz = TimezoneMapper.latLngToTimezoneString(panorama.lat, panorama.lon);

        panorama.peaks_data = HorizonData.parseProfile(peak);
        clearPanorama();
        panorama.peaks_name = new ArrayList<>(Collections.nCopies(360, null));

        generateSunData();
        generateMoonData();

        parsePeakNames();

        PanoramaStorage.getInstance().addPanorama(panorama);
    }

    public Panorama update(Date data) {
        panorama.date = data;
        clearPanorama();

        generateSunData();
        generateMoonData();

        if (namePeak != null && !namePeak.trim().isEmpty()) {
            parsePeakNames();
        }

        return panorama;
    }

    // Calculate Sun trajectory at the configured minute resolution.
    private void generateSunData() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(panorama.date);

        for (int hour = 0; hour < 24; hour++) {
            for (int min = 0; min < 60; min += Constants.RESOLUTION) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, min);

                SunPosition position = SunPosition.compute()
                        .on(calendar.getTime())             // set a date
                        .at(panorama.lat, panorama.lon)     // set a location
                        .timezone(panorama.tz)
                        .execute();                         // get the results

                panorama.sun_data.add(new Position(hour, min, position.getAltitude(), position.getAzimuth()));
            }
        }

        // Ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne SOLE
        panorama.sun_minutes = timeAbovePeaks(panorama.sun_data, panorama.sunrise, panorama.sunset);
    }

    public void generateYearData() {
        int year = Year.now().getValue();
        Calendar iterDays = Calendar.getInstance();
        iterDays.set(year, Calendar.JANUARY, 1);

        int minPeakMinutes = 1440;
        int maxPeakMinutes = 0;
        long minDayMillis = 1440 * 60 * 1000;
        long maxDayMillis = 0;

        Date shorter = null;
        Date longer = null;
        long shorter_minutes = 0;
        long longer_minutes = 0;

        while (true) {
            SunTimes s = SunTimes.compute()
                    .on(iterDays.get(Calendar.YEAR), iterDays.get(Calendar.MONTH) + 1, iterDays.get(Calendar.DAY_OF_MONTH))
                    .at(panorama.lat, panorama.lon)
                    .timezone(panorama.tz)
                    .execute();

            if (iterDays.get(Calendar.YEAR) > year) {
                break;      // we've reached the next year
            }

            ZonedDateTime rise = s.getRise();
            ZonedDateTime set = s.getSet();

            if(rise != null && set != null) {
                Duration duration = Duration.between(rise, set);
                long millis = duration.toMillis();

                if(millis < minDayMillis) {
                    shorter = iterDays.getTime();
                    shorter_minutes = millis / (1000 * 60);
                    minDayMillis = millis;
                }

                if(millis > maxDayMillis) {
                    longer = iterDays.getTime();
                    longer_minutes = millis / (1000 * 60);
                    maxDayMillis = millis;
                }
            }

            int minutes = getRealDayLength(iterDays);

            if(minutes < minPeakMinutes) {
                panorama.shortest_peak = iterDays.getTime();
                panorama.shortest_peak_minutes = minutes;
                minPeakMinutes = minutes;
            }

            if(minutes > maxPeakMinutes) {
                panorama.longest_peak = iterDays.getTime();
                panorama.longest_peak_minutes = minutes;
                maxPeakMinutes = minutes;
            }

            iterDays.add(Calendar.DAY_OF_MONTH, 1);
        }

        panorama.shortest_day = shorter;
        panorama.longest_day = longer;
        panorama.shortest_minutes = shorter_minutes;
        panorama.longest_minutes = longer_minutes;

        panorama.processedYearData = true;
    }

    // Parsing peak's name
    private void parsePeakNames() {
        if (namePeak == null || namePeak.trim().isEmpty()) {
            return;
        }

        if (panorama.peaks_name == null) {
            panorama.peaks_name = new ArrayList<>(Collections.nCopies(360, null));
        }

        List<String> namesList = Arrays.asList(namePeak.split("[\\r\\n]+"));

        for(int a = 0; a < namesList.size(); a++){
            String line = namesList.get(a).trim();
            if (line.isEmpty()) continue;

            List<String> tempsplit = Arrays.asList(line.split("\\s+"));

            if(tempsplit.size() >= 5){
                try {
                    double aziDouble = Double.parseDouble(tempsplit.get(0));
                    double height = Double.parseDouble(tempsplit.get(1));
                    if (!Double.isFinite(aziDouble) || !Double.isFinite(height)) continue;
                    int azi = ((int) Math.round(aziDouble)) % 360;
                    if (azi < 0) azi += 360;

                    List<String> sublist = tempsplit.subList(4, tempsplit.size());
                    String name = String.join(" ", sublist).trim();

                    Peak temp = new Peak(name, aziDouble, height);

                    if (azi < panorama.peaks_name.size()) {
                        panorama.peaks_name.set(azi, temp);
                    }
                } catch (NumberFormatException ignored) {
                    // Line does not contain valid numeric coordinates (e.g. header or invalid format)
                }
            }
        }
    }

    private int getRealDayLength(Calendar day) {
        List<Position> day_temp = new ArrayList<>(Constants.SUN_SAMPLE);
        Calendar hourIter = (Calendar) day.clone();

        for (int hour = 0; hour < 24; hour++) {
            for (int min = 0; min < 60; min += Constants.RESOLUTION) {
                SunPosition position = SunPosition.compute()
                        .on(hourIter.get(Calendar.YEAR), hourIter.get(Calendar.MONTH) + 1, hourIter.get(Calendar.DAY_OF_MONTH), hour, min, 0)          // set a date
                        .at(panorama.lat, panorama.lon)     // set a location
                        .timezone(panorama.tz)
                        .execute();                         // get the results

                day_temp.add(new Position(hour, min, position.getAltitude(), position.getAzimuth()));
            }
        }

        return timeAbovePeaks(day_temp, null, null);
    }

    private int timeAbovePeaks(List<Position> sun, List<Position> sunrise_list, List<Position> sunset_list) {
        boolean prevSun = false;
        boolean isAbove;

        int sun_minutes = 0;

        for(int i = 0; i < sun.size(); i++) {
            isAbove = HorizonData.isAbove(panorama.peaks_data, sun.get(i));

            if(isAbove) sun_minutes += Constants.RESOLUTION;

            // Sunrise
            if(sunrise_list != null && !prevSun && isAbove){
                sunrise_list.add(sun.get(i));
            }

            // Sunset
            if(sunset_list != null && prevSun && !isAbove){
                sunset_list.add(sun.get(i));
            }

            prevSun = isAbove;
        }

        return sun_minutes;
    }

    // Calculate Moon trajectory position every 5 minutes
    private void generateMoonData() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(panorama.date);
        calendar.add(Calendar.DATE, -1);

        for(int day = 0; day < 3; day++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int min = 0; min < 60; min += Constants.MOON_RESOLUTION) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, min);

                    MoonPosition position = MoonPosition.compute()
                            .on(calendar.getTime()) //set a date
                            .at(panorama.lat, panorama.lon) //set a location
                            .timezone(panorama.tz)
                            .execute(); //get the results

                    panorama.moon_data.add(new Position(hour, min, position.getAltitude(), position.getAzimuth()));
                }
            }

            calendar.add(Calendar.DATE, +1);
        }

        // Find moon phase
        MoonIllumination mIll = MoonIllumination.compute().on(panorama.date).execute();
        MoonPhase.Parameters parameters = MoonPhase.compute()
                .phase(MoonPhase.Phase.FULL_MOON);

        panorama.moon_perc = mIll.getFraction() * 100;
        panorama.moon_phase = mIll.getPhase();

        LocalDate iterDate = panorama.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int baseYear = iterDate.getYear();

        while (true) {
            MoonPhase moonPhase = parameters
                    .on(iterDate)
                    .execute();
            ZonedDateTime nextFullMoon = moonPhase
                    .getTime();

            if (nextFullMoon.getYear() > baseYear + 1) {
                break;      // search up to the next year
            }

            if(panorama.next_fullmoon == null)
                panorama.next_fullmoon = Date.from(nextFullMoon.toInstant());

            if (moonPhase.isSuperMoon()) {
                panorama.next_supermoon = Date.from(nextFullMoon.toInstant());
                break;
            }

            iterDate = nextFullMoon.toLocalDate().plusDays(1);
        }

        // Ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne LUNA
        boolean prevMoon = false;
        boolean isMoonAbove = false;
        panorama.moon_minutes = 0;

        for(int i = Constants.MOON_SAMPLES_PER_DAY - 1; i < 2 * Constants.MOON_SAMPLES_PER_DAY; i++) {
            // Cerco alba anche nei 5 minuti prima di mezzanotte per non escludere un alba esattamente a mezzanotte
            isMoonAbove = HorizonData.isAbove(panorama.peaks_data, panorama.moon_data.get(i));

            if (isMoonAbove) panorama.moon_minutes += Constants.MOON_RESOLUTION;

            //alba (non calcolata se è già sorta dal giorno prima)
            if((!prevMoon && isMoonAbove) && i >= Constants.MOON_SAMPLES_PER_DAY)
                panorama.moon_sunsire.add(panorama.moon_data.get(i));

            //tramonto
            if(prevMoon && !isMoonAbove)
                panorama.moon_sunset.add(panorama.moon_data.get(i));

            prevMoon = isMoonAbove;
        }
    }

    private void clearPanorama() {
        panorama.sun_data.clear();
        panorama.moon_data.clear();

        panorama.sunrise.clear();
        panorama.sunset.clear();

        panorama.moon_sunset.clear();
        panorama.moon_sunsire.clear();

        panorama.next_fullmoon = null;
        panorama.next_supermoon = null;
        panorama.sun_minutes = 0;
        panorama.moon_minutes = 0;
    }
}
