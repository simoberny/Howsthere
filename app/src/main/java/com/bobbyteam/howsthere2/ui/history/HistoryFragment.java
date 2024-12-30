package com.bobbyteam.howsthere2.ui.history;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bobbyteam.howsthere2.MainActivity;
import com.bobbyteam.howsthere2.R;
import com.bobbyteam.howsthere2.databinding.FragmentHistoryBinding;
import com.bobbyteam.howsthere2.objects.Panorama;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;

    protected HistoryAdapter adapter;

    protected RecyclerView historyRecycler;

    protected RelativeLayout empty;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        historyRecycler = root.findViewById(R.id.recycler_view);
        empty = root.findViewById(R.id.no_history);

        MainActivity activity = (MainActivity) getActivity();
        List<Panorama> list =  Objects.requireNonNull(activity).getStorage().getAllPanorama();

        adapter = new HistoryAdapter(list);
        historyRecycler.setAdapter(adapter);

        adapter.setActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.history_action_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete_selected) {
                    adapter.deleteSelectedItems();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //adapter.setActionModeCallback(null);
                adapter.unselectItems();
            }
        });

        Toolbar toolbar = root.findViewById(R.id.history_toolbar);
        toolbar.inflateMenu(R.menu.history_toolbar_menu);

        // Gestisci il click sull'icona del menu
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_all) {
                // Elimina tutti gli elementi
                adapter.clearItems(); // Assumi che il tuo adapter abbia un metodo per eliminare tutti gli elementi
                empty.setVisibility(View.VISIBLE);

                return true;
            }

            return false;
        });

        if(!list.isEmpty()){
            empty.setVisibility(View.GONE);
        }else{
            empty.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}