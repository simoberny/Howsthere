package it.bobbyfriends.howsthere;

import android.os.AsyncTask;
import android.os.Build;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import it.bobbyfriends.howsthere.objects.Panorama;
import it.bobbyfriends.howsthere.objects.PanoramaStorage;
import it.bobbyfriends.howsthere.objects.Peak;
import it.bobbyfriends.howsthere.objects.Position;

public class Calc extends AsyncTask<Void, Void, Void> {
    String peak = "";
    String namePeak = "";
    Panorama processing_pan;

    public Calc(String peak, String namePeak, Panorama pan){
        this.peak = peak;
        this.namePeak = namePeak;
        this.processing_pan = pan;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        this.calc(peak, namePeak);
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        //showResult();

        // TODO Show result
    }

    public void calc(String peak, String namePeak){
        SunTimes s = SunTimes.compute()
                .on(processing_pan.data)
                .at(processing_pan.lat, processing_pan.lon)
                .execute();

        processing_pan.albaNoMontagne = s.getRise();
        processing_pan.tramontoNoMontagne = s.getSet();

        // Calculate sun every 5 minutes
        int indexSole = 0;
        for (int ora = 0; ora<24; ora++) {
            for (int min = 0; min < 60; min+=5) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(processing_pan.data);
                calendar.set(Calendar.HOUR_OF_DAY, ora);
                calendar.set(Calendar.MINUTE, min);

                SunPosition position = SunPosition.compute()
                        .on(calendar.getTime())       // set a date
                        .at(processing_pan.lat, processing_pan.lon)   // set a location
                        .execute();     // get the results

                processing_pan.risultatiSole[indexSole] = new Position();
                processing_pan.risultatiSole[indexSole].ora = ora;
                processing_pan.risultatiSole[indexSole].minuto = min;
                processing_pan.risultatiSole[indexSole].altezza = position.getAltitude();
                processing_pan.risultatiSole[indexSole].azimuth = position.getAzimuth();

                indexSole++;
            }
        }

        // Calculate moon every 5 minutes
        int indexLuna = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(processing_pan.data);
        calendar.add(Calendar.DATE, -1);

        for(int giorno = 0; giorno<3; giorno++) {
            calendar.add(Calendar.DATE, +1);

            for (int ora = 0; ora < 24; ora++) {
                for (int min = 0; min < 60; min += 5) {
                    calendar.set(Calendar.HOUR_OF_DAY, ora);
                    calendar.set(Calendar.MINUTE, min);

                    MoonPosition position = MoonPosition.compute()
                            .on(calendar.getTime()) //set a date
                            .at(processing_pan.lat, processing_pan.lon) //set a location
                            .execute(); //get the results

                    MoonIllumination m = MoonIllumination.compute().on(processing_pan.data).execute();
                    processing_pan.percentualeLuna = m.getFraction() * 100;
                    processing_pan.faseLuna = m.getPhase();

                    MoonTimes m1 = MoonTimes.compute().on(processing_pan.data).execute();
                    processing_pan.albaLunaNoMontagne = m1.getRise();
                    processing_pan.tramontoLunaNoMontagne = m1.getSet();

                    processing_pan.risultatiLuna[indexLuna] = new Position(ora, min, position.getAltitude(), position.getAzimuth());

                    indexLuna++;
                }
            }
        }

        // Parsing name
        List<String> linee_nomi = Arrays.asList(namePeak.split("[\\r\\n]+"));
        for(int a = 0; a < linee_nomi.size(); a++){
            List<String> tempsplit = Arrays.asList(linee_nomi.get(a).split(" "));

            if(tempsplit.size() >= 5){
                List<String> sublist = tempsplit.subList(4, tempsplit.size());

                StringBuilder b = new StringBuilder();
                for(int j = 0; j < sublist.size(); j++){
                    b.append(String.valueOf(sublist.get(j)));
                    b.append(" ");
                }

                Peak temp = new Peak(b.toString(), Double.parseDouble(tempsplit.get(0)), Double.parseDouble(tempsplit.get(1)));
                processing_pan.nomiPeak.add(temp);
            }
        }

        //PARSING dati
        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));
        for(int a = 1; a< lines.size(); a++){
            List<String> tempsplit = Arrays.asList(lines.get(a).split(","));
            for (int i =0; i<7; i++) {
                processing_pan.risultatiMontagne[i][a-1] = Double.parseDouble(tempsplit.get(i));
            }
        }

        //Ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne SOLE
        boolean prevSole = false;
        processing_pan.minutiSole = 0;
        for(int i = 0; i<288; i++){

            boolean is_sopra = sopra(i);
            if (is_sopra) processing_pan.minutiSole+=5;
            if(!prevSole && is_sopra){ //alba
                processing_pan.albe.add(processing_pan.risultatiSole[i]);
            }
            if(prevSole && !is_sopra){ //alba
                processing_pan.tramonti.add(processing_pan.risultatiSole[i]);
            }
            prevSole=is_sopra;
        }

        //ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne LUNA
        boolean prevLuna = false;
        processing_pan.minutiLuna = 0;
        for(int i = 287; i<576; i++){ //cerco alba anche nei 5 minuti prima di mezzanotte per non escludere un alba esattamente a mezzanotte
            boolean is_sopra_luna = sopraLuna(i);
            if (is_sopra_luna) processing_pan.minutiLuna+=5;
            //alba (non calcolata se è già sorta dal giorno prima)
            if((!prevLuna && is_sopra_luna)&& i > 287) processing_pan.albeLuna.add(processing_pan.risultatiLuna[i]);
            //tramonto
            if(prevLuna && !is_sopra_luna) processing_pan.tramontiLuna.add(processing_pan.risultatiLuna[i]);

            prevLuna=is_sopra_luna;
        }

        // TODO Save panorama
    }

    /**
     *
     * @param i posizione i-esima del sole
     * @return se il sole è sopra o sotto il profilo
     *
     * Per la posizione i-esima del sole allineo con l' azimuth rispetto alle montagne e confronto l' altezza.
     * nota: ci sono 2 casi limite, uno è che il sole / luna abbia l' azimuth iniziale più basso di tutti i punti del profilo montagne e l' altro è che lo abbia maggiore di tutte le montagne.
     *
     */
    private boolean sopra(int i){
        //todo confronto fra il primo e l' ultimo (0-360)
        int j = 0;
        for(int c = 0; c<360 && !(processing_pan.risultatiMontagne[0][c]>=processing_pan.risultatiSole[i].azimuth); c++){ //allineamento sole montagne
            j = c;
        }

        if(j==359){ //azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (processing_pan.risultatiSole[i].altezza > ((processing_pan.risultatiMontagne[2][259] + processing_pan.risultatiMontagne[2][0])/2)) {
                return true;
            }
        }else{  //azimuth intermedio
            if (processing_pan.risultatiSole[i].altezza > ((processing_pan.risultatiMontagne[2][j] + processing_pan.risultatiMontagne[2][j+1])/2)) {
                return true;
            }
        }
        return false;
    }

    private boolean sopraLuna(int i){
        int j = 0;
        for(int c = 0; c<360 && !(processing_pan.risultatiMontagne[0][c] >= processing_pan.risultatiLuna[i].azimuth);c++){ //allineamento Luna montagne
            j = c;
        }

        if(j==359){ //azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (processing_pan.risultatiLuna[i].altezza > ((processing_pan.risultatiMontagne[2][259] + processing_pan.risultatiMontagne[2][0])/2)) {
                return true;
            }
        }else{ //azimuth intermedio
            if (processing_pan.risultatiLuna[i].altezza > ((processing_pan.risultatiMontagne[2][j] + processing_pan.risultatiMontagne[2][j+1])/2)) {
                return true;
            }
        }
        return false;
    }

    public class Conteggio{
        private long count;

        public Conteggio(long count){
            this.count = count;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}


