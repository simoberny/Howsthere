package it.unitn.simob.howsthere;

import android.content.Context;
import it.unitn.simob.howsthere.Helper.ForecastCallback;
import it.unitn.simob.howsthere.Helper.WeatherCallback;
import it.unitn.simob.howsthere.retrofit.api.ApiClient;
import it.unitn.simob.howsthere.retrofit.api.WeatherRetrofitCallback;
import it.unitn.simob.howsthere.retrofit.models.ForecastResponseModel;
import it.unitn.simob.howsthere.retrofit.models.WeatherResponseModel;
import retrofit2.Call;

public class WeatherMap {
    Context context;
    String APP_ID;

    public WeatherMap(Context context, String APP_ID) {
        this.context = context;
        this.APP_ID = APP_ID;
    }

    public void getCityWeather(String city, final WeatherCallback weatherCallback) {
        final ApiClient objApi = ApiClient.getInstance();
        try {
            Call objCall = null;

            objCall = objApi.getApi(context).getCityWeather(APP_ID, city);

            if (objCall != null) {
                objCall.enqueue(new WeatherRetrofitCallback<WeatherResponseModel>(context) {

                    @Override
                    public void onFailure(Call call, Throwable t) {

                        weatherCallback.failure("Failed");
                        super.onFailure(call, t);
                    }

                    @Override
                    protected void onResponseWeatherResponse(Call call, retrofit2.Response response) {

                        if (!response.isSuccessful())
                            weatherCallback.failure("Failed");
                    }

                    @Override
                    protected void onResponseWeatherObject(Call call, WeatherResponseModel response) {

                        weatherCallback.success(response);
                    }

                    @Override
                    protected void common() {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLocationWeather(String latitude, String longitude, final WeatherCallback weatherCallback) {
        final ApiClient objApi = ApiClient.getInstance();
        try {
            Call objCall = null;

            objCall = objApi.getApi(context).getLocationWeather(APP_ID, latitude, longitude, "it");

            if (objCall != null) {
                objCall.enqueue(new WeatherRetrofitCallback<WeatherResponseModel>(context) {

                    @Override
                    public void onFailure(Call call, Throwable t) {

                        weatherCallback.failure("Failed");
                        super.onFailure(call, t);
                    }

                    @Override
                    protected void onResponseWeatherResponse(Call call, retrofit2.Response response) {

                        if (!response.isSuccessful())
                            weatherCallback.failure("Failed");
                    }

                    @Override
                    protected void onResponseWeatherObject(Call call, WeatherResponseModel response) {

                        weatherCallback.success(response);
                    }

                    @Override
                    protected void common() {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCityForecast(String city, final ForecastCallback forecastCallback) {
        final ApiClient objApi = ApiClient.getInstance();
        try {
            Call objCall = null;

            objCall = objApi.getApi(context).getCityForcast(APP_ID, city);

            if (objCall != null) {
                objCall.enqueue(new WeatherRetrofitCallback<ForecastResponseModel>(context) {

                    @Override
                    public void onFailure(Call call, Throwable t) {

                        forecastCallback.failure("Failed");
                        super.onFailure(call, t);
                    }

                    @Override
                    protected void onResponseWeatherResponse(Call call, retrofit2.Response response) {

                        if (!response.isSuccessful())
                            forecastCallback.failure("Failed");
                    }

                    @Override
                    protected void onResponseWeatherObject(Call call, ForecastResponseModel response) {

                        forecastCallback.success(response);
                    }

                    @Override
                    protected void common() {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLocationForecast(String latitude, String longitude, final ForecastCallback forecastCallback) {
        final ApiClient objApi = ApiClient.getInstance();
        try {
            Call objCall = null;

            objCall = objApi.getApi(context).getLocationForecast(APP_ID, latitude, longitude, "it");

            if (objCall != null) {
                objCall.enqueue(new WeatherRetrofitCallback<ForecastResponseModel>(context) {

                    @Override
                    public void onFailure(Call call, Throwable t) {

                        forecastCallback.failure("Failed");
                        super.onFailure(call, t);
                    }

                    @Override
                    protected void onResponseWeatherResponse(Call call, retrofit2.Response response) {

                        if (!response.isSuccessful())
                            forecastCallback.failure("Failed");
                    }

                    @Override
                    protected void onResponseWeatherObject(Call call, ForecastResponseModel response) {

                        forecastCallback.success(response);
                    }

                    @Override
                    protected void common() {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
