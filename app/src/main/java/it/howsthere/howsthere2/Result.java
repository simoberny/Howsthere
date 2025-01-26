package it.howsthere.howsthere2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;

import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.PanoramaStorage;
import it.howsthere.howsthere2.ui.result.ResultViewModel;

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
            if (item.getItemId() == R.id.action_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                String shareLink = getResources().getString(R.string.checkout) +
                        " \nhttp://www.howsthere.netsons.org/share?date=" +
                        pan.date.getTime() + "&lat=" + pan.lat + "&lon=" + pan.lon;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink);
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_with)));

                return true;
            }

            return false;
        });

        changeDateBtn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTime(pan.date);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    Result.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year_,
                                              int monthOfYear_, int dayOfMonth_) {
                            Calendar selected = Calendar.getInstance();
                            selected.set(Calendar.YEAR, year_);
                            selected.set(Calendar.MONTH, monthOfYear_);
                            selected.set(Calendar.DAY_OF_MONTH, dayOfMonth_);

                            String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selected.getTime());
                            textDate.setText(currentDate);

                            pan.date = Date.from(selected.toInstant());

                            Processing updateProcess = new Processing(pan);
                            pan = updateProcess.update(pan.date);

                            vm.setPanorama(pan);
                        }
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        BottomNavigationView navigation = findViewById(R.id.result_nav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_activity_result);
        NavigationUI.setupWithNavController(navigation, navController);

        // Get ID from intent and change UI
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        id = (String) extras.get("id");

        if(id != null) {
            pan = PanoramaStorage.getInstance().getPanoramaByID(id);
            vm.setPanorama(pan);

            textDate.setText(sdf.format(pan.date));
            textCity.setText(pan.city);

            // Render small maps preview of the position
            Glide.with(this)
                    .load("https://maps.googleapis.com/maps/api/staticmap?center=" + pan.lat  + "," + pan.lon + "&zoom=12&size=200x230&sensor=false&markers=color:blue%7Clabel:S%7C" + pan.lat  + "," + pan.lon + "&key=" + BuildConfig.MAPS_API_KEY)
                    .placeholder(R.drawable.noimage)
                    .into(previewImage);

            if(!pan.processedYearData) {
                new Thread(() -> {
                    Processing followUp = new Processing(pan);
                    followUp.generateYearData();

                    vm.postPanorama(followUp.getPanorama());
                    PanoramaStorage.getInstance().addPanorama(pan);
                }).start();
            }
        }
    }
}
