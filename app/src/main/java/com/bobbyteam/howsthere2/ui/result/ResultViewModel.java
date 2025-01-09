package com.bobbyteam.howsthere2.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class ResultViewModel extends ViewModel {
    private final MutableLiveData<String> data = new MutableLiveData<>();

    public void setData(String value) {
        data.setValue(value);
    }

    public LiveData<String> getData() {
        return data;
    }
}
