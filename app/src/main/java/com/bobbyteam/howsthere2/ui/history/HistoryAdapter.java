package com.bobbyteam.howsthere2.ui.history;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bobbyteam.howsthere2.R;
import com.bobbyteam.howsthere2.objects.Panorama;
import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ItemViewHolder> {
    private static final Integer maxItems = 100;
    private List<Panorama> items;
    private List<Integer> selectedPositions;
    private boolean isMultiSelect = false;
    private Context ctx;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    public HistoryAdapter(List<Panorama> items) {
        this.items = items;
        this.selectedPositions = new ArrayList<>();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textCity;
        ImageView previewImage;
        CheckBox selected;

        public ItemViewHolder(View itemView) {
            super(itemView);

            textDate = itemView.findViewById(R.id.item_date);
            textCity = itemView.findViewById(R.id.item_city);
            previewImage = itemView.findViewById(R.id.preview_image);
            selected = itemView.findViewById(R.id.selectable);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Panorama p = items.get(position);
        holder.textCity.setText(p.city);

        String d = (String) DateFormat.format("dd", p.date) +
                "/" + (String) DateFormat.format("MM", p.date) +
                "/" + (String) DateFormat.format("yyyy", p.date);
        holder.textDate.setText(d);
        holder.selected.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);

        int selectedColor = MaterialColors.getColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY);
        int defaultColor = MaterialColors.getColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE);

        holder.selected.setChecked(selectedPositions.contains(position));
        holder.itemView.setBackgroundColor(selectedPositions.contains(position) ? selectedColor : defaultColor);

        // Render small maps preview of the position
        Glide.with(holder.itemView.getContext())
                .load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon + "&key=AIzaSyC60n_RZwR9UwMxqj9lD_1JTXGZRF5arKg")
                .placeholder(R.drawable.noimage)
                .into(holder.previewImage);

        // Lungo clic per attivare la selezione multipla
        holder.itemView.setOnLongClickListener(v -> {
            if (!isMultiSelect) {
                isMultiSelect = true;

                if (actionModeCallback != null) {
                    actionMode = v.startActionMode(actionModeCallback);
                }
            }

            toggleSelection(position);
            return true;
        });

        // Clic semplice per selezionare o deselezionare
        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelect) {
                toggleSelection(position);
            } else {
                // TODO open activity with the result
                System.out.println("Apri intent!");
            }
        });
    }

    private void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }

        if(selectedPositions.isEmpty()) {
            isMultiSelect = false;
        }

        notifyDataSetChanged();

        if (actionMode != null) {
            actionMode.setTitle(selectedPositions.size() + " selected");

            if (selectedPositions.isEmpty()) {
                actionMode.finish();
            }
        }
    }

    public void setActionModeCallback(ActionMode.Callback callback) {
        this.actionModeCallback = callback;
    }

    public void unselectItems() {
        selectedPositions.clear();
        isMultiSelect = false;
        notifyDataSetChanged();
    }

    public void deleteSelectedItems() {
        for (int i = selectedPositions.size() - 1; i >= 0; i--) {
            int position = selectedPositions.get(i);
            items.remove(position);
        }

        selectedPositions.clear();
        notifyDataSetChanged();

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void clearItems() {
        items.clear();
        isMultiSelect = false;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
