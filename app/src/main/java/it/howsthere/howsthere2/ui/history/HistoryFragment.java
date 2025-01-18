package it.howsthere.howsthere2.ui.history;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.databinding.FragmentHistoryBinding;

import java.util.List;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;

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

        List<Panorama> list =  PanoramaStorage.getInstance().getAllPanorama();

        adapter = new HistoryAdapter(requireActivity(), list);
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

                    if(adapter.getItemCount() == 0) {
                        empty.setVisibility(View.GONE);
                    }

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
                showDeleteConfirmationDialog();

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

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title)) // Titolo localizzato
            .setMessage(getString(R.string.dialog_message)) // Messaggio localizzato
            .setPositiveButton(getString(R.string.dialog_positive), (dialog, which) -> {
                // Elimina tutti gli elementi
                adapter.clearItems();
                empty.setVisibility(View.VISIBLE);
            })
            .setNegativeButton(getString(R.string.dialog_negative), (dialog, which) -> {
                // Chiudi il dialogo
                dialog.dismiss();
            })
            .create()
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}