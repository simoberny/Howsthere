package it.unitn.lpmt.howsthere.Adapter;

/**
 * Created by matteo on 21/05/18.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.R;

public class StoricoAdapter extends ArrayAdapter<Panorama>{

    public List<Panorama> l = null;
    private LineChart chart;
    Context context;
    private List<String> selezionati_id;

    public StoricoAdapter(Context context, int textViewResourceId, List<Panorama> objects, List<String> selezionati_id) {
        super(context, textViewResourceId, objects);
        this.selezionati_id = selezionati_id;
        l = objects;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.singolo_storico, null);
        Panorama p = l.get(position);

        TextView nome_citta = (TextView) convertView.findViewById(R.id.nome_citta);
        TextView data = (TextView) convertView.findViewById(R.id.data);
        ImageView anteprima = (ImageView) convertView.findViewById(R.id.anteprima);
        CheckBox selectable = convertView.findViewById(R.id.selectable);
        chart = (LineChart) convertView.findViewById(R.id.chart_storico);


        nome_citta.setText(p.citta+ " ");
        Picasso.get().load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=300x350&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon).placeholder(R.drawable.nomap).into(anteprima);

        String d = (String) DateFormat.format("dd",p.data)+"/"+ (String) DateFormat.format("MM",p.data)+"/"+ (String) DateFormat.format("yyyy",p.data);
        data.setText(getTempoStorico(d));

        if (selezionati_id.contains(p.ID)){
            convertView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
            selectable.setVisibility(View.VISIBLE);
            selectable.setChecked(true);
        }

        stampaGrafico(p,chart);

        return convertView;
    }

    private String getTempoStorico(String d){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        long diff = 0;
        boolean dataPassata = false;
        try {
            Date date = format.parse(d);
            if (new Date().getTime() - date.getTime() >= 0){ // mi segno se la data è futura o passata
                dataPassata = true;
            }
            long diffInMillies = Math.abs(new Date().getTime() - date.getTime());
            diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String tempo = "";

        if(dataPassata==true) {
            if (diff >= 60 && (diff / 60) < 24) {
                tempo = "oggi";
            } else if (diff >= 60 && (diff / 60) > 24 && (diff / 60) < 48) {
                tempo = "ieri";
            } else if (diff >= 60 && (diff / 60) > 48 && (diff / 60) < 3 * 30 * 24) {
                long temp = (diff / 60 / 24);
                tempo = temp + " giorn" + (((int) temp == 1) ? "o" : "i") + " fa";
            } else {
                tempo = d;
            }
        }else{
            if (diff >= 60 && (diff / 60) < 24) {
                tempo = "oggi";
            } else if (diff >= 60 && (diff / 60) > 24 && (diff / 60) < 48) {
                tempo = "domani";
            } else if (diff >= 60 && (diff / 60) > 48 && (diff / 60) < 3 * 30 * 24) {
                long temp = (diff / 60 / 24);
                tempo = "fra " + temp + " giorn" + (((int) temp == 1) ? "o" : "i");
            } else {
                tempo = d;
            }
        }

        return tempo;
    }

    void stampaGrafico(Panorama p, final LineChart chart){
        List<Entry> entriesMontagne = new ArrayList<Entry>();
        final List<Entry> entriesSole = new ArrayList<Entry>();
        List<Entry> entriesLuna = new ArrayList<Entry>();
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
        chart.getAxisLeft().setAxisMinimum(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinimum(-1);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

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

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.fade_red);
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
        l.setEnabled(false);

        // set custom labels and colors
        List<Entry> entrinseo = new ArrayList<Entry>();
        entrinseo.add(new Entry(0,5));
        //l.setCustom(entrinseo);
        chart.setData(lineData);
        chart.invalidate();
    }
}


