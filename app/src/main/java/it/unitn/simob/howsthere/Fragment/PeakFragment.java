package it.unitn.simob.howsthere.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.gestures.GestureDetector;

import java.util.ArrayList;

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

    float currentDegree = 0f;
    SensorManager mSensorManager;
    float angoloDaSotrarre = 0;

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
        Peak highest = new Peak("Highest", 0.0, 0.0);

        p = ((RisultatiActivity)getActivity()).p;

        RecyclerView lista = view.findViewById(R.id.lista_montagne);
        LinearLayoutManager mLayoutManager = new MyLinearLayoutManager(getActivity());
        lista.setLayoutManager(mLayoutManager);
        adapter = new PeakFragment.PeakAdapter(getActivity(), listItems);
        lista.setAdapter(adapter);

        //Carico la lista dei nomi delle montagne nella lista
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

}
