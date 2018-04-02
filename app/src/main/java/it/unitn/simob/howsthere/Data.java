package it.unitn.simob.howsthere;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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
import retrofit2.http.Path;
import retrofit2.http.Query;

public class Data extends AppCompatActivity {

    private String id = null;
    private String gPeak = null;

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

        TextView latT = (TextView) findViewById(R.id.latT);
        TextView longT = (TextView) findViewById(R.id.longT);
        TextView dataT = (TextView) findViewById(R.id.dateT);
        TextView idt = (TextView) findViewById(R.id.idt);

        latT.setText("" + lat);
        longT.setText("" + lng);
        dataT.setText("" + data);

        if (savedInstanceState != null) {
            // Se c'è un istanza salvata non richiedo nuovamente i dati
            String savedID = savedInstanceState.getString("ID");
            String savedPeak = savedInstanceState.getString("peak");
            id = savedID;
            gPeak = savedPeak;
            idt.setText(savedID);
            setPeak(savedPeak);
        }else{
            // Altrimenti controllo la connessione a internet e inizio la richiesta partendo dall'ID
            if(checkInternetConnection()){
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://www.heywhatsthat.com/")
                        .build();

                HeyWhatsID service = retrofit.create(HeyWhatsID.class);
                Call<ResponseBody> call = service.getID(lat, lng);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                id = response.body().string();
                                //Ottenuto l'ID controllo lo stato della generazione del panorama
                                changeID(id);
                                checkStatus(id);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Impossibile caricare l'ID!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        // Salvataggio dell'istanza
        super.onSaveInstanceState(outState);
        outState.putCharSequence("ID", id);
        outState.putCharSequence("peak", gPeak);
    }

    private void changeID(String idpass){
        TextView idt = (TextView) findViewById(R.id.idt);
        idt.setText(idpass);
    }

    private void checkStatus(final String idpass){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.heywhatsthat.com/")
                .build();

        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(idpass);

        // Finestra di dialogo con il loader di attesa
        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(Data.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Eseguendo il panorama...");
        progressDoalog.setTitle("Attendere (1 minuto)...");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String status = response.body().string();
                        if(status.length() > 0 && status.charAt(0) == '1'){
                            // Se la mappa è pronta vado ad ottenere il panorama
                            loadPeakData(idpass);
                        }else{
                            //Altrimenti aspetto e riprovo dopo 10 secondi
                            Thread.sleep(10000);
                            checkStatus(idpass);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Attesa fallita!", Toast.LENGTH_SHORT).show();
                }
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Error", t.getMessage());
                progressDoalog.dismiss();
            }
        });
    }

    private void loadPeakData(String idpass){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.heywhatsthat.com/")
                .build();
        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(idpass);

        final ProgressDialog progressDoalog;
        progressDoalog = new ProgressDialog(Data.this);
        progressDoalog.setMax(100);
        progressDoalog.setMessage("Caricando l'orizzonte...");
        progressDoalog.setTitle("Attendere...");
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDoalog.show();

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
                    try {
                        Log.d("Error", response.errorBody().string());
                        Log.d("Error", response.message());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                progressDoalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDoalog.dismiss();
                Log.d("Error", t.getMessage());
            }
        });
    }

    private void setPeak(String peak){

        //Faccio il parsing della stringa e butto i dati nella libreria per generare grafici

        List<Entry> entries = new ArrayList<Entry>();
        List<String> lines = Arrays.asList(peak.split("[\\r\\n]+"));

        for(int a = 1; a< lines.size(); a++){
            List<String> tempsplit = Arrays.asList(lines.get(a).split(","));
            entries.add(new Entry(Float.parseFloat(tempsplit.get(1)), Float.parseFloat(tempsplit.get(6))));
        }

        LineChart chart = (LineChart) findViewById(R.id.chart);

        chart.setDrawGridBackground(false);
        chart.setMaxHighlightDistance(300);
        XAxis x = chart.getXAxis();
        x.setEnabled(false);

        chart.getAxisRight().setEnabled(false);

        chart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format("%.2f",value);
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "Profilo montagne"); // add entries to dataset
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setFillColor(ContextCompat.getColor(getApplicationContext(),R.color.pale_green));
        dataSet.setColor(ContextCompat.getColor(getApplicationContext(),R.color.pale_green));

        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(true);

        dataSet.setFillAlpha(255);
        dataSet.setDrawCircles(false);

        chart.getDescription().setText("Profilo montagne");
        LineData lineData = new LineData(dataSet);
        chart.getXAxis().setDrawGridLines(false);
        chart.setData(lineData);
        chart.animateX(2500);
        chart.invalidate(); // refresh
    }

    //Interfacce per le richieste GET
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

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Internet non disponibile", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
