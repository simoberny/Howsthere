package it.unitn.simob.howsthere.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.MyLinearLayoutManager;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.Peak;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.RisultatiActivity;

import static android.content.Context.SENSOR_SERVICE;

public class PeakFragment extends Fragment implements SensorEventListener {
    ArrayList<Peak> listItems=new ArrayList<Peak>();
    PeakFragment.PeakAdapter adapter;
    Panorama p;
    AlertDialog.Builder builder;
    private Boolean isShowing = false;
    LineChart chart = null;

    float currentDegree = 0f;
    SensorManager mSensorManager;
    float angoloDaSotrarre = 0;
    Peak highest = new Peak("Highest", 0.0, 0.0);

    private ImageView compass_img  = null;

    public PeakFragment() { }

    public static PeakFragment newInstance(){
        return new PeakFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_peak, container, false);

        final TextView high_nome = view.findViewById(R.id.highest_nome);
        final TextView high_alt = view.findViewById(R.id.highest_alt);

        //Picco temporaneo per salvare quello più alto
        p = ((RisultatiActivity)getActivity()).p;

        //chiamo grafico
        chart = view.findViewById(R.id.chart_peak);
        if(p != null){
            stampaGrafico();
        }else{
            getActivity().finish();
        }

        RecyclerView lista = view.findViewById(R.id.lista_montagne);
        LinearLayoutManager mLayoutManager = new MyLinearLayoutManager(getActivity());
        lista.setLayoutManager(mLayoutManager);
        adapter = new PeakFragment.PeakAdapter(getActivity(), listItems);
        lista.setAdapter(adapter);

        //Carico la lista dei nomi delle montagne nella lista
        listItems.clear();
        for(int i = 0; i < p.nomiPeak.size(); i++){
            Peak temp = p.nomiPeak.get(i);
            listItems.add(temp);

            if(temp.getAltezza() > highest.getAltezza()){
                highest = temp;
            }
        }
        adapter.notifyDataSetChanged();

        //Setto i valori della montagna più alta
        high_nome.setText(highest.getNome_picco());
        high_alt.setText(highest.getAltezza() + " °");


        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        if(builder != null && isShowing){
            RotateAnimation ra = new RotateAnimation(
                    currentDegree+angoloDaSotrarre,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(210);
            ra.setFillAfter(true);
            compass_img.startAnimation(ra);
        }

        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    //!!!!!!!!inizio peak adapter classe
    //Adattatore compatto per la lista dei picchi
    public class PeakAdapter extends RecyclerView.Adapter<PeakAdapter.MyViewHolder>{
        private Context context;
        ArrayList<Peak> array;

        public PeakAdapter(Context context, ArrayList<Peak> array) {
            this.context = context;
            this.array = array;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.elemento_peak, parent, false);
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Peak temp = array.get(position);
            holder.peak_name.setText(temp.getNome_picco());
            holder.peak_altitude.setText(temp.getAltezza() + "°");
            holder.azimuth.setText(temp.getAzimuth() + "° N");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    create_dialog(temp.getAzimuth(), temp.getNome_picco());
                }
            });
        }

        public void create_dialog(double azimuth, String nome){
            View view = getLayoutInflater().inflate(R.layout.compass_dialog, null);
            angoloDaSotrarre = (float) azimuth;
            TextView textazi = view.findViewById(R.id.azimuth);
            textazi.setText(azimuth + " °");

            TextView peak = view.findViewById(R.id.name_peak);
            peak.setText(nome);

            compass_img = view.findViewById(R.id.dialog_compass);

            builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); isShowing=false;}
            });
            builder.setView(view);
            builder.show();
            isShowing = true;
        }

        @Override
        public int getItemCount() {
            return array.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder
        {
            TextView peak_name, peak_altitude, azimuth;

            public MyViewHolder(View itemView) {
                super(itemView);
                peak_name = itemView.findViewById(R.id.name);
                peak_altitude = itemView.findViewById(R.id.altitude);
                azimuth = itemView.findViewById(R.id.azimuth_peak);
            }
        }
    }

    //!!!!!!!!fine peak adapter classe

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    void stampaGrafico() {
        List<Entry> entriesMontagne = new ArrayList<Entry>();
        final List<Entry> entriesNum = new ArrayList<Entry>();
        //SOLE
        //Arrays.sort(p.risultatiSole); //ordino secondo azimuth
        for (int i = 0; i < p.nomiPeak.size(); i++) { //passo dati al grafico
            entriesNum.add(new Entry((float) p.nomiPeak.get(i).getAzimuth(), (float) p.nomiPeak.get(i).getAltezza()));

        }
        //MONTAGNE
        for (int i = 0; i < 360; i++) {
            entriesMontagne.add(new Entry((float) p.risultatiMontagne[0][i], (float) p.risultatiMontagne[2][i]));
        }

        //proprietà grafico:
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinValue(-1);  //faccio partire da -1 le y. non da 0 perchè da una montagna alta è possibile finire leggermente sotto lo 0
        chart.getAxisRight().setAxisMinValue(-1);

        chart.setVisibleXRangeMaximum(20); // allow 20 values to be displayed at once on the x-axis, not more
        chart.moveViewToX(10); // set the left edge of the chart to x-index 10

        LineDataSet dataSetMontagne = new LineDataSet(entriesMontagne, "Montagne"); // add entries to dataset
        final LineDataSet dataSetNum = new LineDataSet(entriesNum, "Num");

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

        //proprietà grafiche numeri
        dataSetNum.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetNum.setColor(Color.YELLOW);
        dataSetNum.setLineWidth(0);
        dataSetNum.setDrawValues(true);
        dataSetNum.setDrawCircles(false);
        dataSetNum.setCircleColor(Color.YELLOW);
        dataSetNum.setDrawCircleHole(false);
        dataSetNum.setDrawFilled(false);
        dataSetNum.setDrawValues(true);
        dataSetNum.setDrawHighlightIndicators(false);
        dataSetNum.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int idx = chart.getLineData().getDataSetByIndex(dataSetIndex).getEntryIndex(entry);
                return String.valueOf(idx);
            }
        });


        chart.getDescription().setText("");
        LineData lineData = new LineData();
        lineData.addDataSet(dataSetMontagne);
        lineData.addDataSet(dataSetNum);
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
        entrinseo.add(new Entry(0, 5));

        chart.setData(lineData);
        chart.animateX(3500);
        chart.invalidate();

    }


}
