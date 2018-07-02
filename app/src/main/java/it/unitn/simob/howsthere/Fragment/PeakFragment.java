package it.unitn.simob.howsthere.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.unitn.simob.howsthere.Adapter.MyLinearLayoutManager;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.Peak;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.RisultatiActivity;

public class PeakFragment extends Fragment {
    ArrayList<Peak> listItems=new ArrayList<Peak>();
    PeakFragment.PeakAdapter adapter;

    Panorama p;

    public PeakFragment() { }

    public static PeakFragment newInstance(){
        return new PeakFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_peak, container, false);

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
        }
        adapter.notifyDataSetChanged();

        return view;
    }

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
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Peak temp = array.get(position);
            holder.peak_name.setText(temp.getNome_picco());
            holder.peak_altitude.setText(temp.getAltezza() + "°");
            holder.azimuth.setText(temp.getAzimuth() + "° N");
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

}
