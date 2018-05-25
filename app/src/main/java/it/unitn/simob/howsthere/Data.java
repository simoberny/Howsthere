package it.unitn.simob.howsthere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.shredzone.commons.suncalc.MoonPosition;
import org.shredzone.commons.suncalc.SunPosition;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Data extends AppCompatActivity {
    private String id = null;
    private String gPeak = null;
    private Retrofit retrofit = null;
    private TextView idt = null;
    private ProgressDialog progressDialog;
    private Date data;
    private Double lon,lat;
    private Integer n_tentativi = 5;
    private Integer richiestaID = 0;
    private Integer richiestaStato = 0;
    private Integer richiestaDatiMontagne = 0;
    //Le richieste GET vengono gestite dalla libreria RetroFit

    /*FORMATO DATI MONTAGNE (7 colonne,e 361 righe di cui una di descrizione)
           -azimuth:               0-360 vale 0 a nord e cresce verso est
           -altitude:              inclinazione all' orizzonte già calcolata
           -distance (m):          distanza montagna
           -latitude:
           -longitude
           -elevation (m amsl):    altitudine montagna dal mare
       */
    double[][] risultatiMontagne = new double[7][360];
        /*FORMATO DATI SOLE/LUNA (288 righe di "posizione") viene eseguito un calcolo ogni 5 minuti 24*(60/5)
            -ora:
            -minuto:
            -altezza (gradi):       inclinazione all' orizzonte
            -azimuth:               0-360 vale 0 a nord e cresce verso est
        */

    class posizione implements Comparable
    {
        public int compareTo(Object x) {

            if(x == null) System.out.println("dati sole/luna mancanti!");;

            if(!(x instanceof posizione)) throw new ClassCastException();

            posizione e = (posizione) x;

            if(azimuth>e.azimuth){
                return 1;
            }else if(azimuth<e.azimuth){
                return -1;
            }else{
                return 0;
            }

        }
        int ora;
        int minuto;
        double altezza;
        double azimuth;
    };
    posizione[] risultatiSole = new posizione[288];
    posizione[] risultatiLuna = new posizione[288];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Intent i = getIntent();
        Double lat = i.getDoubleExtra("lat", 0.0);
        Double lng = i.getDoubleExtra("long", 0.0);
        this.lon = lng;
        this.lat = lat;
        //String data = new SimpleDateFormat("dd/MM/yyyy").format(new Date(i.getLongExtra("data", 0)));
        Date data = new Date();
        data.setTime(i.getLongExtra("data", 0));
        this.data = data;
        if(data.getTime() == 0){
            Toast.makeText(getApplicationContext(), "Data non selezionata",
                    Toast.LENGTH_LONG).show();
        }else{
            //Toast.makeText(getApplicationContext(), "data: " + data.toString(), Toast.LENGTH_LONG).show();
        }
        //System.out.println("data: " + datas);

        idt = (TextView) findViewById(R.id.idt);
        //Preparo la finestra di caricamento
        progressDialog = new ProgressDialog(Data.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //Variabile Retrofit per gestire tutte le richieste GET
        retrofit = new Retrofit.Builder()
                .baseUrl("http://www.heywhatsthat.com/")
                .build();

        if (savedInstanceState != null) {
            // Se c'è un istanza salvata non richiedo nuovamente i dati
            String savedID = savedInstanceState.getString("ID");
            String savedPeak = savedInstanceState.getString("peak");
            id = savedID;
            gPeak = savedPeak;
            idt.setText(savedID);
            setPeak(savedPeak);
        }else{
            callsAPI(lat, lng);
        }
    }


    /**
     * Salvataggio dell'istanza
     */
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("ID", id);
        outState.putCharSequence("peak", gPeak);
    }

    /**
     * Interfacce per le richieste GET
     */
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
                        id = response.body().string();
                        System.out.print("ID: " + id);
                        idt.post(new Runnable() {
                            @Override
                            public void run() {
                                idt.setText(id);
                            }
                        });
                        progressDialog.dismiss();
                        checkStatus(id); //Ottenuto l'ID controllo lo stato della generazione del panorama

                    } catch (IOException e) {
                        e.printStackTrace();
                        richiediID();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Impossibile caricare l'ID!", Toast.LENGTH_SHORT).show();
                    richiediID();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)                                                                                                                                                                                                         {
                Log.d("Errore: ", t.getMessage());
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
            callsAPI(lat, lon);
            progressDialog.setMessage("Richiesta id panorama, tentativo n°: " + (richiestaID+1));
            richiestaID++;
        }else{
            System.out.println("ID non ottenuto, controllare la connessione");
            Snackbar.make(findViewById(R.id.dataContainerLayout), "ID non ottenuto, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    richiestaID = 0;
                    callsAPI(lat, lon);
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    private void checkStatus(final String idpass){
        progressDialog.setMessage("Aspettando il panorama...");
        progressDialog.setTitle("Attesa dati Panorama");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(idpass);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String status = response.body().string();
                        if(status.length() > 0 && status.charAt(0) == '1'){ // Se la mappa è pronta vado ad ottenere il panorama
                            progressDialog.dismiss();
                            loadPeakData(idpass);
                        }else{
                            richiediStato(idpass);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        richiediStato(idpass);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "La mappa non è stata generata!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    richiediStato(idpass);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Errore: ", t.getMessage());
                richiediStato(idpass);

            }
        });
    }

    private void richiediStato(final String idpass){
        if (richiestaStato<n_tentativi){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            checkStatus(idpass);
            progressDialog.setMessage("controllo se è pronto il panorama, tentativo n°: " + (richiestaStato+1));
            richiestaStato++;
        }else{
            System.out.println("panorama non pronto, controllare la connessione");
            Snackbar.make(findViewById(R.id.dataContainerLayout), "panorama non pronto, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    richiestaStato = 0;
                    checkStatus(idpass);
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    private void loadPeakData(String idpass){
        progressDialog.setMessage("Creazione grafico...");
        progressDialog.setTitle("Scaricamento e Incrocio dati");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento
        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(idpass);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        gPeak = response.body().string();
                        setPeak(gPeak);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Impossibile caricare i picchi!", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Error", t.getMessage());
                progressDialog.dismiss();
            }
        });
    }
    private void richiediDatiMontagne(final String idpass){
        if (richiestaDatiMontagne<10){ // richiedo l' id se non lì ho già fatto troppe volte
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            checkStatus(idpass);
            progressDialog.setMessage("controllo se è pronto il panorama, tentativo n°: " + (richiestaDatiMontagne+1));
            richiestaDatiMontagne++;
        }else{
            System.out.println("panorama non pronto, controllare la connessione");
            Snackbar.make(findViewById(R.id.dataContainerLayout), "dati non ricevuti, controllare la connessione e riprovare", Snackbar.LENGTH_LONG).setAction("Riprova", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    richiestaDatiMontagne = 0;
                    checkStatus(idpass);
                }
            }).show();
            progressDialog.dismiss();
        }
    }

    private void setPeak(String peak){
        //Faccio il parsing della stringa e butto i dati nella libreria per generare grafici e per salvare nel database.

        List<Entry> entriesMontagne = new ArrayList<Entry>();
        List<Entry> entriesSole = new ArrayList<Entry>();
        List<Entry> entriesLuna = new ArrayList<Entry>();

        //CALCOLO SOLE ogni 5 min
        int indexSole = 0;
        for (int ora = 0; ora<24; ora++) {
            for (int min = 0; min < 60; min+=5) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(data);
                calendar.set(Calendar.HOUR_OF_DAY, ora);
                calendar.set(Calendar.MINUTE, min);
                SunPosition position = SunPosition.compute()
                        .on(calendar.getTime())       // set a date
                        .at(lat, lon)   // set a location
                        .execute();     // get the results
                //System.out.println("ora: " + ora + " Elevazione: " + position.getAltitude() + "Azimuth: " + position.getAzimuth()+'\n');
                risultatiSole[indexSole] = new posizione();
                risultatiSole[indexSole].ora=ora;
                risultatiSole[indexSole].minuto=min;
                risultatiSole[indexSole].altezza=position.getAltitude();
                risultatiSole[indexSole].azimuth=position.getAzimuth();
                indexSole++;
            }
        }
        Arrays.sort(risultatiSole); //ordino secondo azimuth
        for(int i = 0; i<288; i++) { //passo dati al grafico
            if(risultatiSole[i].minuto == 0) {
                entriesSole.add(new Entry((float) risultatiSole[i].azimuth, (float) risultatiSole[i].altezza));
            }
        }

        //CALCOLO LUNA ogni 5 min
        int indexLuna = 0;
        for (int ora = 0; ora<24; ora++) {
            for (int min = 0; min < 60; min+=5) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(data);
                calendar.set(Calendar.HOUR_OF_DAY, ora);
                calendar.set(Calendar.MINUTE, min);

                MoonPosition position = MoonPosition.compute()
                        .on(calendar.getTime())       // set a date
                        .at(lat, lon)   // set a location
                        .execute();     // get the results

                //System.out.println("ora: " + ora + " Elevazione: " + position.getAltitude() + "Azimuth: " + position.getAzimuth()+'\n');
                risultatiLuna[indexLuna] = new posizione();
                risultatiLuna[indexLuna].ora=ora;
                risultatiLuna[indexLuna].minuto=min;
                risultatiLuna[indexLuna].altezza=position.getAltitude();
                risultatiLuna[indexLuna].azimuth=position.getAzimuth();
                indexLuna++;
            }
        }
        //Arrays.sort(risultatiLuna); //ordino secondo azimuth ATTENZIONE: se vengono ordinati allora si mescolano i dati della mattina dopo quelli della sera
        for(int i = 0; i<288; i++) { //passo dati al grafico
            if(risultatiLuna[i].minuto == 0){
                entriesLuna.add(new Entry((float) risultatiLuna[i].azimuth, (float) risultatiLuna[i].altezza));
            }
        }

        //PARSING dati
        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));
        for(int a = 1; a< lines.size(); a++){
            List<String> tempsplit = Arrays.asList(lines.get(a).split(","));
            for (int i =0; i<7; i++) {
                 risultatiMontagne[i][a-1] = Double.parseDouble(tempsplit.get(i));
            }
            entriesMontagne.add(new Entry(Float.parseFloat(tempsplit.get(0)), Float.parseFloat(tempsplit.get(2))));
        }

        //ricerca alba / uscita dalle montagne e tramonto / entrata nelle montagne
        List<posizione> alba = new ArrayList<posizione>();
        List<posizione> tramonto = new ArrayList<posizione>();
        boolean prev = false;
        int minutiSole = 0;
        for(int i = 0; i<288; i++){
            boolean sopra = sopra(i);
            if (sopra) minutiSole+=5;
            //System.out.println("ora: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto +" sopra? " + sopra);
            if(!prev && sopra){ //alba
                alba.add(risultatiSole[i]);
                //System.out.println("alba: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto );
                Toast.makeText(getApplicationContext(),"alba: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto, Toast.LENGTH_LONG).show();
            }
            if(prev && !sopra){ //alba
                tramonto.add(risultatiSole[i]);
                //System.out.println("tramonto: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto );
                Toast.makeText(getApplicationContext(),"tramonto: " + risultatiSole[i].ora + ":"+ risultatiSole[i].minuto , Toast.LENGTH_LONG).show();
            }
            prev=sopra;
        }
        Toast.makeText(getApplicationContext(),"Ore di Sole: " + minutiSole/60 + " ore, "+ (minutiSole-(minutiSole/60)*60)+ " minuti" , Toast.LENGTH_LONG).show();


        //proprietà grafico:
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        LineDataSet dataSetMontagne = new LineDataSet(entriesMontagne, "Profilo montagne"); // add entries to dataset
        LineDataSet dataSetSole = new LineDataSet(entriesSole, "sole");
        LineDataSet dataSetLuna = new LineDataSet(entriesLuna, "luna");

        //proprietà grafico Montagne
        dataSetMontagne.setMode(LineDataSet.Mode.LINEAR);
        dataSetMontagne.setColor(R.color.pale_green);
        //dataSet.setLineWidth(4f);
        dataSetMontagne.setDrawValues(false);
        dataSetMontagne.setDrawCircles(false);
        dataSetMontagne.setCircleColor(Color.BLACK);
        dataSetMontagne.setDrawCircleHole(false);
        dataSetMontagne.setDrawValues(true);
        dataSetMontagne.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
        dataSetMontagne.setFillDrawable(drawable);

        //proprietà grafiche Sole
        dataSetSole.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetSole.setColor(Color.YELLOW);
        dataSetSole.setLineWidth(1f);
        dataSetSole.setDrawValues(false);
        dataSetSole.setDrawCircles(true);
        dataSetSole.setCircleColor(Color.YELLOW);
        dataSetSole.setDrawCircleHole(false);
        dataSetSole.setDrawValues(true);
        dataSetSole.setDrawFilled(false);

        //proprietà grafiche Luna
        dataSetLuna.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetLuna.setColor(Color.GRAY);
        dataSetLuna.setLineWidth(1f);
        dataSetLuna.setDrawValues(false);
        dataSetLuna.setDrawCircles(true);
        dataSetLuna.setCircleColor(Color.GRAY);
        dataSetLuna.setDrawCircleHole(false);
        dataSetLuna.setDrawValues(false);
        dataSetLuna.setDrawFilled(false);

        chart.getDescription().setText("Profilo montagne con sole e luna");

        LineData lineData = new LineData();
        lineData.addDataSet(dataSetMontagne);
        lineData.addDataSet(dataSetSole);
        lineData.addDataSet(dataSetLuna);
        XAxis left = chart.getXAxis();
        left.setGranularity(1f);
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 0 || value == 360) {
                    return "N"; // here you can map your values or pass it as empty string
                }else if (value == 90) {
                    return "E"; // here you can map your values or pass it as empty string
                }else if (value == 180) {
                    return "S"; // here you can map your values or pass it as empty string
                }else if (value == 270) {
                    return "O"; // here you can map your values or pass it as empty string
                }else{
                    return "";
                }
            }
        });

        Legend l = chart.getLegend();
        l.setFormSize(10f); // set the size of the legend forms/shapes
        //l.setForm(LegendForm.CIRCLE); // set what type of form/shape should be used
        //l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        //l.setTypeface(...);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis

        // set custom labels and colors
        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0,5));
        //l.setCustom(entrinseo);

        chart.setData(lineData);
        chart.animateX(2500);
        chart.invalidate();


        //SALA STAMPA

        //stampo risultati montagne
        /*for(int i = 0; i<360; i++){
            for(int j = 0; j<7; j++){
                System.out.print(risultatiMontagne[j][i] + "," + '\t');
            }
            System.out.println('\n');
        }*/

        //stampo risultati sole
        /*for(int i = 0; i<288; i++){
            System.out.println("SOLE: " + "ora: "+ risultatiSole[i].ora + ":"+ risultatiSole[i].minuto + '\t' +" altitudine: " + risultatiSole[i].altezza + '\t' +" azimuth: " + risultatiSole[i].azimuth);
        }*/

        //stampo risultati luna
        /*for(int i = 0; i<288; i++){
            System.out.println("LUNA: " + "ora: "+ risultatiLuna[i].ora + ":"+ risultatiLuna[i].minuto + '\t' +" altitudine: " + risultatiLuna[i].altezza + '\t' +" azimuth: " + risultatiLuna[i].azimuth);
        }*/


    }
    private boolean sopra(int i){
        //todo confronto fra il primo e l' ultimo (0-360)
        //per la posizione i-esima del sole allineo con l' azimuth rispetto alle montagne e confronto l' altezza.
        //nota: ci sono 2 casi limite, uno è che il sole / luna abbia l' azimuth iniziale più basso di tutti i punti del profilo montagne e l' altro è che lo abbia maggiore di tutte le montagne.
        int j = 0;
        for(int c = 0; c<360 && !(risultatiMontagne[0][c]>=risultatiSole[i].azimuth);c++){ //allineamento sole montagne
            //if(i==3)System.out.println(" azimut " + risultatiMontagne[0][c]+ " azimut sole" + risultatiSole[i].azimuth);
            j = c;
            //System.out.println(j);
        }

        if(j==359){ //azimuth maggiore di tutti i dati delle montagne quindi confronto con l' ultimo e il primo
            if (risultatiSole[i].altezza > ((risultatiMontagne[2][259]+risultatiMontagne[2][0])/2)) {
                return true;
            }
        }else{ //azimuth intermedio
            if (risultatiSole[i].altezza > ((risultatiMontagne[2][j]+risultatiMontagne[2][j+1])/2)) {
                //System.out.println("Montangna: " + risultatiMontagne[2][j] + " Sole: " + risultatiSole[i].altezza);
                //System.out.print(" true "+ risultatiSole[i].ora + ":" + risultatiSole[i].minuto);
                //System.out.println(" azimut 1 " + risultatiMontagne[0][j-1]+ " azimut 2 " + risultatiMontagne[0][j]+ " azimut sole" + risultatiSole[i].azimuth);
                return true;
            }
        }
        return false;
    }
}


