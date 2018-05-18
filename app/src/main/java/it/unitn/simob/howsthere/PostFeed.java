package it.unitn.simob.howsthere;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.UUID;

import it.unitn.simob.howsthere.Oggetti.Feed;
import retrofit2.http.Url;


public class PostFeed extends AppCompatActivity {
    private static final String TAG = "Feed";

    Bitmap mBitmap;
    CropImageView cropImageView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Caricando...");
        progressDialog.setTitle("Database");

        Intent data = getIntent();

        mBitmap = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("photo"), 0, getIntent().getByteArrayExtra("photo").length);

        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageBitmap(mBitmap);
        cropImageView.setOnCropWindowChangedListener(new CropImageView.OnSetCropWindowChangeListener() {
            @Override
            public void onCropWindowChanged() {
                mBitmap = cropImageView.getCroppedImage();
            }
        });
        cropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
                mBitmap = cropImageView.getCroppedImage();
            }
        });

        Button invia = findViewById(R.id.invia);
        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                String file_name = UUID.randomUUID() + ".jpg";

                final StorageReference feedRef = storageRef.child("images/" + file_name);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = feedRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

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
                            sendUri(downloadUri);
                        } else {
                        }
                    }
                });
            }
        });
    }

    private void sendUri(Uri down){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("uri", down.toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

}
