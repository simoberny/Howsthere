package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.WeatherRecyclerAdapter;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.Weather.models.WeatherCompleto;


public class ForecastFragment extends Fragment implements Serializable{

    private WeatherRecyclerAdapter adapter;
    List<WeatherCompleto> forecast = new ArrayList<>();

    public ForecastFragment() {
    }

    public static ForecastFragment newInstance(List<WeatherCompleto> forecast) {
        ForecastFragment fragment = new ForecastFragment();
        Bundle bd = new Bundle();
        bd.putSerializable("forecast", (Serializable) forecast);
        fragment.setArguments(bd);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View view =  inflater.inflate(R.layout.fragment_forecast, container, false);

        Bundle bd = getArguments();
        forecast  = (List<WeatherCompleto>) bd.getSerializable("forecast");

        adapter = new WeatherRecyclerAdapter(getActivity(), forecast);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        return view;
    }

    public void update(List<WeatherCompleto> forecast){
        this.forecast = forecast;
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
