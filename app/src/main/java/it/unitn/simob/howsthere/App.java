package it.unitn.simob.howsthere;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class App extends Application {
    public static App INSTANCE;
    private boolean isNightModeEnabled = false;

    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        INSTANCE = this;
    }
}