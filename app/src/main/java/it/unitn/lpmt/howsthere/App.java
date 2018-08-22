package it.unitn.lpmt.howsthere;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class App extends Application {
    public static App INSTANCE;

    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Thread.setDefaultUncaughtExceptionHandler(new CustomExHandler(this));
        Fresco.initialize(this);
        INSTANCE = this;
    }

}