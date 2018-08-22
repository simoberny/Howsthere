package it.unitn.lpmt.howsthere;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Exiter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        }
    }
}
