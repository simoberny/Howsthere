package com.bobbyteam.howsthere2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bobbyteam.howsthere2.objects.Panorama;
import com.bobbyteam.howsthere2.objects.PanoramaStorage;
import com.bobbyteam.howsthere2.ui.result.ResultViewModel;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class Result extends AppCompatActivity {
    private Panorama pan = null;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ResultViewModel vm = new ViewModelProvider(this).get(ResultViewModel.class);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        TextView textDate = findViewById(R.id.item_date);
        TextView textCity = findViewById(R.id.item_city);
        ImageView previewImage = findViewById(R.id.preview_image);

        Toolbar toolbar = findViewById(R.id.result_toolbar);
        toolbar.inflateMenu(R.menu.result_toolbar_menu);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24);

        // Imposta il click listener per la freccia "indietro"
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Configura il callback per il pulsante "indietro"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Gestisci il click sull'icona del menu
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        getResources().getString(R.string.checkout) +
                                " \nhttps://howsthere.page.link/panorama?date=" +
                                pan.date.getTime() + "&lat=" + pan.lat + "&lon=" + pan.lon);

                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;
            }

            return false;
        });

        BottomNavigationView navigation = findViewById(R.id.result_nav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_activity_result);
        NavigationUI.setupWithNavController(navigation, navController);

        // Get ID from intent and change UI
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        id = (String) extras.get("id");
        vm.setData(id);

        if(id != null) {
            pan = PanoramaStorage.getInstance().getPanoramaByID(id);
            textDate.setText(sdf.format(pan.date));
            textCity.setText(pan.city);

            // Render small maps preview of the position
            Glide.with(this)
                    .load("https://maps.googleapis.com/maps/api/staticmap?center=" + pan.lat  + "," + pan.lon + "&zoom=10&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + pan.lat  + "," + pan.lon + "&key=" + BuildConfig.MAPS_API_KEY)
                    .placeholder(R.drawable.noimage)
                    .into(previewImage);
        }
    }
}
