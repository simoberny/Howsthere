package it.unitn.lpmt.howsthere.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.RisultatiActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonFragment extends Fragment {
    private Panorama p;
    private String id;

    private FirebaseAuth mAuth;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int GALLERY_INTENT = 25;
    public static final int CAMERA_INTENT = 26;
    private String mCurrentPhotoPath;
    private FrameLayout main = null;

    LineChart chart = null;

    public MoonFragment() {
        // Required empty public constructor
    }

    public static MoonFragment newInstance(){
        MoonFragment sf = new MoonFragment();
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
        final View view = inflater.inflate(R.layout.fragment_moon, container, false);

        p = ((RisultatiActivity)getActivity()).p;

        if(p!=null) {
            final Button apparizioniLunaButton = view.findViewById(R.id.apparizioniLunaButton);
            final Button sparizioniLunaButton = view.findViewById(R.id.sparizioniLunaButton);

            final CardView apparizioniCard = view.findViewById(R.id.apparizioniCard_luna);
            final CardView sparizioniCard = view.findViewById(R.id.sparizioniCard_luna);

            if (p.albeLuna.size() > 1) {
                apparizioniCard.setVisibility(View.VISIBLE);
            }
            apparizioniLunaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout apparizioniLunaLayout = view.findViewById(R.id.apparizioniLunaLayout);
                    TextView apparizioniLunaTx = view.findViewById(R.id.apparizioniLunaTx);

                    if (apparizioniLunaLayout.isExpanded() == false && p != null) {
                        apparizioniLunaTx.setText("");
                        for (int i = 0; i < p.albeLuna.size(); i++) {
                            apparizioniLunaTx.append("" + p.albeLuna.get(i).ora + ":" + p.albeLuna.get(i).minuto + '\n');
                        }
                    }
                    apparizioniLunaLayout.toggle();
                }
            });

            //menù espandibile sparizioni sole
            if (p.tramontiLuna.size() > 1) {
                sparizioniCard.setVisibility(View.VISIBLE);
            }
            sparizioniLunaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout sparizioniLunaLayout = view.findViewById(R.id.sparizioniLunaLayout);
                    TextView sparizioniLunaTx = view.findViewById(R.id.sparizioniLunaTx);
                    if (sparizioniLunaLayout.isExpanded() == false && p != null) {
                        sparizioniLunaTx.setText("");
                        for (int i = 0; i < p.tramontiLuna.size(); i++) {
                            sparizioniLunaTx.append("" + p.tramontiLuna.get(i).ora + ":" + p.tramontiLuna.get(i).minuto + '\n');
                        }
                    }
                    sparizioniLunaLayout.toggle();
                }
            });
        }

        final String yyyy = (String) DateFormat.format("yyyy",p.data);
        final String mm = (String)DateFormat.format("MM",p.data);
        final String gg = (String)DateFormat.format("dd",p.data);

        Button saveAlbaLuna = view.findViewById(R.id.saveAlbaLuna);
        saveAlbaLuna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.valueOf(yyyy), Integer.valueOf(mm), Integer.valueOf(gg), p.getAlbaLuna().ora, p.getAlbaLuna().minuto);
                long startmillis = cal.getTimeInMillis();
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + "," + p.lon);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", startmillis);
                intent.putExtra("allDay", false);
                intent.putExtra("rrule", "FREQ=YEARLY");
                intent.putExtra("endTime", startmillis+60*60*1000);
                intent.putExtra("title", "Scatto foto luna all'alba");
                startActivity(intent);
            }
        });

        Button saveTramontoLuna = view.findViewById(R.id.saveTramontoLuna);
        saveTramontoLuna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.valueOf(yyyy), Integer.valueOf(mm), Integer.valueOf(gg), p.getAlbaLuna().ora, p.getAlbaLuna().minuto);
                long startmillis = cal.getTimeInMillis();
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + "," + p.lon);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", startmillis);
                intent.putExtra("allDay", true);
                intent.putExtra("rrule", "FREQ=YEARLY");
                intent.putExtra("endTime", startmillis+60*60*1000);
                intent.putExtra("title", "Scatto foto luna al tramonto");
                startActivity(intent);
            }
        });

        chart = view.findViewById(R.id.chart_m);
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
        List<Entry> entriesLuna = new ArrayList<Entry>();

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

        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinValue(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinValue(-1);

        LineDataSet dataSetMontagne = new LineDataSet(entriesMontagne, getContext().getResources().getString(R.string.mountain)); // add entries to dataset
        LineDataSet dataSetLuna = new LineDataSet(entriesLuna, getContext().getResources().getString(R.string.risultati_luna));

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

        //proprietà grafiche Luna
        dataSetLuna.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetLuna.setColor(Color.LTGRAY);
        dataSetLuna.setLineWidth(1f);
        dataSetLuna.setDrawValues(true);
        dataSetLuna.setDrawCircles(true);
        //dataSetLuna.setCircleColor(Color.GRAY);
        dataSetLuna.setDrawCircleHole(false);
        dataSetLuna.setDrawValues(true);
        dataSetLuna.setDrawFilled(false);
        dataSetLuna.setDrawHighlightIndicators(false);
        dataSetLuna.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int idx = chart.getLineData().getDataSetByIndex(dataSetIndex).getEntryIndex(entry);
                if (idx>23 && idx <48) {
                    return String.valueOf(idx % 24);
                }else{
                    return "";
                }
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
        dataSetLuna.setCircleColors(coloricerchiLuna);

        chart.getDescription().setText("");
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetMontagne);
        lineData.addDataSet(dataSetLuna);
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
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);

        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0,5));

        chart.setData(lineData);
        chart.animateX(3500);
        chart.invalidate();
    }

    void stampaValoriBase(View view){
        /*
        TextView albaTv = (TextView)view.findViewById(R.id.oraAlbaLuna);
        albaTv.setText(p.getAlba().ora + ":" + p.getAlba().minuto);
        TextView tramontoTv = (TextView)view.findViewById(R.id.oraTramontoLuna);
        tramontoTv.setText(p.getTramonto().ora+ ":" + p.getTramonto().minuto);
        TextView albaNoMontagneTv = (TextView)view.findViewById(R.id.albaOrizzonteLuna);
        albaNoMontagneTv.setText("Alba all' orizzonte: ora "+p.albaNoMontagne.getHours() + ":" + p.albaNoMontagne.getMinutes());
        TextView tramontoNoMontagneTv = (TextView)view.findViewById(R.id.tramontoNoMontagne);
        tramontoNoMontagneTv.setText("Tramonto all' orizzonte: ora "+p.tramontoNoMontagne.getHours() + ":" + p.tramontoNoMontagne.getMinutes());
        TextView dataTv = (TextView)view.findViewById(R.id.data_m);
        dataTv.setText((String) DateFormat.format("dd",p.data)+"/"+ (String) DateFormat.format("MM",p.data)+"/"+ (String) DateFormat.format("yyyy",p.data));
        TextView frazioneLunaTv = (TextView)view.findViewById(R.id.frazioneLuna);
        frazioneLunaTv.setText("percentuale luna: "+(int)p.percentualeLuna +"%");
        TextView faseLunaTv = (TextView)view.findViewById(R.id.faseLuna);
        faseLunaTv.setText("fase luna(0=nuova,50=piena): "+(int)p.faseLuna);
        */
        TextView albaTv = (TextView) view.findViewById(R.id.oraAlbaLuna);
        if (p.getAlbaLuna() != null) {
            albaTv.setText(p.getAlbaLuna().ora + ":" + (p.getAlbaLuna().minuto < 10 ? "0" + p.getAlbaLuna().minuto : p.getAlbaLuna().minuto));
            ((TextView) view.findViewById(R.id.azimutAlbaLuna)).setText(new DecimalFormat("##.##").format(p.getAlbaLuna().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneAlbaLuna)).setText(new DecimalFormat("##.##").format(p.getAlbaLuna().altezza));
        } else {
            albaTv.setText("nd");
        }
        ((TextView) view.findViewById(R.id.albaOrizzonteLuna)).setText((String) DateFormat.format("HH", p.albaLunaNoMontagne) + ":" + (String) DateFormat.format("mm", p.albaLunaNoMontagne)+ ":" + (String) DateFormat.format("dd", p.albaLunaNoMontagne));


        //card tramonto sole
        TextView tramontoTv = (TextView) view.findViewById(R.id.oraTramontoLuna);
        if (p.getTramontoLuna() != null) {
            tramontoTv.setText(p.getTramontoLuna().ora + ":" + (p.getTramontoLuna().minuto < 10 ? "0" + p.getTramontoLuna().minuto : p.getTramontoLuna().minuto));
            ((TextView) view.findViewById(R.id.azimutTramontoLuna)).setText(new DecimalFormat("##.##").format(p.getTramontoLuna().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneTramontoLuna)).setText(new DecimalFormat("##.##").format(p.getTramontoLuna().altezza));
        } else {
            tramontoTv.setText("nd");
        }
        ((TextView) view.findViewById(R.id.tramontoOrizzonteLuna)).setText((String) DateFormat.format("HH", p.tramontoLunaNoMontagne) + ":" + (String) DateFormat.format("mm", p.tramontoLunaNoMontagne)+ ":" + (String) DateFormat.format("dd", p.tramontoLunaNoMontagne));
        ((TextView) view.findViewById(R.id.minutiLunaMontagne)).setText("" + p.minutiLuna/60 + ":"+((p.minutiLuna%60) < 10 ? ("0" + (p.minutiLuna%60)) : (p.minutiLuna%60)));
        ((TextView) view.findViewById(R.id.data_m)).setText((String) DateFormat.format("dd", p.data) + "/" + (String) DateFormat.format("MM", p.data) + "/" + (String) DateFormat.format("yyyy", p.data));
        ((TextView) view.findViewById(R.id.faselunare)).setText(p.faseLuna+"");
        ((TextView) view.findViewById(R.id.luceluna)).setText(p.percentualeLuna +"%");
    }
}
