package it.bobbyfriends.howsthere;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.Date;

import it.bobbyfriends.howsthere.objects.Panorama;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Hwt {
    private Context activity_context;
    private Retrofit retrofit = null;
    private BottomSheetDialog dialog;

    private TextView dialog_message;
    private Button bRetry;
    private ProgressBar progress;

    public Panorama panorama = null;
    private String peaks;
    private String peaks_name;
    private Integer max_retry = 3;
    private Integer process_state = 0;

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

    public Hwt (Context context) {
        this.activity_context = context;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://www.heywhatsthat.com/")
                .build();

        this.dialog = new BottomSheetDialog(activity_context);
        View dialogView = View.inflate(activity_context, R.layout.activity_loading, null);
        this.dialog.setCancelable(false);
        this.dialog.setContentView(dialogView);

        this.bRetry = dialogView.findViewById(R.id.retry);
        bRetry.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                retryStage();
            }
        });

        this.progress = dialogView.findViewById(R.id.progressBar);
        this.dialog_message = dialogView.findViewById(R.id.stat_news);

        this.panorama = new Panorama();
    }

    public void initializePanorama(LatLng pos, String city, Date sel_date){
        panorama.setCity(city);
        panorama.setDate(sel_date);
        panorama.setPosition(pos);
    }

    public void requestData(){
        dialog_message.setText(activity_context.getResources().getString(R.string.get_id));
        dialog.show();

        obtainId(0);
    }

    public void obtainId(Integer retry){
        if(retry > max_retry){
            enableRetry();
            return;
        }

        HeyWhatsID service = retrofit.create(HeyWhatsID.class);
        Call<ResponseBody> call = service.getID(this.panorama.lat, this.panorama.lon);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String id = response.body().string();

                        dialog_message.setText("ID: " + id);

                        if(id != null && id != "") {
                            panorama.ID = id;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkStatus(0);
                                }
                            }, 1000);
                        }else{
                            waitRetry(retry);
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                waitRetry(retry);
            }
        });
    }

    public void checkStatus(Integer retry){
        process_state = 1;
        dialog_message.setText(activity_context.getResources().getString(R.string.get_status) + " - t: " + retry);

        if(retry > max_retry){
            enableRetry();
            return;
        }

        HeyWhatsReady service = retrofit.create(HeyWhatsReady.class);
        Call<ResponseBody> call = service.getStatus(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String status = response.body().string();

                        if(status.length() > 0 && status.charAt(0) == '1'){
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
        process_state = 2;
        dialog_message.setText(activity_context.getResources().getString(R.string.get_mountain) + " - t: " + retry);

        if(retry > max_retry){
            enableRetry();
            return;
        }

        HeyWhatsPeak service = retrofit.create(HeyWhatsPeak.class);
        Call<ResponseBody> call = service.getPeak(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        peaks = response.body().string();
                        if(peaks.length() > 0){
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
        process_state = 3;
        dialog_message.setText(activity_context.getResources().getString(R.string.get_mountain_name) + " - t: " + retry);

        if(retry > max_retry){
            enableRetry();
            return;
        }

        HeyWhatsNamePeak service = retrofit.create(HeyWhatsNamePeak.class);
        Call<ResponseBody> call = service.getNamePeak(panorama.ID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        peaks_name = response.body().string();
                        processPeak();
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
        process_state = 4; // Processing
        dialog_message.setText(activity_context.getResources().getString(R.string.processing));

        Calc c = new Calc(peaks, null);
        c.execute();
    }

    private void waitRetry(Integer retry){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog_message.setText(activity_context.getResources().getString(R.string.failed_try));
                newRetry(retry + 1);
            }
        }, 2000);
    }

    private void enableRetry(){
        progress.setVisibility(View.GONE);
        bRetry.setVisibility(View.VISIBLE);
        dialog_message.setText(activity_context.getResources().getString(R.string.get_error));
    }

    public void newRetry(Integer retry){
        switch(process_state){
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
        bRetry.setVisibility(View.INVISIBLE);
        newRetry(0);
    }
}