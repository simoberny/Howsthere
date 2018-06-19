package it.unitn.simob.howsthere.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.twitter.sdk.android.core.models.TwitterCollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.CheckedOutputStream;

import it.unitn.simob.howsthere.Adapter.StoricoAdapter;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;
import it.unitn.simob.howsthere.Risultati;
import it.unitn.simob.howsthere.RisultatiActivity;


public class HistoryFragment extends Fragment{

    //static List selezionati_posiz = new ArrayList();
    List<String> selezionati_id = new ArrayList<String>();
    boolean in_selezione = false;

    public HistoryFragment() {}
    public StoricoAdapter adapter;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.bar_storico, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete) {
            if(in_selezione) {
                //adapter.onClick_menu();
                for (int i = 0; i < selezionati_id.size(); i++) {
                    System.err.println("     selezionati:  " + selezionati_id.get(i));
                    PanoramiStorage p = PanoramiStorage.panorami_storage;
                    p.delete_by_id(selezionati_id.get(i));
                }
                adapter.notifyDataSetChanged();
                selezionati_id.clear();
                in_selezione = false;
                return true;
            }else{
                Snackbar.make(getActivity().findViewById(R.id.layout_base),
                        "Nessun elemento selezionato.... long_press per selezionare", Snackbar.LENGTH_LONG).setAction("Elimina Tutto", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PanoramiStorage.panorami_storage.delete_all();
                        adapter.notifyDataSetChanged();
                        selezionati_id.clear();
                        in_selezione = false;
                    }
                }).show();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_history, container, false);
        // riscrivere la barra con la barra di sistema
        Toolbar bar = view.findViewById(R.id.history_toolbar);
        bar.showOverflowMenu();
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).setSupportActionBar(bar);

        FloatingActionButton new_pan = view.findViewById(R.id.new_pan);
        new_pan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView navigation  = (getActivity()).findViewById(R.id.navigation);
                navigation.setSelectedItemId(R.id.navigation_home);
            }
        });

        LinearLayout nofeed = view.findViewById(R.id.nofeed);
        ListView r = (ListView) view.findViewById(R.id.storico_lista);
        List<Panorama> list = PanoramiStorage.panorami_storage.getAllPanorama();
        adapter = new StoricoAdapter(view.getContext(), R.layout.singolo_storico, list,selezionati_id);
        r.setAdapter(adapter);

        r.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                TextView ID = view.findViewById(R.id.ID);
                String idi = (String) ID.getText();
                ImageView v = view.findViewById(R.id.spunta);
                if(!in_selezione) {
                    Intent i = new Intent(getActivity(), RisultatiActivity.class);
                    i.putExtra("ID", idi);
                    startActivity(i);

                }else if(selezionati_id.contains(idi)){
                    selezionati_id.remove(idi);
                    v.setVisibility(View.INVISIBLE);
                    if(selezionati_id.size()==0){
                        in_selezione = false;
                    }

                }else{
                    selezionati_id.add(idi);
                    v.setVisibility(View.VISIBLE);

                }
            }
        });

        r.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                ImageView v = view.findViewById(R.id.spunta);
                v.setVisibility(View.VISIBLE);
                TextView ID = view.findViewById(R.id.ID);
                String idi = (String) ID.getText();

                selezionati_id.add(idi);
                //selezionati_id.add(idi);

                //System.err.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE posizione : "+idi+"   arraysize: "+selezionati_id.size());
                in_selezione = true;
                return true;
            }
        });

        if(list.size() > 0){
            nofeed.setVisibility(View.GONE);
        }else{
            nofeed.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }




}
