package it.unitn.simob.howsthere;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;

public class Risultati extends AppCompatActivity {
    private Panorama p;
    private String id;

    private PickImageDialog dialog;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int GALLERY_INTENT = 25;
    public static final int CAMERA_INTENT = 26;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultati);

        FloatingActionButton take_photo = findViewById(R.id.take_photo);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        Intent i = getIntent();
        id = i.getStringExtra("ID");
        System.out.println("RISULTATI id: " +id);
        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
        p = panoramiStorage.getPanoramabyID(id);
        System.out.println("recuperato panorama" +id);
        System.out.println("panorama NULL? " + p);
        stampaGrafico();
        stampaValoriBase(); //informazioni sempre conosciute
    }

    void stampaGrafico(){
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

        System.out.println("Montagne: "+ entriesMontagne.size() + " Sole: " + entriesSole.size() + " Luna: "+ entriesLuna.size());
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


    public void getImage(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MaxPhotoRef", 0);
        final SharedPreferences.Editor editor = pref.edit();
        Integer day = pref.getInt("day", 32);
        Integer actual_max = pref.getInt("max_daily", 5);

        if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > day){
            editor.putInt("max_daily", 0);
        }else{
            if(actual_max > 0){
                dialog = PickImageDialog.build(new PickSetup().setPickTypes(EPickType.GALLERY, EPickType.CAMERA).setGalleryIcon(R.mipmap.gallery_colored).setCameraIcon(R.mipmap.camera_colored))
                        .setOnClick(new IPickClick() {
                            @Override
                            public void onGalleryClick() {
                                if(checkPermission()) {
                                    openGallery();
                                }
                            }

                            @Override
                            public void onCameraClick() {
                                if(checkPermission()) {
                                    openCamera();
                                }
                            }
                        }).show(this);
            }else{
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.risultatiMainLayout), "Superato il limite massimo di foto in un giorno, torna domani!", Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }

    public void openGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_INTENT);
    }

    public void openCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "it.unitn.simob.howsthere.fileprovider",
                        photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, CAMERA_INTENT);
            }
        }
    }

    public void cropImage(Uri uri) {
        if(uri != null){
            Intent i = new Intent(this, PostFeed.class);
            i.putExtra("photo", uri.toString());
            i.putExtra("ID", id);
            i.putExtra("lat", p.lat);
            i.putExtra("lon", p.lon);
            // Eventualmente mandare anche la foto del panorame //

            startActivityForResult(i, 15);
        }
    }

    public boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CAMERA_INTENT:
                if(resultCode == RESULT_OK){
                    File m_file = new File(mCurrentPhotoPath);
                    Uri m_imgUri = Uri.fromFile(m_file);
                    cropImage(m_imgUri);
                }

                break;
            case GALLERY_INTENT:
                if(resultCode == RESULT_OK){
                    cropImage(data.getData());
                }
                break;
            case 15:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtras(extras);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }
    }
    void stampaValoriBase(){
        TextView albaTv = (TextView)findViewById(R.id.alba);
        albaTv.setText("Alba: "+p.getAlba().ora + ":" + p.getAlba().minuto);
        TextView tramontoTv = (TextView)findViewById(R.id.tramonto);
        tramontoTv.setText("Tramonto: "+p.getTramonto().ora+ ":" + p.getTramonto().minuto);
        TextView minSoleTv = (TextView)findViewById(R.id.minutiSole);
        minSoleTv.setText("Ore di Sole: " + p.minutiSole/60 + " ore, "+ (p.minutiSole-(p.minutiSole/60)*60)+ " minuti");
        TextView cittaTv = (TextView)findViewById(R.id.citta);
        cittaTv.setText("città: "+p.citta);
        TextView dataTv = (TextView)findViewById(R.id.data);
        dataTv.setText("data: "+p.data);
    }
}
