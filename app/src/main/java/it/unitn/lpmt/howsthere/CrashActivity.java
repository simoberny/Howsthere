package it.unitn.lpmt.howsthere;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        Button restart = findViewById(R.id.restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PanoramiStorage p = PanoramiStorage.panorami_storage;
                List<Panorama> list = p.getAllPanorama();
                if(list.size() > 0){
                    p.delete_by_id(list.get(0).ID);
                    System.out.println("DELETE: " + list.get(0).ID + list.get(0).citta);
                }
                System.out.println("Panorama: " + list.size());

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(App.INSTANCE.getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager mgr = (AlarmManager) App.INSTANCE.getBaseContext().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 25, pendingIntent);
                System.exit(0);
            }
        });
    }
}
