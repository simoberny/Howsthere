package it.howsthere.howsthere2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

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
import java.util.Date;
import java.util.List;

import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.objects.Peak;
import it.howsthere.howsthere2.objects.Position;
import it.howsthere.howsthere2.objects.TimezoneMapper;
import it.howsthere.howsthere2.ui.result.ResultViewModel;

public class Processing  {
    private Context context;
    private String peak = "";
    private String namePeak = "";
    private Panorama panorama;
    private ResultViewModel vm;

    public Processing(Panorama pan_) {
        panorama = pan_;
    }

    public Processing(Context context_, String peak_, String namePeak_, Panorama pan_){
        peak = peak_;
        namePeak = namePeak_;
        panorama = pan_;

        context = context_;

        vm = new ViewModelProvider((ViewModelStoreOwner) context_).get(ResultViewModel.class);
    }

    public void execute() {
        panorama.tz = TimezoneMapper.latLngToTimezoneString(panorama.lat, panorama.lon);

        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));
        for(int a = 1; a < lines.size(); a++) {
            List<String> tempSplit = Arrays.asList(lines.get(a).split(","));

            for (int i = 0; i < 7; i++) {
                panorama.peaks_data[i][a-1] = Double.parseDouble(tempSplit.get(i));
            }
        }

        generateSunData();
        generateMoonData();

        PanoramaStorage.getInstance().addPanorama(panorama);
    }

    public Panorama update(Date data) {
        panorama.date = data;
        clearPanorama();

        generateSunData();
        generateMoonData();

        parsingPeakName();

        return panorama;
    }

    // Calculate Sun trajectory position every 5 minutes
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
    private void parsingPeakName() {
        List<String> namesList = Arrays.asList(namePeak.split("[\\r\\n]+"));

        for(int a = 0; a < namesList.size(); a++){
            List<String> tempsplit = Arrays.asList(namesList.get(a).split(" "));

            if(tempsplit.size() >= 5){
                List<String> sublist = tempsplit.subList(4, tempsplit.size());

                StringBuilder b = new StringBuilder();
                for(int j = 0; j < sublist.size(); j++){
                    b.append(String.valueOf(sublist.get(j)));
                    b.append(" ");
                }

                int azi = Integer.parseInt(tempsplit.get(0));
                Peak temp = new Peak(b.toString(),
                        Double.parseDouble(tempsplit.get(0)),
                        Double.parseDouble(tempsplit.get(1)));

                System.out.println("PEAKSS: " + temp);

                panorama.peaks_name.set(azi, temp);
            }
        }
    }

    private int getRealDayLength(Calendar day) {
        List<Position> day_temp = new ArrayList<Position>(Constants.SUN_SAMPLE);
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

        for(int i = 0; i < Constants.SUN_SAMPLE; i++) {
            isAbove = abovePeaks(sun, i);

            if(isAbove) sun_minutes += 1;

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
        int indexMoon = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(panorama.date);
        calendar.add(Calendar.DATE, -1);

        for(int day = 0; day < 3; day++) {
            for (int hour = 0; hour < 24; hour++) {
                for (int min = 0; min < 60; min += 5) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, min);

                    MoonPosition position = MoonPosition.compute()
                            .on(calendar.getTime()) //set a date
                            .at(panorama.lat, panorama.lon) //set a location
                            .timezone(panorama.tz)
                            .execute(); //get the results

                    panorama.moon_data.add(new Position(hour, min, position.getAltitude(), position.getAzimuth()));

                    indexMoon++;
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
        int year = Year.now().getValue();

        while (true) {
            MoonPhase moonPhase = parameters
                    .on(iterDate)
                    .execute();
            ZonedDateTime nextFullMoon = moonPhase
                    .getTime();

            if (nextFullMoon.getYear() > year) {
                break;      // we've reached the next year
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

        for(int i = 287; i < 576; i++) {
            // Cerco alba anche nei 5 minuti prima di mezzanotte per non escludere un alba esattamente a mezzanotte
            isMoonAbove = abovePeaksMoon(i);

            if (isMoonAbove) panorama.moon_minutes += 5;

            //alba (non calcolata se è già sorta dal giorno prima)
            if((!prevMoon && isMoonAbove) && i > 287)
                panorama.moon_sunsire.add(panorama.moon_data.get(i));

            //tramonto
            if(prevMoon && !isMoonAbove)
                panorama.moon_sunset.add(panorama.moon_data.get(i));

            prevMoon = isMoonAbove;
        }
    }

    /**
     *
     * @param i posizione i-esima del sole
     * @return se il sole è sopra o sotto il profilo
     *
     * Per la posizione i-esima del sole allineo con l' azimuth rispetto alle montagne e confronto l' altezza.
     * nota: ci sono 2 casi limite, uno è che il sole / luna abbia l' azimuth iniziale più basso di tutti i punti
     * del profilo montagne e l' altro è che lo abbia maggiore di tutte le montagne.
     *
     */
    private boolean abovePeaks(List<Position> sun, int i){
        int j = findClosestAzimuthIndex(sun.get(i).azimuth);

        if(j == 359) {
            // Azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (sun.get(i).height >
                    ((panorama.peaks_data[2][259] + panorama.peaks_data[2][0]) / 2)) {
                return true;
            }
        }else{
            // Azimuth intermedio
            if (sun.get(i).height >
                    ((panorama.peaks_data[2][j] + panorama.peaks_data[2][j+1]) / 2)) {
                return true;
            }
        }

        return false;
    }

    private boolean abovePeaksMoon(int i){
        int j = 0;

        // Allineamento Luna montagne
        for(int c = 0; c < 360 &&
                !(panorama.peaks_data[0][c] >= panorama.moon_data.get(i).azimuth); c++){
            j = c;
        }

        if(j==359){
            // Azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (panorama.moon_data.get(i).height >
                    ((panorama.peaks_data[2][259] + panorama.peaks_data[2][0]) / 2)) {
                return true;
            }
        }else{
            // Azimuth intermedio
            if (panorama.moon_data.get(i).height >
                    ((panorama.peaks_data[2][j] + panorama.peaks_data[2][j+1]) / 2)) {
                return true;
            }
        }

        return false;
    }

    private int findClosestAzimuthIndex(double azimuth) {
        // Usa ricerca binaria per trovare l'indice
        int left = 0, right = panorama.peaks_data[0].length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (panorama.peaks_data[0][mid] < azimuth) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return Math.max(0, left - 1);
    }

    public Panorama getPanorama() {
        return panorama;
    }

    private void clearPanorama() {
        panorama.sun_data.clear();

        panorama.sunrise.clear();
        panorama.sunset.clear();

        panorama.moon_sunset.clear();
        panorama.moon_sunsire.clear();

        panorama.sun_minutes = 0;
        panorama.moon_minutes = 0;
    }
}
