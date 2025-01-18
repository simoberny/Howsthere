package it.howsthere.howsthere2.ui.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.howsthere.howsthere2.R;
import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import it.howsthere.howsthere2.BuildConfig;
import it.howsthere.howsthere2.Result;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ItemViewHolder> {
    private static final Integer maxItems = 100;
    private List<Panorama> items;
    private List<Integer> selectedPositions;
    private boolean isMultiSelect = false;
    private Context ctx;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;

    public HistoryAdapter(Context ctx_, List<Panorama> items_) {
        ctx = ctx_;
        items = items_;
        selectedPositions = new ArrayList<>();
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

        if(p.date != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            holder.textDate.setText(sdf.format(p.date));
        }

        holder.selected.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);

        int selectedColor = MaterialColors.getColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY);
        int defaultColor = MaterialColors.getColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorSurface, Color.WHITE);

        holder.selected.setChecked(selectedPositions.contains(position));
        holder.itemView.setBackgroundColor(selectedPositions.contains(position) ? selectedColor : defaultColor);

        // Render small maps preview of the position
        Glide.with(holder.itemView.getContext())
                .load("https://maps.googleapis.com/maps/api/staticmap?center=" + p.lat  + "," + p.lon + "&zoom=10&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + p.lat  + "," + p.lon + "&key=" + BuildConfig.MAPS_API_KEY)
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
                String selected_id = items.get(position).id;

                Intent i = new Intent(ctx, Result.class);
                i.putExtra("id", selected_id);
                ctx.startActivity(i);
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
            PanoramaStorage.getInstance().deleteById(items.get(position).id);
        }

        isMultiSelect = false;
        selectedPositions.clear();

        // Get updated panoramas
        items = PanoramaStorage.getInstance().getAllPanorama();

        notifyDataSetChanged();

        if (actionMode != null) {
            actionMode.finish();
        }
    }

    public void clearItems() {
        PanoramaStorage.getInstance().deleteAll();

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
