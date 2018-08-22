package it.unitn.lpmt.howsthere;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.bumptech.glide.util.MarkEnforcingInputStream;

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
        if(ex.toString().contains("NegativeArraySizeException")){
            Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
            intent.putExtra("crash", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            PendingIntent pendingIntent = PendingIntent.getActivity(App.INSTANCE.getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager) App.INSTANCE.getBaseContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 25, pendingIntent);

            System.exit(0);
        }
    }

}