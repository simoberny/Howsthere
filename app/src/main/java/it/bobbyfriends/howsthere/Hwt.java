package it.bobbyfriends.howsthere;

import android.content.Context;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Hwt {
    private Context activity_context;
    private Retrofit retrofit = null;

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


    }

    public void requestData(){

    }

}

