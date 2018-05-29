package it.unitn.simob.howsthere.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class HistoryFragment extends Fragment{

    static List selezionati_posiz = new ArrayList();
    //List<String> selezionati_id = new ArrayList<String>(); //TODO: implementare il delete con ID invece che posizione!
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
            System.err.println("jasdklfjjjjjjjjjjjjjjjjjjjjjjjjjjjj numero di item:" +selezionati_posiz.size()+" adapter:  "+adapter.l.size()+"  panorami: "+ PanoramiStorage.panorami_storage.Panorami.size()+ "    posizione 0  "+selezionati_posiz.get(0));
            //adapter.onClick_menu();
            for(int i =0; i<selezionati_posiz.size(); i++){
                System.out.println("     selezionati:  "+selezionati_posiz.get(i));
                PanoramiStorage p = PanoramiStorage.panorami_storage;
                p.delete(0);
                adapter.l.remove(0);
                adapter.notifyDataSetChanged();
                selezionati_posiz.remove(i);
                in_selezione = false;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_history, container, false);
        // riscrivere la barra con la barra di sistema
        Toolbar bar = view.findViewById(R.id.history_toolbar);
        bar.showOverflowMenu();
        setHasOptionsMenu(true);

        ((MainActivity)getActivity()).setSupportActionBar(bar);

        ListView r = (ListView) view.findViewById(R.id.storico_lista);
        List<Panorama> list = PanoramiStorage.panorami_storage.getAllPanorama();
        adapter = new StoricoAdapter(view.getContext(), R.layout.singolo_storico, list);
        r.setAdapter(adapter);

        r.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                TextView ID = parent.findViewById(R.id.ID);
                String idi = (String) ID.getText();
                if(!in_selezione) {
                    Intent i = new Intent(getActivity(), Risultati.class);
                    i.putExtra("ID", idi);
                    startActivity(i);
                }else{
                    selezionati_posiz.add(position);
                    //selezionati_id.add(idi);
                    ImageView v = view.findViewById(R.id.spunta);
                    v.setVisibility(View.VISIBLE);
                }
            }
        });

        r.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                ImageView v = view.findViewById(R.id.spunta);
                v.setVisibility(View.VISIBLE);

                selezionati_posiz.add(position);
                //selezionati_id.add(idi);

                System.err.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE posizione : "+position+"   arraysize: "+selezionati_posiz.size());
                in_selezione = true;
                return true;
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
