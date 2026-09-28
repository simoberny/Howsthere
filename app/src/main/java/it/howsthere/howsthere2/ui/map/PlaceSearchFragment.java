package it.howsthere.howsthere2.ui.map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.howsthere.howsthere2.BuildConfig;
import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.ui.SystemInsets;

/** App-owned autocomplete UI: no dependency on the legacy Places window implementation. */
public class PlaceSearchFragment extends DialogFragment {
    static final String RESULT = "selected-place";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final PredictionsAdapter adapter = new PredictionsAdapter();
    private PlacesClient places;
    private AutocompleteSessionToken session;
    private TextInputEditText query;
    private TextView status;
    private View progress;
    private int generation;
    private boolean fetchingPlace;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setStyle(STYLE_NO_TITLE, R.style.Theme_Howsthere2_Search);
        // Preserve the project's existing Places API configuration.
        if (!Places.isInitialized()) Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        places = Places.createClient(requireContext());
        session = AutocompleteSessionToken.newInstance();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_place_search, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, Bundle state) {
        SystemInsets.apply(view);
        query = view.findViewById(R.id.place_query);
        status = view.findViewById(R.id.place_search_status);
        progress = view.findViewById(R.id.place_search_progress);
        RecyclerView list = view.findViewById(R.id.place_predictions);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        view.findViewById(R.id.place_search_close).setOnClickListener(v -> dismiss());
        ImageView attribution = view.findViewById(R.id.places_attribution);
        boolean dark = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        attribution.setImageResource(dark
                ? com.google.android.libraries.places.R.drawable.places_powered_by_google_dark
                : com.google.android.libraries.places.R.drawable.places_powered_by_google_light);
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        query.requestFocus();
    }

    @Override public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowCompat.enableEdgeToEdge(window);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            query.post(() -> {
                if (query != null) WindowCompat.getInsetsController(window, query)
                        .show(androidx.core.view.WindowInsetsCompat.Type.ime());
            });
        }
    }

    private void scheduleSearch(String text) {
        int request = ++generation;
        handler.removeCallbacksAndMessages(null);
        adapter.replace(Collections.emptyList());
        progress.setVisibility(View.GONE);
        status.setText(R.string.search_hint);
        if (text.length() < 2) return;
        handler.postDelayed(() -> {
            if (query == null || fetchingPlace) return;
            progress.setVisibility(View.VISIBLE);
            status.setText(R.string.search_loading);
            FindAutocompletePredictionsRequest search = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(session).setQuery(text).build();
            places.findAutocompletePredictions(search)
                    .addOnSuccessListener(result -> {
                        if (!isCurrent(request)) return;
                        progress.setVisibility(View.GONE);
                        adapter.replace(result.getAutocompletePredictions());
                        status.setText(adapter.getItemCount() == 0 ? R.string.search_empty : R.string.search_select);
                    })
                    .addOnFailureListener(error -> showFailure(request));
        }, 300);
    }

    private boolean isCurrent(int request) { return query != null && request == generation; }

    private void select(AutocompletePrediction prediction) {
        if (fetchingPlace) return;
        fetchingPlace = true;
        int request = ++generation;
        handler.removeCallbacksAndMessages(null);
        query.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        FetchPlaceRequest details = FetchPlaceRequest.builder(prediction.getPlaceId(),
                Collections.singletonList(Place.Field.LOCATION)).setSessionToken(session).build();
        places.fetchPlace(details).addOnSuccessListener(result -> {
            if (!isCurrent(request)) return;
            com.google.android.gms.maps.model.LatLng position = result.getPlace().getLocation();
            if (position == null) { showFailure(request); return; }
            Bundle selected = new Bundle();
            selected.putDouble("latitude", position.latitude);
            selected.putDouble("longitude", position.longitude);
            getParentFragmentManager().setFragmentResult(RESULT, selected);
            dismiss();
        }).addOnFailureListener(error -> showFailure(request));
    }

    private void showFailure(int request) {
        if (!isCurrent(request)) return;
        fetchingPlace = false;
        query.setEnabled(true);
        progress.setVisibility(View.GONE);
        status.setText(R.string.search_error);
    }

    @Override public void onDestroyView() {
        generation++;
        handler.removeCallbacksAndMessages(null);
        query = null;
        status = null;
        progress = null;
        fetchingPlace = false;
        super.onDestroyView();
    }

    private final class PredictionsAdapter extends RecyclerView.Adapter<PredictionHolder> {
        private final List<AutocompletePrediction> predictions = new ArrayList<>();

        void replace(List<AutocompletePrediction> items) {
            predictions.clear();
            predictions.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull @Override public PredictionHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            return new PredictionHolder(LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull PredictionHolder holder, int position) {
            AutocompletePrediction prediction = predictions.get(position);
            holder.primary.setText(prediction.getPrimaryText(null));
            holder.secondary.setText(prediction.getSecondaryText(null));
            holder.itemView.setOnClickListener(v -> {
                int index = holder.getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) select(predictions.get(index));
            });
        }

        @Override public int getItemCount() { return predictions.size(); }
    }

    private static final class PredictionHolder extends RecyclerView.ViewHolder {
        final TextView primary, secondary;
        PredictionHolder(View view) {
            super(view);
            primary = view.findViewById(android.R.id.text1);
            secondary = view.findViewById(android.R.id.text2);
        }
    }
}
