package it.howsthere.howsthere2;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Date;

import it.howsthere.howsthere2.objects.Panorama;
import retrofit2.Retrofit;
import okhttp3.OkHttpClient;

/** Owns the loading UI; HorizonRequest owns the two independent API flows. */
public class Hwt {
    private final Context context;
    private final HorizonRequest.Api api;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BottomSheetDialog dialog;
    private final TextView dialogMessage;
    private final Button btnRetry;
    private final ProgressBar progress;
    private HorizonRequest request;
    public final Panorama panorama = new Panorama();

    private final DefaultLifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            cancelRequest();
            dialog.dismiss();
        }
    };

    public Hwt(Context context) {
        this.context = context;
        api = new Retrofit.Builder()
                .baseUrl("https://www.heywhatsthat.com/")
                // Recovery must request a fresh ID after a failed download.
                .client(new OkHttpClient.Builder().retryOnConnectionFailure(false).build())
                .build()
                .create(HorizonRequest.Api.class);
        dialog = new BottomSheetDialog(context);
        View view = View.inflate(context, R.layout.loading_bottom_sheet, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        btnRetry = view.findViewById(R.id.retry);
        progress = view.findViewById(R.id.progressBar);
        dialogMessage = view.findViewById(R.id.textInfo);
        btnRetry.setOnClickListener(v -> requestData());
        dialog.setOnDismissListener(d -> {
            cancelRequest();
            if (context instanceof LifecycleOwner) {
                ((LifecycleOwner) context).getLifecycle().removeObserver(lifecycleObserver);
            }
        });
    }

    public void initializePanorama(LatLng position, String city, Date date) {
        panorama.setCity(city);
        panorama.setDate(date);
        panorama.setPosition(position);
    }

    public void requestData() {
        cancelRequest();
        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(lifecycleObserver);
        }
        progress.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
        dialogMessage.setText(R.string.get_id);
        dialog.show();
        request = new HorizonRequest(api, (action, delay) -> handler.postDelayed(action, delay),
                new HorizonRequest.Listener() {
                    @Override
                    public void onStage(HorizonRequest.Stage stage) {
                        switch (stage) {
                            case ID: dialogMessage.setText(R.string.get_id); break;
                            case WAITING: dialogMessage.setText(R.string.get_status); break;
                            case PROFILE: dialogMessage.setText(R.string.get_mountain); break;
                            case NAMES: dialogMessage.setText(R.string.get_mountain_name); break;
                        }
                    }

                    @Override
                    public void onComplete(String profileId, String profile, String names) {
                        panorama.id = profileId;
                        dialogMessage.setText(R.string.processing);
                        try {
                            new Processing(profile, names, panorama).execute();
                        } catch (RuntimeException error) {
                            Log.e("Hwt", "Unable to process panorama", error);
                            showRetry();
                            return;
                        }
                        dialog.dismiss();
                        Intent intent = new Intent(context, Result.class);
                        intent.putExtra("id", panorama.id);
                        intent.putExtra("city", panorama.city);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onError() {
                        showRetry();
                    }
                }, panorama.lat, panorama.lon);
        request.start();
    }

    private void cancelRequest() {
        if (request != null) request.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    private void showRetry() {
        cancelRequest();
        progress.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        dialogMessage.setText(R.string.get_error);
    }
}
