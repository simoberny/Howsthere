package it.unitn.simob.howsthere;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.imagepipeline.common.SourceUriType;
import com.twitter.sdk.android.core.models.TwitterCollection;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;
import org.shredzone.commons.suncalc.util.Moon;
import org.shredzone.commons.suncalc.util.Sun;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.Oggetti.Peak;
import it.unitn.simob.howsthere.Oggetti.Posizione;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Data extends AppCompatActivity {
    private String gPeak = null;
    private String gNamePeak = null;
    private Retrofit retrofit = null;
    private ProgressDialog progressDialog;
    private final Integer n_tentativi = 4; //+1 iniziale
    private Integer richiestaID = 0;
    private Integer richiestaStato = 0;
    private Integer richiestaDatiMontagne = 0;
    private Integer richiestaNomiMontagne = 0;
    public Panorama panorama = new Panorama();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Intent i = getIntent();
        panorama.lat = i.getDoubleExtra("lat", 0.0);
        panorama.lon = i.getDoubleExtra("long", 0.0);

        panorama.data.setTime(i.getLongExtra("data", 0));
        if(panorama.data.getTime() == 0){
            Toast.makeText(getApplicationContext(), "Data non selezionata", Toast.LENGTH_LONG).show();
        }

        panorama.citta = i.getStringExtra("citta");
        //Preparo la finestra di caricamento
        progressDialog = new ProgressDialog(Data.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);

        //Variabile Retrofit per gestire tutte le richieste GET
        retrofit = new Retrofit.Builder()
                .baseUrl("http://www.heywhatsthat.com/")
                .build();
        if (savedInstanceState != null) {
            // Se c'è un istanza salvata non richiedo nuovamente i dati
            String savedID = savedInstanceState.getString("ID");
            String savedPeak = savedInstanceState.getString("peak");
            String savedNamePeak = savedInstanceState.getString("namePeak");
            panorama.ID = savedID;
            gPeak = savedPeak;
            gNamePeak = savedNamePeak;
            setPeak(savedPeak, savedNamePeak);
        }else{
            callsAPI(panorama.lat, panorama.lon);
        }
    }

    /* Salvataggio dell'istanza */
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("ID", panorama.ID);
        outState.putCharSequence("peak", gPeak);
        outState.putCharSequence("namePeak", gNamePeak);
    }

    /* Interfacce per le richieste GET */
    public interface HeyWhatsID {
        @GET("api/query?src=hows")
        Call<ResponseBody> getID(@Query("lat") Double lat, @Query("lon") Double lon);
    }

    public interface HeyWhatsReady {
        @GET("api/ready?src=hows")
        Call<ResponseBody> getStatus(@Query("id") String ID);
    }

    public interface HeyWhatsPeak {
        @GET("api/horizon.csv?resolution=.999")
        Call<ResponseBody> getPeak(@Query("id") String ID);
    }

    public interface HeyWhatsNamePeak {
        @GET("api/horizon-peaks?src=hows")
        Call<ResponseBody> getNamePeak(@Query("id") String ID);
    }

    private void callsAPI(final Double lat, final Double lng){
        HeyWhatsID service = retrofit.create(HeyWhatsID.class);
        Call<ResponseBody> call = service.getID(lat, lng);
        progressDialog.setMessage("Generazione id panorama...");
        progressDialog.setTitle("Richiesta id panorama");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String id = response.body().string();
                        if(id != null && id != "") {
                            panorama.ID = id;
                            TextView tx = (TextView)findViewById(R.id.idCheck); //recupero e rendo visibile la conferma ricezione ID
                            tx.setVisibility(TextView.VISIBLE);
                            try {   //aspetto prima del primo tentativo, il server ci mette sempre almeno un secondo.
                                Thread.sleep(1000);//
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progressDialog.dismiss();
                            checkStatus(); //Ottenuto l'ID controllo lo stato della generazione del panorama
                        }else{
                            progressDialog.dismiss();
                            LinearLayout ln = (LinearLayout) findViewById(R.id.idErrLayout);
                            ln.setVisibility(TextView.VISIBLE);
                            TextView tx = (TextView)findViewById(R.id.idErrTitolo);
                            tx.setText("Posizione scelta non valida!");
                            TextView tx1 = (TextView)findViewById(R.id.idErrDescrizione);
                            tx1.setText("Le posizioni consentite includono latitudini da 60N fino a 54S (inclusa parte dell' alaska). Il mare è disponibile solo vicino alle coste.");
                            Button bt = (Button) findViewById(R.id.idErrButton);
                            bt.setVisibility(TextView.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        richiediID();
                    }
                } else {
                    richiediID();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
                {
                    richiediID();
                }
        });
    }

    private void richiediID(){
        if (richiestaID < n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            callsAPI(panorama.lat, panorama.lon);
            progressDialog.setMessage("Richiesta id panorama, tentativo n°: " + (richiestaID+1));
            richiestaID++;
        }else{ //ID non ottenuto
            LinearLayout ln = (LinearLayout) findViewById(R.id.idErrLayout);
            ln.setVisibility(TextView.VISIBLE);
            Snackbar.make(findViewById(R.id.dataContainerLayout), "ID non ottenuto, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    azzeraTentativiID();
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    public void azzeraTentativiID(View view) { //wrapper chiamato dal pulsante riprova
        azzeraTentativiID();
    }
    public void azzeraTentativiID(){
        LinearLayout ln = (LinearLayout) findViewById(R.id.idErrLayout); //recupero e rendo visibile la conferma ricezione ID
        ln.setVisibility(TextView.GONE);
        richiestaID = 0;
        callsAPI(panorama.lat, panorama.lon);
    }

    private void checkStatus(){
        progressDialog.setMessage("Aspettando il panorama...");
        progressDialog.setTitle("Attesa dati Panorama");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String status = response.body().string();
                        if(status.length() > 0 && status.charAt(0) == '1'){ // Se la mappa è pronta vado ad ottenere il panorama
                            progressDialog.dismiss();
                            TextView tx = (TextView)findViewById(R.id.panoramaCheck); //recupero e rendo visibile la conferma che è pronto il panorama
                            tx.setVisibility(TextView.VISIBLE);
                            loadPeakData();
                        }else{
                            richiediStato();
                        }
                    } catch (IOException e) {
                        richiediStato();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "La mappa non è stata generata!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    richiediStato();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                richiediStato();
            }
        });
    }

    private void richiediStato(){
        if (richiestaStato < n_tentativi){ //richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            checkStatus();
            richiestaStato++;
            progressDialog.setMessage("controllo se è pronto il panorama, tentativo n°: " + (richiestaStato+1));
        }else{
            LinearLayout ln = (LinearLayout) findViewById(R.id.prontoErrLayout);
            ln.setVisibility(TextView.VISIBLE);
            Snackbar.make(findViewById(R.id.dataContainerLayout), "panorama non pronto, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    azzeraTentativiStato();
                }
            }).show();
            progressDialog.dismiss();
        }
    }
    public void azzeraTentativiStato(View view) { //wrapper chiamato dal pulsante riprova
        azzeraTentativiStato();
    }
    public void azzeraTentativiStato(){
        LinearLayout ln = (LinearLayout) findViewById(R.id.prontoErrLayout);
        ln.setVisibility(TextView.GONE);
        richiestaStato = 0;
        checkStatus();
    }

    private void loadPeakData(){
        progressDialog.setMessage("Scarico dati Montagne...");
        progressDialog.setTitle("Scaricamento e Calcolo posizione pianeti");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        gPeak = response.body().string();
                        progressDialog.dismiss();
                        TextView tx = (TextView)findViewById(R.id.panoramaDownload); //recupero e rendo visibile la conferma scaricamento dati
                        tx.setVisibility(TextView.VISIBLE);
                        progressDialog.setMessage("Scarico nomi montagne...");
                        loadNamePeak();
                    } catch (IOException e) {
                        richiediDatiMontagne();
                    }
                } else {
                    richiediDatiMontagne();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                richiediDatiMontagne();
            }
        });
    }

    private void richiediDatiMontagne(){
        if (richiestaDatiMontagne < n_tentativi){ //richiedo l'id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            richiestaDatiMontagne++;
            progressDialog.setMessage("Controllo se è pronto il panorama, tentativo n°: " + (richiestaDatiMontagne+1));
            loadPeakData();
        }else{
            LinearLayout ln = (LinearLayout) findViewById(R.id.scaricoErrLayout);
            ln.setVisibility(TextView.VISIBLE);
            Snackbar.make(findViewById(R.id.dataContainerLayout), "Dati non ricevuti, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    azzeraTentativiScaricamentoPanorama();
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    public void azzeraTentativiScaricamentoPanorama(){
        LinearLayout ln = (LinearLayout) findViewById(R.id.scaricoErrLayout);
        ln.setVisibility(TextView.GONE);
        richiestaDatiMontagne = 0;
        loadPeakData();
    }

    public void azzeraTentativiScaricamentoPanorama(View view) { //wrapper chiamato dal pulsante riprova
        azzeraTentativiScaricamentoPanorama();
    }

    private void loadNamePeak(){
        progressDialog.setTitle("Scaricamento nomi montagne");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        HeyWhatsNamePeak service = retrofit.create(HeyWhatsNamePeak.class);
        Call<ResponseBody> call = service.getNamePeak(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        gNamePeak = response.body().string();
                        if(gNamePeak == null || gNamePeak == ""){
                            richiediNomiMontagne();
                        }else{
                            progressDialog.dismiss();
                            TextView tx = findViewById(R.id.panoramaName); //recupero e rendo visibile la conferma scaricamento dati
                            tx.setVisibility(TextView.VISIBLE);
                            setPeak(gPeak, gNamePeak);
                        }
                    } catch (IOException e) {
                        richiediNomiMontagne();
                    }
                } else {
                    richiediNomiMontagne();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                richiediNomiMontagne();
            }
        });
    }

    private void richiediNomiMontagne(){
        if (richiestaNomiMontagne < n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            richiestaNomiMontagne++;
            progressDialog.setMessage("Controllo se sono disponibili i nomi delle montagne, tentativo n°: " + (richiestaNomiMontagne+1));
            loadNamePeak();
        }else{
            LinearLayout ln = (LinearLayout) findViewById(R.id.scaricoNomiErrLayout);
            ln.setVisibility(TextView.VISIBLE);
            Snackbar.make(findViewById(R.id.dataContainerLayout), "Dati non ricevuti, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    azzeraTentativiNomiMontagne();
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    public void azzeraTentativiNomiMontagne(){
        LinearLayout ln = (LinearLayout) findViewById(R.id.scaricoNomiErrLayout);
        ln.setVisibility(TextView.GONE);
        richiestaNomiMontagne = 0;
        loadNamePeak();
    }
    public void azzeraTentativiNomiMontagne(View view) { //wrapper chiamato dal pulsante riprova
        azzeraTentativiNomiMontagne();
    }

    /**
     * Funzione per il salvataggio panorama e dati
     * @param peak profilo montagne
     * @param namePeak nome picchi più importanti
     */
    private void setPeak(String peak, String namePeak){
        progressDialog.setMessage("Calcolo posizione pianeti...");
        progressDialog.setTitle("Scaricamento e Calcolo posizione pianeti");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento

        //calcolo Alba e Tramonto senza montagne
        SunTimes s = SunTimes.compute()
                .on(panorama.data)       // set a date
                .at(panorama.lat, panorama.lon)   // set a location
                .execute();
        panorama.albaNoMontagne = s.getRise();
        panorama.tramontoNoMontagne = s.getSet();

        //CALCOLO SOLE ogni 5 min
        int indexSole = 0;
        for (int ora = 0; ora<24; ora++) {
            for (int min = 0; min < 60; min+=5) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(panorama.data);
                calendar.set(Calendar.HOUR_OF_DAY, ora);
                calendar.set(Calendar.MINUTE, min);
                SunPosition position = SunPosition.compute()
                        .on(calendar.getTime())       // set a date
                        .at(panorama.lat, panorama.lon)   // set a location
                        .execute();     // get the results

                panorama.risultatiSole[indexSole] = new Posizione();
                panorama.risultatiSole[indexSole].ora=ora;
                panorama.risultatiSole[indexSole].minuto=min;
                panorama.risultatiSole[indexSole].altezza=position.getAltitude();
                panorama.risultatiSole[indexSole].azimuth=position.getAzimuth();
                indexSole++;
            }
        }

        //CALCOLO LUNA ogni 5 min
        int indexLuna = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(panorama.data);
        calendar.add(Calendar.DATE, -1);
        for(int giorno = 0; giorno<3; giorno++) {
            calendar.add(Calendar.DATE, +1);
            for (int ora = 0; ora < 24; ora++) {
                for (int min = 0; min < 60; min += 5) {
                    calendar.set(Calendar.HOUR_OF_DAY, ora);
                    calendar.set(Calendar.MINUTE, min);
                    MoonPosition position = MoonPosition.compute()
                            .on(calendar.getTime()) //set a date
                            .at(panorama.lat, panorama.lon) //set a location
                            .execute(); //get the results

                    MoonIllumination m = MoonIllumination.compute().on(panorama.data).execute();
                    panorama.percentualeLuna = m.getFraction()*100;
                    panorama.faseLuna = m.getPhase();

                    MoonTimes m1 = MoonTimes.compute().on(panorama.data).execute();
                    panorama.albaLunaNoMontagne = m1.getRise();
                    panorama.tramontoLunaNoMontagne = m1.getSet();

                    panorama.risultatiLuna[indexLuna] = new Posizione(ora, min, position.getAltitude(), position.getAzimuth());

                    indexLuna++;
                }
            }
        }

        //PARSING nome
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
                panorama.nomiPeak.add(temp);
            }
        }

        //PARSING dati
        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));
        for(int a = 1; a< lines.size(); a++){
            List<String> tempsplit = Arrays.asList(lines.get(a).split(","));
            for (int i =0; i<7; i++) {
                panorama.risultatiMontagne[i][a-1] = Double.parseDouble(tempsplit.get(i));
            }
        }

        //Ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne SOLE
        boolean prevSole = false;
        panorama.minutiSole = 0;
        for(int i = 0; i<288; i++){
            boolean is_sopra = sopra(i);
            if (is_sopra) panorama.minutiSole+=5;
            if(!prevSole && is_sopra){ //alba
                panorama.albe.add(panorama.risultatiSole[i]);
            }
            if(prevSole && !is_sopra){ //alba
                panorama.tramonti.add(panorama.risultatiSole[i]);
            }
            prevSole=is_sopra;
        }

        //ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne LUNA
        boolean prevLuna = false;
        panorama.minutiLuna = 0;
        for(int i = 288; i<576; i++){
            boolean is_sopra_luna = sopraLuna(i);
            if (is_sopra_luna) panorama.minutiLuna+=5;
            //alba
            if(!prevLuna && is_sopra_luna) panorama.albeLuna.add(panorama.risultatiLuna[i]);
            //tramonto
            if(prevLuna && !is_sopra_luna) panorama.tramontiLuna.add(panorama.risultatiLuna[i]);

            prevLuna=is_sopra_luna;
        }

        progressDialog.setMessage("Calcolo posizione sole e luna...");
        progressDialog.setTitle("Salvo i dati...");
        progressDialog.show();

        PanoramiStorage p = PanoramiStorage.panorami_storage; //salvo i dati del panorama con panoramiStorage
        p.addPanorama(panorama);

        progressDialog.dismiss();

        Intent i = new Intent(this,RisultatiActivity.class); //chiamo la classe Risultati
        i.putExtra("ID", panorama.ID);
        i.putExtra("citta", panorama.citta);
        startActivity(i);
        //Finish così l'attività non resta nello stack e quando nei risultati si premerà indietro tornerà direttamente nella main page
        finish();
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
        for(int c = 0; c<360 && !(panorama.risultatiMontagne[0][c]>=panorama.risultatiSole[i].azimuth); c++){ //allineamento sole montagne
            j = c;
        }

        if(j==359){ //azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (panorama.risultatiSole[i].altezza > ((panorama.risultatiMontagne[2][259]+panorama.risultatiMontagne[2][0])/2)) {
                return true;
            }
        }else{  //azimuth intermedio
            if (panorama.risultatiSole[i].altezza > ((panorama.risultatiMontagne[2][j]+panorama.risultatiMontagne[2][j+1])/2)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param i posizione i-esima della luna
     * @return se la luna è sopra o sotto il profilo
     *
     */

    private boolean sopraLuna(int i){
        int j = 0;
        for(int c = 0; c<360 && !(panorama.risultatiMontagne[0][c]>=panorama.risultatiLuna[i].azimuth);c++){ //allineamento Luna montagne
            j = c;
        }

        if(j==359){ //azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (panorama.risultatiLuna[i].altezza > ((panorama.risultatiMontagne[2][259]+panorama.risultatiMontagne[2][0])/2)) {
                return true;
            }
        }else{ //azimuth intermedio
            if (panorama.risultatiLuna[i].altezza > ((panorama.risultatiMontagne[2][j]+panorama.risultatiMontagne[2][j+1])/2)) {
                return true;
            }
        }
        return false;
    }
}