package it.howsthere.howsthere2.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import it.howsthere.howsthere2.objects.Panorama;

public class ResultViewModel extends ViewModel {
    private boolean calculatingYear;

    /** Rotation retains this ViewModel and must not start a second annual calculation. */
    public synchronized void ensureYearData(Panorama value) {
        if (value.processedYearData || calculatingYear) return;
        calculatingYear = true;
        new Thread(() -> {
            try {
                new it.howsthere.howsthere2.Processing(value).generateYearData();
                postPanorama(value);
                it.howsthere.howsthere2.objects.PanoramaStorage.getInstance().addPanorama(value);
            } finally {
                synchronized (ResultViewModel.this) {
                    calculatingYear = false;
                }
            }
        }, "panorama-year").start();
    }

    private final MutableLiveData<Panorama> panorama = new MutableLiveData<>();


    public LiveData<Panorama> getPanorama() {
        return panorama;
    }

    public void setPanorama(Panorama value) {
        panorama.setValue(value);
    }

    private void postPanorama(Panorama value) {
        panorama.postValue(value);
    }

}
