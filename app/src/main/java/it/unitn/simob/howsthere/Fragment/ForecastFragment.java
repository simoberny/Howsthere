package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unitn.simob.howsthere.Adapter.WeatherRecyclerAdapter;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.MeteoActivity;
import it.unitn.simob.howsthere.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    private WeatherRecyclerAdapter adapter;
    public ForecastFragment() {
        // Required empty public constructor
    }

    public static ForecastFragment newInstance() {
        ForecastFragment fragment = new ForecastFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View view =  inflater.inflate(R.layout.fragment_forecast, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MeteoActivity met = (MeteoActivity) getActivity();
        recyclerView.setAdapter(met.adapter);
        return view;
    }

}
