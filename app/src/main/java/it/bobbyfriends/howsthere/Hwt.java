package it.bobbyfriends.howsthere;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.Date;

import it.bobbyfriends.howsthere.objects.Panorama;
import it.bobbyfriends.howsthere.ui.home.HomeFragment;
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

    public Panorama panorama = null;
    private Integer max_retry = 2;
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
        System.out.println("Retry: " + retry);

        if(retry > max_retry){
            bRetry.setVisibility(View.VISIBLE);
            return;
        }

        HeyWhatsID service = retrofit.create(HeyWhatsID.class);
        Call<ResponseBody> call = service.getID(this.panorama.lat, this.panorama.lon);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    dialog_message.setText("Response susseccful");
                    try {
                        String id = response.body().string();

                        dialog_message.setText("ID: " + id);

                        if(id != null && id != "") {
                            panorama.ID = id;

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkStatus(id);
                                }
                            }, 750);
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }

                //waitRetry(retry);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
                dialog_message.setText("Errore: " + t.getMessage());
                //waitRetry(retry);
            }
        });
    }

    private void waitRetry(Integer retry){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog_message.setText(activity_context.getResources().getString(R.string.failed_try));
                obtainId(retry + 1);
            }
        }, 2000);
    }

    public void checkStatus(String id){
        dialog_message.setText(activity_context.getResources().getString(R.string.get_status));
        System.out.println("ID: " + id);
    }

    public void retryStage(){
        System.out.println("Clicked!");
    }
}