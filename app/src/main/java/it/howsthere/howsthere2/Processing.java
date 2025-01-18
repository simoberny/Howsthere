package it.howsthere.howsthere2;

import android.content.Context;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.objects.Position;
import it.howsthere.howsthere2.objects.TimezoneMapper;

public class Processing  {
    Context context;
    String peak = "";
    String namePeak = "";
    Panorama panorama;

    public Processing(Panorama pan_) {
        panorama = pan_;
    }

    public Processing(Context context_, String peak_, String namePeak_, Panorama pan_){
        peak = peak_;
        namePeak = namePeak_;
        panorama = pan_;

        context = context_;
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

        return panorama;
    }

    // Calculate Sun trajectory position every 5 minutes
    private void generateSunData() {
        int indexSun = 0;
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

                panorama.sun_data[indexSun] =
                        new Position(hour, min, position.getAltitude(), position.getAzimuth());

                indexSun++;
            }
        }

        // Ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne SOLE
        boolean prevSun = false;
        boolean isAbove = false;
        panorama.sun_minutes = 0;

        for(int i = 0; i < Constants.SUN_SAMPLE; i++) {
            isAbove = abovePeaks(i);

            if(isAbove) panorama.sun_minutes += 1;

            // Sunrise
            if(!prevSun && isAbove){
                panorama.sunrise.add(panorama.sun_data[i]);
            }

            // Sunset
            if(prevSun && !isAbove){
                panorama.sunset.add(panorama.sun_data[i]);
            }

            prevSun = isAbove;
        }
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

                    panorama.moon_data[indexMoon] =
                            new Position(hour, min, position.getAltitude(), position.getAzimuth());

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
                panorama.moon_sunsire.add(panorama.moon_data[i]);

            //tramonto
            if(prevMoon && !isMoonAbove)
                panorama.moon_sunset.add(panorama.moon_data[i]);

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
    private boolean abovePeaks(int i){
        int j = 0;

        // Allineamento sole montagne
        for(int c = 0; c < 360 &&
                !(panorama.peaks_data[0][c] >= panorama.sun_data[i].azimuth); c++) {
            j = c;
        }

        if(j == 359) {
            // Azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (panorama.sun_data[i].height >
                    ((panorama.peaks_data[2][259] + panorama.peaks_data[2][0]) / 2)) {
                return true;
            }
        }else{
            // Azimuth intermedio
            if (panorama.sun_data[i].height >
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
                !(panorama.peaks_data[0][c] >= panorama.moon_data[i].azimuth); c++){
            j = c;
        }

        if(j==359){
            // Azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (panorama.moon_data[i].height >
                    ((panorama.peaks_data[2][259] + panorama.peaks_data[2][0]) / 2)) {
                return true;
            }
        }else{
            // Azimuth intermedio
            if (panorama.moon_data[i].height >
                    ((panorama.peaks_data[2][j] + panorama.peaks_data[2][j+1]) / 2)) {
                return true;
            }
        }

        return false;
    }

    private void clearPanorama() {
        panorama.sunrise.clear();
        panorama.sunset.clear();

        panorama.moon_sunset.clear();
        panorama.moon_sunsire.clear();

        panorama.sun_minutes = 0;
        panorama.moon_minutes = 0;
    }
}
