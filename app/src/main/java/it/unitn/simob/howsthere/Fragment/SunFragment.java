package it.unitn.simob.howsthere.Fragment;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.PostFeed;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.RisultatiActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SunFragment extends Fragment {
    private Panorama p;
    private String id;

    private FirebaseAuth mAuth;

    private PickImageDialog dialog;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int GALLERY_INTENT = 25;
    public static final int CAMERA_INTENT = 26;
    private String mCurrentPhotoPath;
    private FrameLayout main = null;

    LineChart chart = null;
    private View currentView;

    public SunFragment() {
        // Required empty public constructor
    }

    public static SunFragment newInstance(){
        SunFragment sf = new SunFragment();
        return sf;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("dataGotFromServer", dataGotFromServer);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //dataGotFromServer = savedInstanceState.getString("dataGotFromServer");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_sun, container, false);
        currentView = view;
        ((RisultatiActivity)getActivity()).getSupportActionBar().setTitle("Sole");

        FloatingActionButton take_photo = view.findViewById(R.id.take_photo);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuth.getCurrentUser() != null){
                    getImage();
                }else{
                    Snackbar mySnackbar = Snackbar.make(view.findViewById(R.id.risultatiMainLayout), "Devi eseguire il login per poter scattare un panorama!", Snackbar.LENGTH_LONG);
                    mySnackbar.setAction("Login", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            intent.putExtra("login", 1);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    mySnackbar.show();
                }
            }
        });

        final Button button = view.findViewById(R.id.expandableButton1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ExpandableRelativeLayout expandableLayout1 = (ExpandableRelativeLayout) currentView.findViewById(R.id.expandableLayout1);
                expandableLayout1.toggle(); // toggle expand and collapse
            }
        });

        p = ((RisultatiActivity)getActivity()).p;

        chart = view.findViewById(R.id.chart);
        main = view.findViewById(R.id.risultatiMainLayout);

        //Se per qualche motivo il panorama non è leggibile o non c'è chiudo l'attività
        if(p != null){
            stampaGrafico();
            stampaValoriBase(view); //informazioni sempre conosciute
        }else{
            getActivity().finish();
        }

        return view;
    }

    void stampaGrafico(){
        List<Entry> entriesMontagne = new ArrayList<Entry>();
        final List<Entry> entriesSole = new ArrayList<Entry>();
        //SOLE
        //Arrays.sort(p.risultatiSole); //ordino secondo azimuth
        for(int i = 0; i<288; i++) { //passo dati al grafico
            if(p.risultatiSole[i].minuto == 0) {
                entriesSole.add(new Entry((float) p.risultatiSole[i].azimuth, (float) p.risultatiSole[i].altezza));
            }
        }
        //MONTAGNE
        for (int i =0; i<360; i++) {

            entriesMontagne.add(new Entry((float)p.risultatiMontagne[0][i], (float)p.risultatiMontagne[2][i]));
        }

        //System.out.println("Montagne: "+ entriesMontagne.size() + " Sole: " + entriesSole.size());
        //proprietà grafico:

        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinValue(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinValue(-1);

        LineDataSet dataSetMontagne = new LineDataSet(entriesMontagne, "Profilo montagne"); // add entries to dataset
        final LineDataSet dataSetSole = new LineDataSet(entriesSole, "sole");

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

        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_red);
        dataSetMontagne.setFillDrawable(drawable);
        dataSetMontagne.setDrawHighlightIndicators(false);

        //proprietà grafiche Sole
        dataSetSole.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetSole.setColor(Color.YELLOW);
        dataSetSole.setLineWidth(1f);
        dataSetSole.setDrawValues(true);
        dataSetSole.setDrawCircles(true);
        dataSetSole.setCircleColor(Color.YELLOW);
        dataSetSole.setDrawCircleHole(false);
        dataSetSole.setDrawFilled(false);
        dataSetSole.setDrawValues(true);
        dataSetSole.setDrawHighlightIndicators(false);
        dataSetSole.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int idx = chart.getLineData().getDataSetByIndex(dataSetIndex).getEntryIndex(entry);
                return String.valueOf(idx);
            }
        });



        int[] coloricerchiLuna = new int[64]; //un colore per ogni dato sul grafico (24 al giorno)
        for(int i = 0; i<24; i++){
            coloricerchiLuna[i] = Color.argb(65,158, 158, 158);
        }
        for(int i = 24; i<48; i++){
            coloricerchiLuna[i] = Color.GRAY;
        }
        for(int i = 48; i<64; i++){
            coloricerchiLuna[i] = Color.argb(65,158, 158, 158);
        }

        chart.getDescription().setText("");
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetMontagne);
        lineData.addDataSet(dataSetSole);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setMaxVisibleValueCount(Integer.MAX_VALUE);//mostrami tutti i label
        chart.setScaleEnabled(false);

        Legend l = chart.getLegend();
        l.setFormSize(10f);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis

        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0,5));

        chart.setData(lineData);
        chart.animateX(3500);
        chart.invalidate();
    }

    public void getImage(){
        final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MaxPhotoRef", 0);
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
                        }).show(getActivity());
            }else{
                Snackbar mySnackbar = Snackbar.make(main, "Superato il limite massimo di foto in un giorno, torna domani!", Snackbar.LENGTH_LONG);
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
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "it.unitn.simob.howsthere.fileprovider",
                        photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, CAMERA_INTENT);
            }
        }
    }

    public void cropImage(Uri uri) {
        if(uri != null){
            Intent i = new Intent(getActivity(), PostFeed.class);
            i.putExtra("photo", uri.toString());
            i.putExtra("ID", id);
            i.putExtra("lat", p.lat);
            i.putExtra("lon", p.lon);
            // Eventualmente mandare anche la foto del panorame //

            startActivityForResult(i, 15);
        }
    }

    public boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    if(mCurrentPhotoPath != null) {
                        File m_file = new File(mCurrentPhotoPath);
                        Uri m_imgUri = Uri.fromFile(m_file);
                        cropImage(m_imgUri);
                    }
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
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtras(extras);
                    intent.putExtra("panoramaid", id);
                    intent.putExtra("addFeed", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }
    }

    void stampaValoriBase(View view){
        TextView albaTv = (TextView) view.findViewById(R.id.alba);
        if(p.getAlba() != null) {
            albaTv.setText(p.getAlba().ora + ":" + (p.getAlba().minuto<10?"0"+p.getAlba().minuto:p.getAlba().minuto));
        }else{
            albaTv.setText("nd");
        }
        TextView tramontoTv = (TextView)view.findViewById(R.id.tramonto);
        if(p.getTramonto() != null) {
            tramontoTv.setText(p.getTramonto().ora + ":" + (p.getTramonto().minuto<10?"0"+p.getTramonto().minuto:p.getTramonto().minuto));
        }else{
            tramontoTv.setText("nd");
        }
        TextView albaNoMontagneTv = (TextView)view.findViewById(R.id.albaNoMontagne);
        albaNoMontagneTv.setText("Alba all' orizzonte: ora "+p.albaNoMontagne.getHours() + ":" + p.albaNoMontagne.getMinutes());
        TextView tramontoNoMontagneTv = (TextView)view.findViewById(R.id.tramontoNoMontagne);
        tramontoNoMontagneTv.setText("Tramonto all' orizzonte: ora "+p.tramontoNoMontagne.getHours() + ":" + p.tramontoNoMontagne.getMinutes());
        TextView minSoleTv = (TextView)view.findViewById(R.id.minutiSole);
        minSoleTv.setText("Ore di Sole: " + p.minutiSole/60 + " ore, "+ (p.minutiSole-(p.minutiSole/60)*60)+ " minuti");
        TextView latitudineTv = (TextView)view.findViewById(R.id.latitudine);
        latitudineTv.setText("Latitudine: "+p.lat);
        TextView longitudineTv = (TextView)view.findViewById(R.id.longitudine);
        longitudineTv.setText("Longitudine: "+p.lon);
        TextView dataTv = (TextView)view.findViewById(R.id.data);
        dataTv.setText((String) DateFormat.format("dd",p.data)+"/"+ (String) DateFormat.format("MM",p.data)+"/"+ (String) DateFormat.format("yyyy",p.data));
    }


}
