package it.unitn.lpmt.howsthere.fragment.result;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.Results;
import it.unitn.lpmt.howsthere.objects.Panorama;

public class MoonFragment extends Fragment {
    private Panorama p;
    private String id;

    private FrameLayout main = null;

    private LineChart chart = null;
    private static View currentView;

    public MoonFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView =  inflater.inflate(R.layout.fragment_moon, container, false);
        p = ((Results)getActivity()).getPanorama();

        if(p!=null) {
            final Button apparizioniLunaButton = currentView.findViewById(R.id.apparizioniLunaButton);
            final Button sparizioniLunaButton = currentView.findViewById(R.id.sparizioniLunaButton);

            final CardView apparizioniCard = currentView.findViewById(R.id.apparizioniCard_luna);
            final CardView sparizioniCard = currentView.findViewById(R.id.sparizioniCard_luna);


            if (p.albeLuna.size() > 1) {
                apparizioniCard.setVisibility(View.VISIBLE);
            }

            apparizioniLunaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout apparizioniLunaLayout = currentView.findViewById(R.id.apparizioniLunaLayout);
                    TextView apparizioniLunaTx = currentView.findViewById(R.id.apparizioniLunaTx);
                    if (apparizioniLunaLayout.isExpanded() == false && p != null) {
                        apparizioniLunaTx.setText("");
                        for (int i = 0; i < p.albeLuna.size(); i++) {
                            apparizioniLunaTx.append("" + p.albeLuna.get(i).ora + ":" + (p.albeLuna.get(i).minuto < 10 ? "0" + p.albeLuna.get(i).minuto : p.albeLuna.get(i).minuto) + '\n');
                        }
                    }
                    apparizioniLunaLayout.toggle();
                }
            });


            //menù espandibile sparizioni luna
            if (p.tramontiLuna.size() > 1) {
                sparizioniCard.setVisibility(View.VISIBLE);
            }
            sparizioniLunaButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ExpandableLayout sparizioniLunaLayout = currentView.findViewById(R.id.sparizioniLunaLayout);
                    TextView sparizioniLunaTx = currentView.findViewById(R.id.sparizioniLunaTx);
                    if (sparizioniLunaLayout.isExpanded() == false && p != null) {
                        sparizioniLunaTx.setText("");
                        for (int i = 0; i < p.tramontiLuna.size(); i++) {
                            sparizioniLunaTx.append("" + p.tramontiLuna.get(i).ora + ":" + (p.tramontiLuna.get(i).minuto < 10 ? "0" + p.tramontiLuna.get(i).minuto : p.tramontiLuna.get(i).minuto) + '\n');
                        }
                    }
                    sparizioniLunaLayout.toggle();
                }
            });
        }

        final String yyyy = (String) DateFormat.format("yyyy",p.date);
        final String mm = (String)DateFormat.format("MM",p.date);
        final String gg = (String)DateFormat.format("dd",p.date);

        Button saveAlbaLuna = currentView.findViewById(R.id.saveAlbaLuna);
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

        Button saveTramontoLuna = currentView.findViewById(R.id.saveTramontoLuna);
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

        chart = currentView.findViewById(R.id.chart_m);
        main = currentView.findViewById(R.id.moon_mainlayout);

        //Se per qualche motivo il panorama non è leggibile o non c'è chiudo l'attività
        if(p != null){
            stampaGrafico();
            stampaValoriBase(currentView); //informazioni sempre conosciute
        }else{
            getActivity().finish();
        }

        return currentView;
    }

    void stampaGrafico() {
        List<Entry> entriesMontagne = new ArrayList<Entry>();
        List<Entry> entriesLuna = new ArrayList<Entry>();

        //Arrays.sort(risultatiLuna); //ordino secondo azimuth ATTENZIONE: se vengono ordinati allora si mescolano i dati della mattina dopo quelli della sera
        for (int i = 288; i < 577; i++) { //passo dati al grafico solo del giorno corrente e se validi e passo anche la mezzanotte del giorno dopo
            if (p.risultatiLuna[i].minuto == 0) {
                if (p.risultatiLuna[i].altezza >= -20) {
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));
                } else {
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) -20));
                }
            }
        }

        //codice per mostrare anche la continuazione
        /*for(int i = 0; i<864; i++) { //passo dati al grafico
            if(p.risultatiLuna[i].minuto == 0){

                if(i<288 && p.risultatiLuna[288].azimuth > p.risultatiLuna[i].azimuth) { //solo le ore del giorno prima che concludono l' arco in cielo
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));
                }
                else if(i>575 && p.risultatiLuna[575].azimuth < p.risultatiLuna[i].azimuth) { //solo le ore del giorno dopo che concludono l' arco in cielo
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));
                }
                else if(i>=288 && i<576){
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) p.risultatiLuna[i].altezza));

                }else {
                    entriesLuna.add(new Entry((float) p.risultatiLuna[i].azimuth, (float) -90));
                }
            }
        }*/

        //MONTAGNE
        for (int i = 0; i < 360; i++) {
            entriesMontagne.add(new Entry((float) p.risultatiMontagne[0][i], (float) p.risultatiMontagne[2][i]));
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
        dataSetMontagne.setColor(R.color.cyan_500);
        //dataSet.setLineWidth(4f);
        dataSetMontagne.setDrawValues(false);
        dataSetMontagne.setDrawCircles(false);
        dataSetMontagne.setCircleColor(Color.BLACK);
        dataSetMontagne.setDrawCircleHole(false);
        dataSetMontagne.setDrawValues(false);
        dataSetMontagne.setDrawFilled(true);
        dataSetMontagne.setLineWidth(2f);

        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.fade_mountain);
        dataSetMontagne.setFillDrawable(drawable);
        dataSetMontagne.setDrawHighlightIndicators(false);

        //proprietà grafiche Luna
        dataSetLuna.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetLuna.setColor(Color.LTGRAY);
        dataSetLuna.setLineWidth(2f);
        dataSetLuna.setCircleRadius(4f);
        dataSetLuna.setDrawValues(true);
        dataSetLuna.setDrawCircles(true);
        //dataSetLuna.setCircleColor(Color.GRAY);
        dataSetLuna.setDrawCircleHole(false);
        dataSetLuna.setDrawValues(true);
        dataSetLuna.setDrawFilled(false);
        dataSetLuna.setDrawHighlightIndicators(false);
        dataSetLuna.setValueTextSize(9f);
        dataSetLuna.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int idx = chart.getLineData().getDataSetByIndex(dataSetIndex).getEntryIndex(entry);
                return String.valueOf(idx % 25);

                /*if (idx>23 && idx <48) { //per avere ore sul grafico solo sul giorno stesso e non sulle continuazioni
                    return String.valueOf(idx % 24);
                }else{
                    return "";
                }*/
            }
        });


        int[] coloricerchiLuna = new int[64]; //un colore per ogni dato sul grafico (24 al giorno)
        for (int i = 0; i < 24; i++) {
            coloricerchiLuna[i] = Color.argb(65, 158, 158, 158);
        }
        for (int i = 24; i < 48; i++) {
            coloricerchiLuna[i] = Color.GRAY;
        }
        for (int i = 48; i < 64; i++) {
            coloricerchiLuna[i] = Color.argb(65, 158, 158, 158);
        }
        //dataSetLuna.setCircleColors(coloricerchiLuna); abilitazione colori diversi per continuazione in grigetto traiettoria
        dataSetLuna.setCircleColors(Color.GRAY);
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
        l.setTextSize(16f);
        l.setTextColor(Color.BLACK);
        l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
        l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0, 5));

        chart.setData(lineData);
        chart.animateX(3500);
        chart.invalidate();
    }

    void stampaValoriBase(View view) {
        TextView albaTv = view.findViewById(R.id.oraAlbaLuna);
        if (p.getAlbaLuna() != null) {
            albaTv.setText(p.getAlbaLuna().ora + ":" + (p.getAlbaLuna().minuto < 10 ? "0" + p.getAlbaLuna().minuto : p.getAlbaLuna().minuto));
            ((TextView) view.findViewById(R.id.azimutAlbaLuna)).setText(new DecimalFormat("##.##").format(p.getAlbaLuna().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneAlbaLuna)).setText(new DecimalFormat("##.##").format(p.getAlbaLuna().altezza));
        } else {
            albaTv.setText("nd");
        }

        //card tramonto sole
        TextView tramontoTv = view.findViewById(R.id.oraTramontoLuna);
        if (p.getTramontoLuna() != null) {
            tramontoTv.setText(p.getTramontoLuna().ora + ":" + (p.getTramontoLuna().minuto < 10 ? "0" + p.getTramontoLuna().minuto : p.getTramontoLuna().minuto));
            ((TextView) view.findViewById(R.id.azimutTramontoLuna)).setText(new DecimalFormat("##.##").format(p.getTramontoLuna().azimuth));
            ((TextView) view.findViewById(R.id.elevazioneTramontoLuna)).setText(new DecimalFormat("##.##").format(p.getTramontoLuna().altezza));
        } else {
            tramontoTv.setText("nd");
        }

        if (p.tramontoLunaNoMontagne != null)
            ((TextView) view.findViewById(R.id.tramontoOrizzonteLuna)).setText(DateFormat.format("HH", p.tramontoLunaNoMontagne) + ":" + DateFormat.format("mm", p.tramontoLunaNoMontagne));
        //((TextView) view.findViewById(R.id.minutiLunaMontagne)).setText("" + p.minutiLuna/60 + ":"+((p.minutiLuna%60) < 10 ? ("0" + (p.minutiLuna%60)) : (p.minutiLuna%60)));
        ((TextView) view.findViewById(R.id.data_m)).setText(DateFormat.format("dd", p.date) + "/" + DateFormat.format("MM", p.date) + "/" + DateFormat.format("yyyy", p.date));

        /*   //determinazione stringa per fase lunare
        170 - 180 + -180 - -170     nuova luna
        -170 - -85                  luna crescente
        -85 - -95                   primo quarto
        -95 - -10                   Gibbosa crescente
        -10 - 10                    Luna piena
        10 - 85                     Gibbosa calante
        85 - 95                     ultimo quarto
        95 - 170                    luna calante
         */
        String nomeFaseLuna = "";
        if ((p.faseLuna >= 175 && p.faseLuna < 181) || (p.faseLuna >= -180 && p.faseLuna < -170))
            nomeFaseLuna = "Nuova luna";
        if (p.faseLuna >= -170 && p.faseLuna < -85) nomeFaseLuna = "Luna crescente";
        if (p.faseLuna >= -85 && p.faseLuna < -95) nomeFaseLuna = "Primo quarto";
        if (p.faseLuna >= -95 && p.faseLuna < -10) nomeFaseLuna = "Gibbosa crescente";
        if (p.faseLuna >= -10 && p.faseLuna < 10) nomeFaseLuna = "Luna piena";
        if (p.faseLuna >= 10 && p.faseLuna < 85) nomeFaseLuna = "Gibbosa calante";
        if (p.faseLuna >= 85 && p.faseLuna < 95) nomeFaseLuna = "Ultimo quarto";
        if (p.faseLuna >= 95 && p.faseLuna < 175) nomeFaseLuna = "Luna calante";

        ((TextView) view.findViewById(R.id.faselunare)).setText(nomeFaseLuna + " (" + Math.round(p.faseLuna*100)/100.0d + ") ");
        ((TextView) view.findViewById(R.id.luceluna)).setText((int) p.percentualeLuna + "%");
    }
}