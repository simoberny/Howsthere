package it.howsthere.howsthere2;

import android.content.Intent;
import it.howsthere.howsthere2.ui.AppDatePicker;
import android.content.res.Configuration;
import android.view.View;
import androidx.activity.EdgeToEdge;
import it.howsthere.howsthere2.ui.SystemInsets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.ui.result.ResultViewModel;

public class Result extends AppCompatActivity {
    private Panorama pan = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);

        ResultViewModel vm = new ViewModelProvider(this).get(ResultViewModel.class);
        View root = findViewById(R.id.result_root);
        SystemInsets.apply(root);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        TextView textDate = findViewById(R.id.item_date);
        TextView textCity = findViewById(R.id.item_city);
        ImageView previewImage = findViewById(R.id.preview_image);
        Button changeDateBtn = findViewById(R.id.change_date);

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
            if (item.getItemId() == R.id.action_share && pan != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                String shareLink = getResources().getString(R.string.checkout) +
                        " \nhttps://simoberny.github.io/share?date=" +
                        pan.date.getTime() + "&lat=" + pan.lat + "&lon=" + pan.lon;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_with)));

                return true;
            }

            return false;
        });

        AppDatePicker.register(getSupportFragmentManager(), this, "result-date", selected -> {
            if (pan == null) return;
            pan = new Processing(pan).update(selected);
            textDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(selected));
            vm.setPanorama(pan);
        });
        changeDateBtn.setOnClickListener(v -> {
            if (pan != null) AppDatePicker.show(getSupportFragmentManager(), "result-date", pan.date);
        });

        BottomNavigationView navigation = findViewById(R.id.result_nav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_activity_result);
        NavigationUI.setupWithNavController(navigation, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean immersiveChart = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    && (destination.getId() == R.id.navigation_sun || destination.getId() == R.id.navigation_moon);
            int visibility = immersiveChart ? View.GONE : View.VISIBLE;
            toolbar.setVisibility(visibility);
            findViewById(R.id.relativeLayout).setVisibility(visibility);
            navigation.setVisibility(visibility);
            WindowInsetsControllerCompat bars = WindowCompat.getInsetsController(getWindow(), root);
            bars.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            if (immersiveChart) bars.hide(WindowInsetsCompat.Type.systemBars());
            else bars.show(WindowInsetsCompat.Type.systemBars());
            ViewCompat.requestApplyInsets(root);
        });

        // Get ID from intent and change UI
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id == null) {
            finish();
            return;
        }

        if(id != null) {
            pan = vm.getPanorama().getValue();
            if (pan == null) pan = PanoramaStorage.getInstance().getPanoramaByID(id);
            if (pan == null) {
                finish();
                return;
            }
            vm.setPanorama(pan);

            textDate.setText(sdf.format(pan.date));
            textCity.setText(pan.city);

            // Render small maps preview of the position
            Glide.with(this)
                    .load("https://maps.googleapis.com/maps/api/staticmap?center=" + pan.lat  + "," + pan.lon + "&zoom=12&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + pan.lat  + "," + pan.lon + "&key=" + BuildConfig.MAPS_API_KEY)
                    .placeholder(R.drawable.noimage)
                    .into(previewImage);

            vm.ensureYearData(pan);
        }
    }
}
