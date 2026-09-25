package it.howsthere.howsthere2;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import it.howsthere.howsthere2.objects.Panorama;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Hwt implements AsyncResponse {
    private final Integer maxRetry = 3;

    private Context context;
    private Retrofit retrofit = null;
    private BottomSheetDialog dialog;

    private TextView dialogMessage;
    private Button btnRetry;
    private ProgressBar progress;

    public Panorama panorama = null;
    private String peaks;
    private String peaksName;
    private Integer processState = 0;

    public interface HeyWhatsID {
        @GET("api/query?src=hows")
        Call<ResponseBody> getID(@Query("lat") Double lat, @Query("lon") Double lon);
    }

    public interface HeyWhatsReady {
        @GET("api/ready?src=hows")
        Call<ResponseBody> getStatus(@Query("id") String ID);
    }

    public interface HeyWhatsPeak {
        @GET("api/horizon.csv?resolution=.999")
        Call<ResponseBody> getPeak(@Query("id") String ID);
    }

    public interface HeyWhatsNamePeak {
        @GET("api/horizon-peaks?src=hows")
        Call<ResponseBody> getNamePeak(@Query("id") String ID);
    }

    public Hwt (Context context_) {
        context = context_;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.heywhatsthat.com/")
                .build();

        dialog = new BottomSheetDialog(context);
        View dialogView = View.inflate(context, R.layout.loading_bottom_sheet, null);
        dialog.setCancelable(true);
        dialog.setContentView(dialogView);

        btnRetry = dialogView.findViewById(R.id.retry);
        progress = dialogView.findViewById(R.id.progressBar);
        dialogMessage = dialogView.findViewById(R.id.textInfo);

        btnRetry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                retryStage();
            }
        });

        panorama = new Panorama();
    }

    public void initializePanorama(LatLng pos, String city, Date selDate){
        panorama.setCity(city);
        panorama.setDate(selDate);
        panorama.setPosition(pos);
    }

    public void requestData(){
        dialogMessage.setText(context.getResources().getString(R.string.get_id));
        dialog.show();

        // Start getting ID from Heywhatsthat
        obtainId(0);
    }

    public void obtainId(Integer retry_){
        if(retry_ > maxRetry){
            showRetry();
            return;
        }

        HeyWhatsID service = retrofit.create(HeyWhatsID.class);
        Call<ResponseBody> call = service.getID(panorama.lat, panorama.lon);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (ResponseBody body = response.body()) {
                        String id = Objects.requireNonNull(body).string();

                        if(!id.isEmpty()) {
                            panorama.id = id;

                            new Handler(Looper.getMainLooper())
                                    .postDelayed(() -> checkStatus(0), 1000);
                        }else{
                            waitRetry(retry_);
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                waitRetry(retry_);
            }
        });
    }

    public void checkStatus(Integer retry){
        processState = 1;
        dialogMessage.setText(String.format("%s", context.getResources().getString(R.string.get_status)));

        if(retry > maxRetry){
            showRetry();
            return;
        }

        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(panorama.id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (ResponseBody body = response.body()) {
                        String status = Objects.requireNonNull(body).string();

                        if(!status.isEmpty() && status.charAt(0) == '1'){
                            obtainPeaks(0);
                        }else{
                            waitRetry(retry);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                waitRetry(retry);
            }
        });
    }

    private void obtainPeaks(Integer retry){
        processState = 2;
        dialogMessage.setText(context.getResources().getString(R.string.get_mountain));

        if(retry > maxRetry){
            showRetry();
            return;
        }

        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(panorama.id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (ResponseBody body = response.body()) {
                        peaks = Objects.requireNonNull(body).string();

                        if(!peaks.isEmpty()){
                            //obtainPeakNames(0);
                            processPeak();
                        }else{
                            waitRetry(retry);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                waitRetry(retry);
            }
        });
    }

    private void obtainPeakNames(Integer retry){
        processState = 3;
        dialogMessage.setText(context.getResources().getString(R.string.get_mountain_name));

        if(retry > maxRetry){
            showRetry();
            return;
        }

        HeyWhatsNamePeak service = retrofit.create(HeyWhatsNamePeak.class);
        Call<ResponseBody> call = service.getNamePeak(panorama.id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (ResponseBody body = response.body()) {
                        peaksName = Objects.requireNonNull(body).string();
                        processPeak();

                        /*if(!peaksName.isEmpty()) {
                            System.out.println("PEAKS: " + peaksName);
                            processPeak();
                        } else {
                            System.out.println("PEAKS: " + panorama.id);
                            waitRetry(retry);
                        }*/

                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                waitRetry(retry);
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                waitRetry(retry);
            }
        });
    }

    private void processPeak(){
        processState = 4; // Processing
        dialogMessage.setText(context.getResources().getString(R.string.processing));

        Processing c = new Processing(context, peaks, peaksName, panorama);
        c.execute();

        // After processing start the result activity
        processFinish();
    }

    @Override
    public void processFinish() {
        dialog.dismiss();

        Intent i = new Intent(context, Result.class);

        i.putExtra("id", panorama.id);
        i.putExtra("city", panorama.city);

        context.startActivity(i);
    }

    private void waitRetry(Integer retry_){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogMessage.setText(context.getResources().getString(R.string.failed_try));
                newRetry(retry_ + 1);
            }
        }, 1500);
    }

    private void showRetry() {
        progress.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
        dialogMessage.setText(context.getResources().getString(R.string.get_error));
    }

    public void newRetry(Integer retry){
        switch(processState) {
            case 0:
                obtainId(retry);
                break;
            case 1:
                checkStatus(retry);
                break;
            case 2:
                obtainPeaks(retry);
                break;
            case 3:
                obtainPeakNames(retry);
                break;
            default:
        }
    }

    public void retryStage(){
        progress.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.INVISIBLE);

        newRetry(0);
    }
}