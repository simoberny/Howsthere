package it.unitn.simob.howsthere;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.fenchtose.nocropper.CropperView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import it.unitn.simob.howsthere.Oggetti.Feed;
import retrofit2.http.Url;


public class PostFeed extends AppCompatActivity {
    private static final String TAG = "Feed";

    private Bitmap mBitmap;
    private ProgressDialog progressDialog;
    private CropperView crop;
    private int rotationCount = 0;
    private TextView desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Caricando...");
        progressDialog.setTitle("Database");

        Intent data = getIntent();
        Uri file = Uri.parse(data.getStringExtra("photo"));

        try {
           mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBitmap = getResizedBitmap(mBitmap, 1000);

        crop = findViewById(R.id.cropper_view);
        crop.setImageBitmap(mBitmap);
        desc = findViewById(R.id.desc);

        ImageView rotate = findViewById(R.id.rotate_button);
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        Button invia = findViewById(R.id.invia);
        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                mBitmap = crop.getCroppedBitmap().getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] data = baos.toByteArray();
            }
        });
    }

    private void uploadToFirebase(final byte [] data){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        final String file_name = UUID.randomUUID() + ".jpg";
        final StorageReference feedRef = storageRef.child("images/" + file_name);

        UploadTask uploadTask = feedRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.post_feed_con), "Caricamento foto fallito!", Snackbar.LENGTH_LONG);
                mySnackbar.setAction("Riprova", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadToFirebase(data);
                    }
                });
                mySnackbar.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar mySnackbar = Snackbar.make(findViewById(R.id.post_feed_con), "Caricamento avvenuto con successo!", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return feedRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    sendUri(downloadUri, file_name);
                }
            }
        });
    }

    private void sendUri(Uri down, String filename){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("filename", filename);
        returnIntent.putExtra("uri", down.toString());
        returnIntent.putExtra("descrizione", desc.getText().toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void rotateImage() {
        if (mBitmap == null) {
            Log.e(TAG, "bitmap is not loaded yet");
            return;
        }

        mBitmap = rotateBitmap(mBitmap, 90);
        crop.setImageBitmap(mBitmap);
        rotationCount++;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
