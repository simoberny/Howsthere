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
    private Retrofit retrofit = null;
    private ProgressDialog progressDialog;
    private Integer n_tentativi = 4; //+1 iniziale
    private Integer richiestaID = 0;
    private Integer richiestaStato = 0;
    private Integer richiestaDatiMontagne = 0;
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
            Toast.makeText(getApplicationContext(), "Data non selezionata",
                    Toast.LENGTH_LONG).show();
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
            panorama.ID = savedID;
            gPeak = savedPeak;
            setPeak(savedPeak);
        }else{
            callsAPI(panorama.lat, panorama.lon);
        }
    }

    /**
     * Salvataggio dell'istanza
     */
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("ID", panorama.ID);
        outState.putCharSequence("peak", gPeak);
    }


    /**
     * Interfacce per le richieste GET
     */
    public interface HeyWhatsID { // http://www.heywhatsthat.com/api/query?src=hows&lat=45.627107&lon= 9.315373
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
                            //System.out.print("ID: " + panorama.ID);
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
                            System.out.println("id vuoto: Le posizioni consentite includono latitudini da 60N fino a 54S (disponibile anche parte dell' alaska). Il mare è disponibile solo vicino alle coste.");
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
                        System.out.println("errore generico nella lettura id");
                        e.printStackTrace();
                        richiediID();

                    }
                } else {
                    System.out.println("risposta dal sito fallita");
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
        if (richiestaID<n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            callsAPI(panorama.lat, panorama.lon);
            richiestaID++;
            progressDialog.setMessage("Richiesta id panorama, tentativo n°: " + (richiestaID+1));

        }else{
            System.out.println("ID non ottenuto, controllare la connessione");
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
                        e.printStackTrace();
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
                Log.d("Errore: ", t.getMessage());
                richiediStato();
            }
        });
    }

    private void richiediStato(){
        if (richiestaStato<n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            checkStatus();
            richiestaStato++;
            progressDialog.setMessage("controllo se è pronto il panorama, tentativo n°: " + (richiestaStato+1));
        }else{
            System.out.println("panorama non pronto, controllare la connessione");
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
                        setPeak(gPeak);
                    } catch (IOException e) {
                        richiediDatiMontagne();
                        e.printStackTrace();
                    }
                } else {
                    richiediDatiMontagne();
                }
                //progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Error", t.getMessage());
                richiediDatiMontagne();

            }
        });
    }
    private void richiediDatiMontagne(){
        if (richiestaDatiMontagne<n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            richiestaDatiMontagne++;
            progressDialog.setMessage("Controllo se è pronto il panorama, tentativo n°: " + (richiestaDatiMontagne+1));
            loadPeakData();
        }else{
            System.out.println("Panorama non pronto, controllare la connessione");
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

    private void setPeak(String peak){

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
                //System.out.println("ora: " + ora + " Elevazione: " + position.getAltitude() + "Azimuth: " + position.getAzimuth()+'\n');
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
                            .on(calendar.getTime())       // set a date
                            .at(panorama.lat, panorama.lon)   // set a location
                            .execute();     // get the results

                    //luna ieri
                    //calendar.add(Calendar.DATE, -1);
                    //luna domani
                    //MoonTimes m = MoonTimes.compute().on(panorama.data).execute();
                    MoonIllumination m = MoonIllumination.compute().on(panorama.data).execute();
                    //System.out.println("frazione: "+ m.getFraction()+" fase: "+m.getPhase());
                    panorama.percentualeLuna = m.getFraction()*100;
                    panorama.faseLuna = m.getPhase();
                    MoonTimes m1 = MoonTimes.compute().on(panorama.data).execute();
                    panorama.albaLunaNoMontagne = m1.getRise();
                    panorama.tramontoLunaNoMontagne = m1.getSet();
                    System.out.println("informazioni Luna: " + m1.toString());
                    //System.out.println(m.toString());
                    //System.out.println("ora: " + ora + " Elevazione: " + position.getAltitude() + "Azimuth: " + position.getAzimuth()+'\n');
                    panorama.risultatiLuna[indexLuna] = new Posizione();
                    panorama.risultatiLuna[indexLuna].ora = ora;
                    panorama.risultatiLuna[indexLuna].minuto = min;
                    panorama.risultatiLuna[indexLuna].altezza = position.getAltitude();
                    panorama.risultatiLuna[indexLuna].azimuth = position.getAzimuth();
                    indexLuna++;
                }
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

        //ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne SOLE
        boolean prevSole = false;
        panorama.minutiSole = 0;
        for(int i = 0; i<288; i++){
            boolean sopra = sopra(i);
            if (sopra) panorama.minutiSole+=5;
            //System.out.println("ora: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto +" sopra? " + sopra);
            if(!prevSole && sopra){ //alba
                panorama.albe.add(panorama.risultatiSole[i]);
                //System.out.println("alba: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto );
                //Toast.makeText(getApplicationContext(),"alba: " + panorama.risultatiSole[i].ora + ":"+ panorama.risultatiSole[i].minuto, Toast.LENGTH_LONG).show();
            }
            if(prevSole && !sopra){ //alba
                panorama.tramonti.add(panorama.risultatiSole[i]);
                //System.out.println("tramonto: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto );
                //Toast.makeText(getApplicationContext(),"tramonto: " + panorama.risultatiSole[i].ora + ":"+ panorama.risultatiSole[i].minuto , Toast.LENGTH_LONG).show();
            }
            prevSole=sopra;
        }

        //ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne LUNA
        boolean prevLuna = false;
        panorama.minutiLuna = 0;
        for(int i = 288; i<576; i++){
            boolean sopraLuna = sopraLuna(i);
            if (sopraLuna) panorama.minutiLuna+=5;
            if(!prevLuna && sopraLuna){ //alba
                panorama.albeLuna.add(panorama.risultatiLuna[i]);
            }
            if(prevLuna && !sopraLuna){ //alba
                panorama.tramontiLuna.add(panorama.risultatiLuna[i]);
            }
            prevLuna=sopraLuna;
        }

        progressDialog.dismiss();
        progressDialog.setMessage("Calcolo posizione pianeti...");
        progressDialog.setTitle("Salvo i dati...");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        //salvo i dati del panorama con panoramiStorage
        PanoramiStorage p = PanoramiStorage.panorami_storage;
        p.addPanorama(panorama);

        //chiamo la classe Risultati
        Intent i = new Intent(this,RisultatiActivity.class);
        i.putExtra("ID", panorama.ID);
        startActivity(i);
        progressDialog.dismiss();
        finish();
    }
    private boolean sopra(int i){
        //todo confronto fra il primo e l' ultimo (0-360)
        //per la posizione i-esima del sole allineo con l' azimuth rispetto alle montagne e confronto l' altezza.
        //nota: ci sono 2 casi limite, uno è che il sole / luna abbia l' azimuth iniziale più basso di tutti i punti del profilo montagne e l' altro è che lo abbia maggiore di tutte le montagne.
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