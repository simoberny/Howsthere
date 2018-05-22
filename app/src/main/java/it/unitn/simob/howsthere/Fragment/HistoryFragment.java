package it.unitn.simob.howsthere.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ListView;

import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.StoricoAdapter;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.Oggetti.PanoramiStorage;
import it.unitn.simob.howsthere.R;

public class HistoryFragment extends Fragment{
    public HistoryFragment() {}

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

            // Do something
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

        ListView r = view.findViewById(R.id.storico_lista);
        PanoramiStorage ps = new PanoramiStorage();
        List<Panorama> list = ps.getAllPanorama();
        final StoricoAdapter adapter = new StoricoAdapter(view.getContext(), R.layout.singolo_storico, list);
        r.setAdapter(adapter);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
