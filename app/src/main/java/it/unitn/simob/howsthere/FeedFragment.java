package it.unitn.simob.howsthere;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;

    public FeedFragment() {
    }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        feedList = new ArrayList<>(); //Inizializzazione lista dei feed
        adapter = new FeedAdapter(getActivity(), feedList); //Inizializzazione adapter per la lista

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        //Oggetti statici caricati nella feed
        Feed feed = new Feed("Simone Bernabè",
                "Metro Manila, Philippines",
                "https://campingselection-528047.c.cdn77.org/media/default/images/en-us/editorial-area/region-trentino.jpg",
                "3 DAYS AGO");
        feedList.add(0, feed);

        feed = new Feed("Matteo Dal Ponte",
                "Metro Manila, Philippines",
                "https://www.boscolo.com/it/viaggi/files/it_viaggi/styles/bootstrap_header/public/trentino-alto-adige-testata.jpg?itok=OEva-6rR",
                "1 DAY AGO");
        feedList.add(0, feed);

        feed = new Feed("Andrea Filippi",
                "Metro Manila, Philippines",
                "https://c1.staticflickr.com/5/4297/35852716531_e9f57be43b_b.jpg",
                "10 HOURS AGO");
        feedList.add(0, feed);

        feed = new Feed("Marco Rossi",
                "Metro Manila, Philippines",
                "https://media.istockphoto.com/photos/sun-at-the-sky-with-copy-space-picture-id155366999?k=6&m=155366999&s=612x612&w=0&h=EFpmVaOq8RHHN5q1fq1qHDhn_V_YS2Ex_Z7UDD2efEs=",
                "26 MINUTES AGO");
        feedList.add(0, feed);

        //Dico all'adattatore che sono stati aggiunti degli elementi
        adapter.notifyDataSetChanged();

        return view;
    }

}