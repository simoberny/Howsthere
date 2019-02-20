package it.unitn.lpmt.howsthere.Adapter;

/**
 * Created by matteo on 21/05/18.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.RisultatiActivity;

public class StoricoAdapter extends RecyclerView.Adapter<StoricoAdapter.MyViewHolder>{

    public List<Panorama> l = null;
    Context context;
    private List<String> selezionati_id = new ArrayList<String>();
    boolean in_selezione = false;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome_citta, data;
        ImageView anteprima;
        CheckBox selectable;
        LineChart chart;
        View overlay;

        public MyViewHolder(final View view) {
            super(view);
            nome_citta = view.findViewById(R.id.nome_citta);
            data = view.findViewById(R.id.data);
            anteprima = view.findViewById(R.id.anteprima);
            selectable = view.findViewById(R.id.selectable);
            overlay = view.findViewById(R.id.overlay);
            chart = view.findViewById(R.id.chart_storico);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String selected_id = l.get(getAdapterPosition()).ID;
                    CheckBox v = view.findViewById(R.id.selectable);
                    if(!in_selezione) {
                        Intent i = new Intent(context, RisultatiActivity.class);
                        i.putExtra("ID", selected_id);
                        context.startActivity(i);
                    }else if(selezionati_id.contains(selected_id)){
                        selezionati_id.remove(selected_id);
                        view.findViewById(R.id.overlay).setVisibility(View.GONE);
                        v.setVisibility(View.GONE);
                        //view.findViewById(R.id.spunta).setVisibility(View.GONE); //Spunta Matteo
                        v.setChecked(false);

                        if(selezionati_id.size()==0){
                            in_selezione = false;
                        }
                    }else{
                        view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                        v.setVisibility(View.VISIBLE);
                        //view.findViewById(R.id.spunta).setVisibility(View.VISIBLE); //Spunta Matteo
                        v.setChecked(true);
                        selezionati_id.add(selected_id);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String selected_id = l.get(getAdapterPosition()).ID;
                    if(!selezionati_id.contains(selected_id)) {
                        CheckBox v = view.findViewById(R.id.selectable);
                        v.setVisibility(View.VISIBLE);
                        v.setChecked(true);
                        selezionati_id.add(selected_id);

                        view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);

                        in_selezione = true;
                    }
                    return true;
                }
            });
        }
    }

    public StoricoAdapter(Context context, List<Panorama> objects) {
        this.selezionati_id = selezionati_id;
        this.l = objects;
        this.context = context;
    }

    @Override
    public StoricoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.singolo_storico, parent, false);
        return new StoricoAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final StoricoAdapter.MyViewHolder holder, final int position) {
        Panorama p = l.get(position);

        holder.nome_citta.setText(p.citta + " ");
        Glide.with(context)
                .load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon + "&key=AIzaSyAnxgK9W1tUZ7kkItOJr1kQPQ5BpKQlEcY")
                .placeholder(R.drawable.nomap)
                .into(holder.anteprima);
        //Picasso.get().load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=200x250&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon).placeholder(R.drawable.nomap).into(anteprima);

        String d = (String) DateFormat.format("dd",p.data)+"/"+ (String) DateFormat.format("MM",p.data)+"/"+ (String) DateFormat.format("yyyy",p.data);
        holder.data.setText(d);

        if (selezionati_id.contains(p.ID)){
            holder.overlay.setVisibility(View.VISIBLE);
            holder.selectable.setVisibility(View.VISIBLE);
            holder.selectable.setChecked(true);
        }

        new ShowChart(p, holder.chart).execute();
    }

    @Override
    public int getItemCount() {
        return l.size();
    }

    public void clearSelezionati(){
        this.selezionati_id.clear();
    }

    public List<String> getSelezionati(){
        return this.selezionati_id;
    }

    public void setInSelezione(boolean selezione){
        this.in_selezione = selezione;
    }

    public boolean getInSelezione(){
        return this.in_selezione;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    class ShowChart extends AsyncTask<Void, Void, Void>{

        private LineChart chart;
        private Panorama p;

        public ShowChart(Panorama p, LineChart chart){
            this.chart = chart;
            this.p = p;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            stampaGrafico(this.p, this.chart);
            return null;
        }
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

