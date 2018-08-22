package it.unitn.lpmt.howsthere;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;

public class App extends Application {
    public static App INSTANCE;

    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PanoramiStorage.context = this;
        PanoramiStorage.panorami_storage = new PanoramiStorage();
        PanoramiStorage.panorami_storage.initial_load();

        INSTANCE = this;
        Thread.setDefaultUncaughtExceptionHandler(new CustomExHandler(this));
        Fresco.initialize(this);
    }

}