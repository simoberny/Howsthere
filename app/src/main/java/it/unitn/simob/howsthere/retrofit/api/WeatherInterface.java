package it.unitn.simob.howsthere.retrofit.api;


import it.unitn.simob.howsthere.retrofit.models.ForecastResponseModel;
import it.unitn.simob.howsthere.retrofit.models.WeatherResponseModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherInterface {

    @GET("weather")
    Call<WeatherResponseModel> getCityWeather(@Query("appid") String appid,
                                              @Query("q") String city);

    @GET("weather")
    Call<WeatherResponseModel> getLocationWeather(@Query("appid") String appid,
                                                  @Query("lat") String latitude,
                                                  @Query("lon") String longitude,
                                                  @Query("lang") String lang);

    @GET("forecast")
    Call<ForecastResponseModel> getCityForcast(@Query("appid") String appid,
                                               @Query("q") String city);

    @GET("forecast")
    Call<ForecastResponseModel> getLocationForecast(@Query("appid") String appid,
                                                    @Query("lat") String latitude,
                                                    @Query("lon") String longitude,
                                                    @Query("lang") String lang);

}
