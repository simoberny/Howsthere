package it.unitn.lpmt.howsthere;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;

public class CustomExHandler implements Thread.UncaughtExceptionHandler {

    private Application activity;
    public CustomExHandler(Application a) {
        activity = a;
    }

    private static Thread.UncaughtExceptionHandler mDefaultHandler = Thread
            .getDefaultUncaughtExceptionHandler();

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        PanoramiStorage p = PanoramiStorage.panorami_storage;
        List<Panorama> list = p.getAllPanorama();
        if(list.size() > 0){
            p.delete(list.size() - 1);
        }

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.INSTANCE.getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) App.INSTANCE.getBaseContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, pendingIntent);
        System.exit(2);
    }

}