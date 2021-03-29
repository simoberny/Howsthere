package it.bobbyfriends.howsthere.ui.history;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.bobbyfriends.howsthere.MainActivity;
import it.bobbyfriends.howsthere.R;
import it.bobbyfriends.howsthere.adapter.HistoryAdapter;
import it.bobbyfriends.howsthere.objects.Panorama;
import it.bobbyfriends.howsthere.objects.PanoramaStorage;

public class HistoryFragment extends Fragment {
    protected RecyclerView mRecyclerView;
    protected HistoryAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    private RelativeLayout empty = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.history_toolbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*int id = item.getItemId();

        boolean in_selezione = mAdapter.getInSelezione();
        List<String> selezionati_id = mAdapter.getSelezionati();

        if (id == R.id.delete) {
            if(in_selezione) {
                //adapter.onClick_menu();
                for (int i = 0; i < selezionati_id.size(); i++) {
                    System.err.println("     Selected:  " + selezionati_id.get(i));
                    PanoramaStorage p = PanoramaStorage.persistent_storage;
                    p.delete_by_id(selezionati_id.get(i));
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.clearSelezionati();
                mAdapter.setInSelezione(false);
                if(mAdapter.getItemCount() == 0){
                    nostorico.setVisibility(View.VISIBLE);
                }
                return true;
            }else{
                Snackbar.make(getActivity().findViewById(R.id.layout_base),
                        R.string.long_press, Snackbar.LENGTH_LONG).setAction(R.string.del_every, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PanoramaStorage.persistent_storage.delete_all();
                        mAdapter.notifyDataSetChanged();
                        mAdapter.clearSelezionati();
                        mAdapter.setInSelezione(false);
                        if(mAdapter.getItemCount() == 0){
                            nostorico.setVisibility(View.VISIBLE);
                        }
                    }
                }).show();
            }
        }else if(id == R.id.share){
            if(in_selezione){
                String sharing = getResources().getString(R.string.checkout);
                for(int i = 0; i < selezionati_id.size(); i++){
                    Panorama p = PanoramaStorage.persistent_storage.getPanoramabyID(selezionati_id.get(i));
                    sharing += "\nPanorama " + i + " https://howsthere.page.link/panorama?date=" + p.data.getTime() + "&lat=" + p.lat + "&lon=" + p.lon + "\n";
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, sharing);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }else{
                Snackbar.make(getActivity().findViewById(R.id.layout_base), R.string.select_to_share, Snackbar.LENGTH_LONG).show();
            }
        }*/

        return super.onOptionsItemSelected(item);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.white));

        View root = inflater.inflate(R.layout.fragment_history, container, false);
        mRecyclerView = (RecyclerView) root.findViewById(R.id.history_list);

        setRecyclerViewLayoutManager();
        Toolbar bar = root.findViewById(R.id.history_toolbar);
        bar.showOverflowMenu();
        setHasOptionsMenu(true);
        ((MainActivity)getActivity()).setSupportActionBar(bar);

        empty = root.findViewById(R.id.nostorico);

        List<Panorama> list = PanoramaStorage.persistent_storage.getAllPanorama();
        mAdapter = new HistoryAdapter(getActivity(), list);

        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    public void setRecyclerViewLayoutManager() {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }
}