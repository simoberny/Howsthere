package it.unitn.lpmt.howsthere.Fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.unitn.lpmt.howsthere.Adapter.StoricoAdapter;
import it.unitn.lpmt.howsthere.MainActivity;
import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;
import it.unitn.lpmt.howsthere.R;
import it.unitn.lpmt.howsthere.RisultatiActivity;

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
        if (Build.VERSION.SDK_INT >= 21) {
            ((MainActivity)getActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
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
                    System.err.println("     Selected:  " + selezionati_id.get(i));
                    PanoramiStorage p = PanoramiStorage.panorami_storage;
                    p.delete_by_id(selezionati_id.get(i));
                }
                adapter.notifyDataSetChanged();
                selezionati_id.clear();
                in_selezione = false;
                return true;
            }else{
                Snackbar.make(getActivity().findViewById(R.id.layout_base),
                        R.string.long_press, Snackbar.LENGTH_LONG).setAction(R.string.del_every, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PanoramiStorage.panorami_storage.delete_all();
                        adapter.notifyDataSetChanged();
                        selezionati_id.clear();
                        in_selezione = false;
                    }
                }).show();
            }
        }else if(id == R.id.share){
            if(in_selezione){
                for(int i = 0; i < selezionati_id.size(); i++){
                    Panorama p = PanoramiStorage.panorami_storage.getPanoramabyID(selezionati_id.get(i));
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.checkout) + "\nPanorama " + i + " https://howsthere.page.link/panorama?date=" + p.data.getTime() + "&lat=" + p.lat + "&lon=" + p.lon);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }else{
                Snackbar.make(getActivity().findViewById(R.id.layout_base), R.string.select_to_share, Snackbar.LENGTH_LONG).show();
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
        final ListView r = (ListView) view.findViewById(R.id.storico_lista);
        List<Panorama> list = PanoramiStorage.panorami_storage.getAllPanorama();
        adapter = new StoricoAdapter(view.getContext(), R.layout.singolo_storico, list,selezionati_id);
        r.setAdapter(adapter);

        r.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                String selected_id = ((Panorama)parent.getAdapter().getItem(position)).ID;
                CheckBox v = view.findViewById(R.id.selectable);
                if(!in_selezione) {
                    Intent i = new Intent(getActivity(), RisultatiActivity.class);
                    i.putExtra("ID", selected_id);
                    startActivity(i);
                }else if(selezionati_id.contains(selected_id)){
                    selezionati_id.remove(selected_id);
                    view.findViewById(R.id.overlay).setVisibility(View.GONE);
                    //view.findViewById(R.id.spunta).setVisibility(View.GONE); //Spunta Matteo
                    v.setChecked(false);

                    if(selezionati_id.size()==0){
                        for(int i = 0; i < parent.getAdapter().getCount(); i++){
                            CheckBox cb = r.getChildAt(i).findViewById(R.id.selectable);
                            cb.setVisibility(View.GONE);
                        }
                        in_selezione = false;
                    }
                }else{
                    view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                    //view.findViewById(R.id.spunta).setVisibility(View.VISIBLE); //Spunta Matteo
                    v.setChecked(true);
                    selezionati_id.add(selected_id);
                }
            }
        });

        r.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                String selected_id = ((Panorama)parent.getAdapter().getItem(position)).ID;
                if(!selezionati_id.contains(selected_id)) {
                    CheckBox v = view.findViewById(R.id.selectable);
                    if(!in_selezione){
                        for (int i = 0; i < parent.getAdapter().getCount(); i++) {
                            CheckBox cb = r.getChildAt(i).findViewById(R.id.selectable);
                            cb.setVisibility(View.VISIBLE);
                        }
                    }
                    v.setChecked(true);
                    selezionati_id.add(selected_id);

                    view.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                    //view.findViewById(R.id.spunta).setVisibility(View.VISIBLE); //Spunta Matteo

                    in_selezione = true;
                }
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
