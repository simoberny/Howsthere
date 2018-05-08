package it.unitn.simob.howsthere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    //Le richieste GET vengono gestite dalla libreria RetroFit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Intent i = getIntent();
        Double lat = i.getDoubleExtra("lat", 0.0);
        Double lng = i.getDoubleExtra("long", 0.0);
        String data = i.getStringExtra("data");

        if(data == null){
            data = "Data non selezionata!";
        }

        /*TextView latT = (TextView) findViewById(R.id.latT);
        TextView longT = (TextView) findViewById(R.id.longT);
        TextView dataT = (TextView) findViewById(R.id.dateT);*/
        idt = (TextView) findViewById(R.id.idt);

        /*latT.setText("" + lat);
        longT.setText("" + lng);
        dataT.setText("" + data);*/

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

    private void callsAPI(Double lat, Double lng){
        HeyWhatsID service = retrofit.create(HeyWhatsID.class);
        Call<ResponseBody> call = service.getID(lat, lng);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        id = response.body().string();
                        idt.post(new Runnable() {
                            @Override
                            public void run() {
                                idt.setText(id);
                            }
                        });
                        checkStatus(id); //Ottenuto l'ID controllo lo stato della generazione del panorama
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Impossibile caricare l'ID!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Errore: ", t.getMessage());
            }
        });
    }

    private void checkStatus(final String idpass){
        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(idpass);

        progressDialog.setMessage("Eseguendo il panorama...");
        progressDialog.setTitle("Controllo lo stato...");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String status = response.body().string();
                        if(status.length() > 0 && status.charAt(0) == '1'){ // Se la mappa è pronta vado ad ottenere il panorama
                            loadPeakData(idpass);
                        }else{ //Altrimenti aspetto e riprovo dopo 10 secondi
                            Thread.sleep(1000);
                            checkStatus(idpass);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "La mappa non è stata generata!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Errore: ", t.getMessage());
                progressDialog.dismiss();
            }
        });
    }

    private void loadPeakData(String idpass){
        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(idpass);

        progressDialog.setMessage("Caricando l'orizzonte...");
        progressDialog.setTitle("Attendere...");
        progressDialog.show(); //Avvio la finestra di dialogo con il caricamento

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

    private void setPeak(String peak){
        //Faccio il parsing della stringa e butto i dati nella libreria per generare grafici
        List<Entry> entries = new ArrayList<Entry>();

        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));

        for(int a = 1; a< lines.size(); a++){
            List<String> tempsplit = Arrays.asList(lines.get(a).split(","));
            entries.add(new Entry(Float.parseFloat(tempsplit.get(0)), Float.parseFloat(tempsplit.get(2))));
        }

        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        LineDataSet dataSet = new LineDataSet(entries, "Profilo montagne"); // add entries to dataset

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(R.color.pale_green);
        dataSet.setLineWidth(4f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(true);
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
        dataSet.setFillDrawable(drawable);

        chart.getDescription().setText("Profilo montagne");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.animateX(2500);
        chart.invalidate();
    }
}
