package it.unitn.lpmt.howsthere;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fenchtose.nocropper.CropperView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;


public class PostFeed extends AppCompatActivity {
    private static final String TAG = "Feed";

    private Bitmap mBitmap;
    private ProgressDialog progressDialog;
    private CropperView crop;
    private int rotationCount = 0;
    private TextView desc;
    private String id;
    private double lat;
    private double lon;
    private String posizione;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Caricando...");
        progressDialog.setTitle("Database");

        Intent data = getIntent();
        id = data.getStringExtra("ID");

        System.out.println("ID: " + id);

        lat = data.getDoubleExtra("lat", 0.0);
        lon = data.getDoubleExtra("lon", 0.0);

        posizione = getLocation(lat, lon);

        TextView posizione_text = findViewById(R.id.getposizione);
        posizione_text.setText(posizione);

        if(id != null) {
            PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
            Panorama tempP = panoramiStorage.getPanoramabyID(id);
            stampaGrafico(tempP);
        }

        Uri file = Uri.parse(data.getStringExtra("photo"));

        try {
           mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBitmap = getResizedBitmap(mBitmap, 1000);

        crop = findViewById(R.id.cropper_view);
        crop.setImageBitmap(mBitmap);
        crop.setMakeSquare(false);
        desc = findViewById(R.id.desc);

        ImageView rotate = findViewById(R.id.rotate_button);
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        Button invia = findViewById(R.id.invia);
        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                mBitmap = crop.getCroppedBitmap().getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] data = baos.toByteArray();
                uploadToFirebase(data);
            }
        });

        Button annulla = findViewById(R.id.annulla);
        annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void uploadToFirebase(final byte [] data){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        final String file_name = UUID.randomUUID() + ".jpg";
        final StorageReference feedRef = storageRef.child("images/" + file_name);

        UploadTask uploadTask = feedRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.post_feed_con), "Caricamento foto fallito!", Snackbar.LENGTH_LONG);
                mySnackbar.setAction("Riprova", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadToFirebase(data);
                    }
                });
                mySnackbar.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.post_feed_con), "Caricamento avvenuto con successo!", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return feedRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    sendUri(downloadUri, file_name);
                }
            }
        });
    }

    private void sendUri(Uri down, String filename){
        Intent returnIntent = new Intent();

        returnIntent.putExtra("posizione", posizione);
        returnIntent.putExtra("filename", filename);
        returnIntent.putExtra("uri", down.toString());
        returnIntent.putExtra("descrizione", desc.getText().toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void rotateImage() {
        if (mBitmap == null) {
            Log.e(TAG, "bitmap is not loaded yet");
            return;
        }

        mBitmap = rotateBitmap(mBitmap, 90);
        crop.setImageBitmap(mBitmap);
        rotationCount++;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private String getLocation(double lat, double lon){
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String citta = "";
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                if(addresses.get(0).getLocality() != null){
                    citta = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                }else{
                    citta = lat + ", " + lon;
                }
            }else{
                citta = lat + ", " + lon;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return citta;
    }

    private void stampaGrafico(Panorama p){
        List<Entry> entriesMontagne = new ArrayList<Entry>();
        List<Entry> entriesSole = new ArrayList<Entry>();
        List<Entry> entriesLuna = new ArrayList<Entry>();
        //SOLE
        //Arrays.sort(p.risultatiSole); //ordino secondo azimuth
        for(int i = 0; i<288; i++) { //passo dati al grafico
            if(p.risultatiSole[i].minuto == 0) {
                entriesSole.add(new Entry((float) p.risultatiSole[i].azimuth, (float) p.risultatiSole[i].altezza));
            }
        }
        //LUNA
        //Arrays.sort(risultatiLuna); //ordino secondo azimuth ATTENZIONE: se vengono ordinati allora si mescolano i dati della mattina dopo quelli della sera
        for(int i = 0; i<864; i++) { //passo dati al grafico
            if(p.risultatiLuna[i].minuto == 0){
                if(i<288 && p.risultatiLuna[288].azimuth > p.risultatiLuna[i].azimuth) { //solo le ore del giorno prima che concludono l' arco in cielo
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));
                }
                else if(i>575 && p.risultatiLuna[575].azimuth < p.risultatiLuna[i].azimuth) { //solo le ore del giorno prima che concludono l' arco in cielo
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));
                }
                else if(i>=288 && i<576){
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));

                }else {
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) -90));
                }
            }
        }
        //MONTAGNE
        for (int i =0; i<360; i++) {

            entriesMontagne.add(new Entry((float)p.risultatiMontagne[0][i], (float)p.risultatiMontagne[2][i]));
        }

        //Collections.sort(entriesSole, new EntryXComparator());

        //proprietà grafico:
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinValue(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinValue(-1);

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
        dataSetMontagne.setDrawValues(false);
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
        dataSetSole.setDrawValues(false);
        dataSetSole.setDrawFilled(false);

        //proprietà grafiche Luna
        dataSetLuna.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetLuna.setColor(Color.LTGRAY);
        dataSetLuna.setLineWidth(1f);
        dataSetLuna.setDrawValues(false);
        dataSetLuna.setDrawCircles(true);
        //dataSetLuna.setCircleColor(Color.GRAY);
        dataSetLuna.setDrawCircleHole(false);
        dataSetLuna.setDrawValues(false);
        dataSetLuna.setDrawFilled(false);

        int[] coloricerchiLuna = new int[64]; //un colore per ogni dato sul grafico (24 al giorno)
        for(int i = 0; i<24; i++){
            coloricerchiLuna[i] = Color.argb(65,88, 88, 88);
        }
        for(int i = 24; i<48; i++){
            coloricerchiLuna[i] = Color.GRAY;
        }
        for(int i = 48; i<64; i++){
            coloricerchiLuna[i] = Color.argb(65,88, 88, 88);
        }
        dataSetLuna.setCircleColors(coloricerchiLuna);

        //chart.getDescription().setText("Profilo montagne con sole e luna");
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetMontagne);
        lineData.addDataSet(dataSetSole);
        lineData.addDataSet(dataSetLuna);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);

        //chart.saveToGallery("grafico.jpeg",100);
        /*XAxis left = chart.getXAxis();
        //chart.getXAxis().setLabelCount(0);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
        YAxis yAxis = chart.getAxisLeft(); // Show left y-axis line
        yAxis.setDrawAxisLine(false);*/

        /*chart.getXAxis().setValueFormatter(new IAxisValueFormatter() { //tolto perchè vedevi i valori solo zoomando
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value == 1 || value == 359) {
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
        });*/

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
        chart.animateX(3500);
        chart.invalidate();
    }

}
