package it.unitn.lpmt.howsthere.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.Results;
import it.unitn.lpmt.howsthere.objects.Panorama;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context aContext;
    private List<Panorama> list;
    private List<String> selected_ids = new ArrayList<String>();
    boolean selection = false;

    public HistoryAdapter(Context activity, List<Panorama> list) {
        this.aContext = activity;
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView city, date;
        ImageView preview;
        CheckBox selectable;
        LineChart chart;
        View overlay;

        public ViewHolder(View v) {
            super(v);

            city = v.findViewById(R.id.nome_citta);
            date = v.findViewById(R.id.data);
            preview = v.findViewById(R.id.anteprima);
            selectable = v.findViewById(R.id.selectable);
            overlay = v.findViewById(R.id.overlay);
            chart = v.findViewById(R.id.chart_storico);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selected_id = list.get(getAdapterPosition()).ID;

                    CheckBox selected_check = v.findViewById(R.id.selectable);

                    if(!selection) {
                        Intent i = new Intent(aContext, Results.class);
                        i.putExtra("ID", selected_id);
                        aContext.startActivity(i);
                    }else if(selected_ids.contains(selected_id)){
                        selected_ids.remove(selected_id);

                        v.findViewById(R.id.overlay).setVisibility(View.GONE);
                        selected_check.setVisibility(View.GONE);
                        selected_check.setChecked(false);

                        if(selected_ids.size()==0){
                            selection = false;
                        }
                    }else{
                        v.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                        selected_check.setVisibility(View.VISIBLE);
                        selected_check.setChecked(true);
                        selected_ids.add(selected_id);
                    }
                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String selected_id = list.get(getAdapterPosition()).ID;
                    if(!selected_ids.contains(selected_id)) {
                        CheckBox selection_check = v.findViewById(R.id.selectable);
                        selection_check.setVisibility(View.VISIBLE);
                        selection_check.setChecked(true);
                        selected_ids.add(selected_id);

                        v.findViewById(R.id.overlay).setVisibility(View.VISIBLE);

                        selection = true;
                    }

                    return true;
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.history_element, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        Panorama p = list.get(position);

        holder.city.setText(p.city + " ");

        Glide.with(aContext)
                .load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon + "&key=AIzaSyAnxgK9W1tUZ7kkItOJr1kQPQ5BpKQlEcY")
                .placeholder(R.drawable.nomap)
                .into(holder.preview);

        String d = (String) DateFormat.format("dd", p.date)+"/"+ (String) DateFormat.format("MM", p.date)+"/"+ (String) DateFormat.format("yyyy", p.date);
        holder.date.setText(d);

        if (selected_ids.contains(p.ID)){
            holder.overlay.setVisibility(View.VISIBLE);
            holder.selectable.setVisibility(View.VISIBLE);
            holder.selectable.setChecked(true);
        }else{
            holder.overlay.setVisibility(View.GONE);
            holder.selectable.setVisibility(View.GONE);
            holder.selectable.setChecked(false);
        }

        new ShowChart(p, holder.chart).execute();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clearSelected(){
        this.selected_ids.clear();
        this.setSelectionMode(false);
    }

    public List<String> getSelected(){
        return this.selected_ids;
    }

    public void setSelectionMode(boolean selectionState){
        this.selection = selectionState;
    }

    public boolean getSelectionMode(){
        return this.selection;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    class ShowChart extends AsyncTask<Void, Void, Void> {
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

        Drawable drawable = ContextCompat.getDrawable(aContext, R.drawable.fade_mountain);
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
        chart.setData(lineData);
        chart.invalidate();
    }
}