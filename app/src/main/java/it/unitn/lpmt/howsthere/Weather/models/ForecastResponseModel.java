package it.unitn.lpmt.howsthere.Weather.models;

import java.io.Serializable;

public class ForecastResponseModel implements Serializable {
    private String message;

    private String cnt;

    private String cod;

    private WeatherCompleto[] list;

    private City city;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCnt() {
        return cnt;
    }

    public void setCnt(String cnt) {
        this.cnt = cnt;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public WeatherCompleto[] getList() {
        return list;
    }

    public void setList(WeatherCompleto[] list) {
        this.list = list;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "ClassPojo [message = " + message + ", cnt = " + cnt + ", cod = " + cod + ", list = " + list + ", city = " + city + "]";
    }
}
