package it.unitn.lpmt.howsthere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickClick;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.unitn.lpmt.howsthere.Fragment.BussolaFragment;
import it.unitn.lpmt.howsthere.Fragment.MeteoFragment;
import it.unitn.lpmt.howsthere.Fragment.MoonFragment;
import it.unitn.lpmt.howsthere.Fragment.PeakFragment;
import it.unitn.lpmt.howsthere.Fragment.SunFragment;
import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;

public class RisultatiActivity extends AppCompatActivity {
    private MeteoFragment mt = null;
    private SunFragment sf = null;
    private BussolaFragment bf = null;
    private MoonFragment mf = null;
    private PeakFragment pf = null;
    public Panorama p = null;
    private FirebaseAuth mAuth;

    private String id_pan;
    private PickImageDialog dialog;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int GALLERY_INTENT = 25;
    public static final int CAMERA_INTENT = 26;
    private String mCurrentPhotoPath;
    private CoordinatorLayout main = null;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risultati_nav);

        main = findViewById(R.id.coord_risultati);
        Toolbar tl = findViewById(R.id.risultati_tool);
        tl.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        tl.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tl.inflateMenu(R.menu.menu_photo);
        tl.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.photo) {
                    if (mAuth.getCurrentUser() != null) {
                        getImage();
                    } else {
                        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.risultatiMainLayout), "Devi eseguire il login per poter scattare un panorama!", Snackbar.LENGTH_LONG);
                        mySnackbar.setAction("Login", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), MainActivity.class);
                                intent.putExtra("login", 1);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                        mySnackbar.show();
                    }
                } else if (id == R.id.share) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Howsthere: \nhttps://howsthere.page.link/panorama?date=" + p.data.getTime() + "&lat=" + p.lat + "&lon=" + p.lon + "&citta=" + p.citta);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String id = (String) extras.get("ID");
        String pFromIntent = (String) extras.get("pan");

        PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;

        if (id != null) {
            p = panoramiStorage.getPanoramabyID(id);
        } else {
            Panorama obj = null;
            try {
                byte b[] = Base64.decode(pFromIntent.getBytes(), Base64.DEFAULT);
                ByteArrayInputStream bi = new ByteArrayInputStream(b);
                ObjectInputStream si = new ObjectInputStream(bi);
                obj = (Panorama) si.readObject();
            } catch (Exception e) {
                System.out.println(e);
            }
            p = obj; //Intent dalla feed con panorama salvato in Firebase
        }

        id_pan = p.ID;
        mt = MeteoFragment.newInstance();
        sf = SunFragment.newInstance();
        bf = BussolaFragment.newInstance();
        mf = MoonFragment.newInstance();
        pf = PeakFragment.newInstance();

        String posizione = getPosizione(p.lat, p.lon);

        if (posizione != null) {
            tl.setTitle(posizione);
        }

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        BottomNavigationMenuView menuview = (BottomNavigationMenuView) navigation.getChildAt(0);
        try {
            Field shiftingMode = menuview.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuview, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuview.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuview.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
        }

        navigation.setSelectedItemId(R.id.navigation_risultati);
    }

    public String getPosizione(Double latitude, Double longitude){
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        String citta = null;
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                        citta = adr.getLocality() + ", " + adr.getCountryName();
                    }else{
                        citta = adr.getAdminArea() + ", " + adr.getCountryName();
                    }
                }
            }
            if(citta == null) citta = "Non disponibile!";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return citta;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        Fragment selectedFragment = null;
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_risultati:
                    selectedFragment = sf;
                    break;
                case R.id.navigation_luna:
                    selectedFragment = mf;
                    break;
                case R.id.navigation_bussola:
                    selectedFragment = bf;
                    break;
                case R.id.navigation_meteo:
                    selectedFragment = mt;
                    break;
                case R.id.navigation_peak:
                    selectedFragment = pf;
                    break;
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.frame_layout_risultati, selectedFragment);
            transaction.commit();
            return true;
        }
    };

    public void getImage(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MaxPhotoRef", 0);
        final SharedPreferences.Editor editor = pref.edit();
        Integer day = pref.getInt("day", 32);
        Integer actual_max = pref.getInt("max_daily", 5);

        if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > day){
            editor.putInt("max_daily", 0);
        }else{
            if(actual_max > 0){
                dialog = PickImageDialog.build(new PickSetup().setPickTypes(EPickType.GALLERY, EPickType.CAMERA).setGalleryIcon(R.mipmap.gallery_colored).setCameraIcon(R.mipmap.camera_colored))
                        .setOnClick(new IPickClick() {
                            @Override
                            public void onGalleryClick() {
                                if(checkPermission()) openGallery();
                            }

                            @Override
                            public void onCameraClick() {
                                if(checkPermission()) openCamera();
                            }
                        }).show(this);
            }else{
                Snackbar mySnackbar = Snackbar.make(main, "Superato il limite massimo di foto in un giorno, torna domani!", Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }

    public void openGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_INTENT);
    }

    public void openCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "it.unitn.simob.howsthere.fileprovider",
                        photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, CAMERA_INTENT);
            }
        }
    }

    public void cropImage(Uri uri) {
        if(uri != null){
            Intent i = new Intent(this, PostFeed.class);
            i.putExtra("photo", uri.toString());
            i.putExtra("ID", id_pan);
            i.putExtra("lat", p.lat);
            i.putExtra("lon", p.lon);
            startActivityForResult(i, 15);
        }
    }

    public boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case CAMERA_INTENT:
                if(resultCode == RESULT_OK){
                    if(mCurrentPhotoPath != null) {
                        File m_file = new File(mCurrentPhotoPath);
                        Uri m_imgUri = Uri.fromFile(m_file);
                        cropImage(m_imgUri);
                    }
                }
                break;
            case GALLERY_INTENT:
                if(resultCode == RESULT_OK){
                    cropImage(data.getData());
                }
                break;
            case 15:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtras(extras);
                    intent.putExtra("panoramaid", id_pan);
                    intent.putExtra("addFeed", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }
    }


}
