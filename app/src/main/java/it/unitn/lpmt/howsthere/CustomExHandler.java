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

        PanoramiStorage p = PanoramiStorage.panorami_storage;
        List<Panorama> list = p.getAllPanorama();
        if(list.size() > 0){
            p.delete_by_id(list.get(0).ID);
            System.out.println("DELETE: " + list.get(0).ID + list.get(0).citta);
        }

        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.putExtra("crash", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

}