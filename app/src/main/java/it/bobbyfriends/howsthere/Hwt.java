package it.bobbyfriends.howsthere;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Date;

import it.bobbyfriends.howsthere.objects.Panorama;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Hwt {
    private Context activity_context;
    private Retrofit retrofit = null;
    private BottomSheetDialog dialog;
    private TextView dialog_message;
    public Panorama panorama = null;

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
                .baseUrl("http://www.heywhatsthat.com/")
                .build();

        this.dialog = new BottomSheetDialog(activity_context);
        View dialogView = View.inflate(activity_context, R.layout.activity_loading, null);
        this.dialog.setCancelable(false);
        this.dialog.setContentView(dialogView);

        this.dialog_message = dialogView.findViewById(R.id.stat_news);

        this.panorama = new Panorama();
    }

    public void initializePanorama(LatLng pos, String city, Date sel_date){
        panorama.setCity(city);
        panorama.setDate(sel_date);
        panorama.setPosition(pos);
    }

    public void requestData(){
        this.dialog_message.setText(activity_context.getResources().getString(R.string.get_id));
        dialog.show();
    }
}

