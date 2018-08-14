package it.unitn.lpmt.howsthere.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SunFragment extends Fragment {
    private Panorama p;
    private String id;
    private FirebaseAuth mAuth;
    private CoordinatorLayout main = null;

    LineChart chart = null;
    static View currentView;

    public SunFragment() { }

    public static SunFragment newInstance(){
        SunFragment sf = new SunFragment();
        return sf;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_sun, container, false);
        currentView = view;

        p = ((RisultatiActivity)getActivity()).p;
        id = p.ID;
        //menù espandibile apparizioni sole
        if(p!=null) {
            final Button apparizioniSoleButton = currentView.findViewById(R.id.apparizioniSoleButton);
            final Button sparizioniSoleButton = currentView.findViewById(R.id.sparizioniSoleButton);

            final CardView apparizioniCard = currentView.findViewById(R.id.apparizioniCard);
            final CardView sparizioniCard = currentView.findViewById(R.id.sparizioniCard);

            if (p.albe.size() > 1) {
                apparizioniCard.setVisibility(View.VISIBLE);
            }
            apparizioniSoleButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout apparizioniSoleLayout = currentView.findViewById(R.id.apparizioniSoleLayout);
                    TextView apparizioniSoleTx = currentView.findViewById(R.id.apparizioniSoleTx);
                    if (apparizioniSoleLayout.isExpanded() == false && p != null) {
                        apparizioniSoleTx.setText("");
                        for (int i = 0; i < p.albe.size(); i++) {
                            apparizioniSoleTx.append("" + p.albe.get(i).ora + ":" + p.albe.get(i).minuto + '\n');
                        }
                    }
                    apparizioniSoleLayout.toggle();
                }
            });

            //menù espandibile sparizioni sole
            if (p.tramonti.size() > 1) {
                sparizioniCard.setVisibility(View.VISIBLE);
            }
            sparizioniSoleButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout sparizioniSoleLayout = currentView.findViewById(R.id.sparizioniSoleLayout);
                    TextView sparizioniSoleTx = currentView.findViewById(R.id.sparizioniSoleTx);
                    if (sparizioniSoleLayout.isExpanded() == false && p != null) {
                        sparizioniSoleTx.setText("");
                        for (int i = 0; i < p.tramonti.size(); i++) {
                            sparizioniSoleTx.append("" + p.tramonti.get(i).ora + ":" + p.tramonti.get(i).minuto + '\n');
                        }
                    }
                    sparizioniSoleLayout.toggle();
                }
            });
        }

        chart = view.findViewById(R.id.chart);
        main = view.findViewById(R.id.risultatiMainLayout);

        final String yyyy = (String) DateFormat.format("yyyy",p.data);
        final String mm = (String)DateFormat.format("MM",p.data);
        final String gg = (String)DateFormat.format("dd",p.data);

        Button saveAlba = view.findViewById(R.id.saveAlba);
        saveAlba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.valueOf(yyyy), Integer.valueOf(mm), Integer.valueOf(gg), p.getAlba().ora, p.getAlba().minuto);
                long startmillis = cal.getTimeInMillis();
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + ", " + p.lon);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startmillis);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startmillis+60*60*1000);
                intent.putExtra(CalendarContract.Events.TITLE, "Scatto foto all'alba");
                startActivity(intent);
            }
        });

        Button saveTramonto = view.findViewById(R.id.saveTramonto);
        saveTramonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(Integer.valueOf(yyyy), Integer.valueOf(mm), Integer.valueOf(gg), p.getAlba().ora, p.getAlba().minuto);
                long startmillis = cal.getTimeInMillis();
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "" + p.lat + "," + p.lon);
                intent.putExtra("beginTime", startmillis);
                intent.putExtra("allDay", true);
                intent.putExtra("rrule", "FREQ=YEARLY");
                intent.putExtra("endTime", startmillis+60*60*1000);
                intent.putExtra("title", "Scatto foto al tramonto");
                startActivity(intent);
            }
        });


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        main.setMinimumHeight(size.y);

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

        //proprietà grafico:
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinValue(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinValue(-1);

        LineDataSet dataSetMontagne = new LineDataSet(entriesMontagne, getContext().getResources().getString(R.string.mountain)); // add entries to dataset
        final LineDataSet dataSetSole = new LineDataSet(entriesSole, getContext().getResources().getString(R.string.sole));

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
        l.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT);
        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0,5));

        chart.setData(lineData);
        chart.animateX(3500);
        chart.invalidate();
    }

    void stampaValoriBase(View view){
        //card alba sole
        TextView albaTv = view.findViewById(R.id.oraAlbaSole);

        if (p.getAlba() != null) {
            albaTv.setText(p.getAlba().ora + ":" + (p.getAlba().minuto < 10 ? "0" + p.getAlba().minuto : p.getAlba().minuto));
            ((TextView) view.findViewById(R.id.azimutAlbaSole)).setText(new DecimalFormat("##.##").format(p.getAlba().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneAlbaSole)).setText(new DecimalFormat("##.##").format(p.getAlba().altezza));
        } else {
            albaTv.setText("nd");
        }
        ((TextView) view.findViewById(R.id.albaOrizzonteSole)).setText(DateFormat.format("HH", p.albaNoMontagne) + ":" + DateFormat.format("mm", p.albaNoMontagne)+ ":" + DateFormat.format("dd", p.albaNoMontagne));

        //card tramonto sole
        TextView tramontoTv = view.findViewById(R.id.oraTramontoSole);
        if (p.getTramonto() != null) {
            tramontoTv.setText(p.getTramonto().ora + ":" + (p.getTramonto().minuto < 10 ? "0" + p.getTramonto().minuto : p.getTramonto().minuto));
            ((TextView) view.findViewById(R.id.azimutTramontoSole)).setText(new DecimalFormat("##.##").format(p.getTramonto().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneTramontoSole)).setText(new DecimalFormat("##.##").format(p.getTramonto().altezza));
        } else {
            tramontoTv.setText("nd");
        }
        ((TextView) view.findViewById(R.id.tramontoOrizzonteSole)).setText(DateFormat.format("HH", p.tramontoNoMontagne) + ":" + DateFormat.format("mm", p.tramontoNoMontagne)+ ":" + DateFormat.format("dd", p.tramontoNoMontagne));

        ((TextView) view.findViewById(R.id.minutiSoleMontagne)).setText("" + p.minutiSole/60 + ":"+((p.minutiSole%60) < 10 ? ("0" + (p.minutiSole%60)) : (p.minutiSole%60)));
        int minutiSoleNoMontagne = (int)(1440-((Math.abs(p.tramontoNoMontagne.getTime() - p.albaNoMontagne.getTime()))/60000));

        ((TextView) view.findViewById(R.id.minutiSoleNoMontagne)).setText("" + minutiSoleNoMontagne/60 + ":" + ((minutiSoleNoMontagne%60) < 10 ? ("0" + (minutiSoleNoMontagne%60)) : (minutiSoleNoMontagne%60)));
        ((TextView) view.findViewById(R.id.data)).setText(DateFormat.format("dd", p.data) + "/" + DateFormat.format("MM", p.data) + "/" + DateFormat.format("yyyy", p.data));
    }
}
