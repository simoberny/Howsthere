package com.bobbyteam.howsthere2.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bobbyteam.howsthere2.objects.Panorama;

import java.util.Date;

public class ResultViewModel extends ViewModel {
    private final MutableLiveData<String> id = new MutableLiveData<>();

    private final MutableLiveData<Panorama> panorama = new MutableLiveData<>();
    private final MutableLiveData<Date> date = new MutableLiveData<>();

    public void setId(String value) {
        id.setValue(value);
    }

    public void setPanorama(Panorama value) {
        panorama.setValue(value);
    }

    public void setDate(Date value) {
        date.setValue(value);
    }

    public LiveData<String> getId() {
        return id;
    }

    public LiveData<Panorama> getPanorama() {
        return panorama;
    }

    public LiveData<Date> getDate() {
        return date;
    }
}
