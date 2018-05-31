package it.unitn.simob.howsthere.Fragment;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickClick;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unitn.simob.howsthere.Adapter.FeedAdapter;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.Oggetti.Panorama;
import it.unitn.simob.howsthere.PostFeed;
import it.unitn.simob.howsthere.R;

import static android.app.Activity.RESULT_OK;

public class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;
    private FirebaseAuth mAuth;
    private TextView nofeed;
    private PickImageDialog dialog;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseFirestore db;
    private String mCurrentPhotoPath;
    FirebaseUser currentUser;

    Boolean _areLecturesLoaded = false;

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int GALLERY_INTENT = 25;
    public static final int CAMERA_INTENT = 26;

    public FeedFragment() { }

    public static FeedFragment newInstance(Bundle bun) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = bun;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle extras = getArguments();
        if(extras != null) {
            String pan_id = (String) extras.get("panoramaid");
            String posizione = (String) extras.get("posizione");
            String uri = (String) extras.get("uri");
            String filename = (String) extras.get("filename");
            String descrizione = (String) extras.get("descrizione");
            Panorama p = (Panorama) extras.get("panorama");

            if (uri != null) {
                feed_to_db(uri, filename, descrizione, pan_id, posizione, p);
            }

            getArguments().clear();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        currentUser = mAuth.getCurrentUser();
        feedList = new ArrayList<Feed>();

        FloatingActionButton add_feed = view.findViewById(R.id.add_feed);
        add_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        if(currentUser == null){
            add_feed.setVisibility(View.GONE);
        }else{
            add_feed.setVisibility(View.VISIBLE);
        }

        nofeed = view.findViewById(R.id.nofeed);
        adapter = new FeedAdapter(getActivity(), feedList); //Inizializzazione adapter per la lista

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setSmoothScrollbarEnabled(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.setRefreshing(true);
        loadRecyclerViewData();

        return view;
    }

    public void getImage(){
        dialog = PickImageDialog.build(new PickSetup().setPickTypes(EPickType.GALLERY, EPickType.CAMERA).setGalleryIcon(R.mipmap.gallery_colored).setCameraIcon(R.mipmap.camera_colored))
            .setOnClick(new IPickClick() {
                @Override
                public void onGalleryClick() {
                    if(checkPermission()) {
                        openGallery();
                    }
                }

                @Override
                public void onCameraClick() {
                    if(checkPermission()) {
                        openCamera();
                    }
                }
            }).show(getActivity());
    }

    public void openGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_INTENT);
    }

    public void openCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "it.unitn.simob.howsthere.fileprovider",
                        photoFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePicture, CAMERA_INTENT);
            }
        }
    }

    public void cropImage(Uri uri) {
        if(uri != null){
            Intent i = new Intent(getContext(), PostFeed.class);
            i.putExtra("photo", uri.toString());
            startActivityForResult(i, 15);
        }
    }

    public boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
           ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    File m_file = new File(mCurrentPhotoPath);
                    Uri m_imgUri = Uri.fromFile(m_file);
                    cropImage(m_imgUri);
                }

                break;
            case GALLERY_INTENT:
                if(resultCode == RESULT_OK){
                    cropImage(data.getData());
                }
                break;
            case 15:
                if(resultCode == RESULT_OK){
                    dialog.dismiss();
                    Bundle extras = data.getExtras();

                    String pan_id = (String) extras.get("panoramaid");
                    String posizione = (String) extras.get("posizione");
                    String uri = (String) extras.get("uri");
                    String filename = (String) extras.get("filename");
                    String descrizione = (String) extras.get("descrizione");

                    feed_to_db(uri, filename, descrizione, pan_id, posizione, null);
                }
        }
    }

    private void feed_to_db(String uri, String filename, String descrizione, String pan_id, String posizione, Panorama p) {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        mSwipeRefreshLayout.setRefreshing(true);

        final Feed ne = new Feed(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), posizione, uri, dateFormat.format(date), filename, descrizione, pan_id);
        if(p != null){
            String serializedObject = "";
            try {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream so = new ObjectOutputStream(bo);
                so.writeObject(p);
                so.flush();
                serializedObject = new String(Base64.encode(bo.toByteArray(), Base64.DEFAULT));
            } catch (Exception e) {
                System.out.println(e);
            }

            ne.setP(serializedObject);
        }

        final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MaxPhotoRef", 0);
        final SharedPreferences.Editor editor = pref.edit();

        db.collection("feeds")
                .add(ne)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        ne.setID(documentReference.getId());
                        mSwipeRefreshLayout.setRefreshing(false);
                        editor.putInt("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                        editor.putInt("max_daily", pref.getInt("max_daily", 0) + 1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FAILUREWRITE+", "Error adding document", e);
                    }
                });
    }

    @Override
    public void onRefresh() {
        feedList.clear();
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        mSwipeRefreshLayout.setRefreshing(true);

        db.collection("feeds").orderBy("timeStamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Feed getFromDb = document.toObject(Feed.class);
                            if(!feedList.contains(getFromDb)) {
                                Feed newf = document.toObject(Feed.class);
                                newf.setID(document.getId());
                                feedList.add(newf);
                            }
                        }
                    } else {
                        Log.w("Errorcloud", "Error getting documents.", task.getException());
                    }

                    if(feedList.size() > 0){
                        nofeed.setVisibility(View.GONE);
                    }else{
                        nofeed.setVisibility(View.VISIBLE);
                    }

                    recyclerView.getLayoutManager().scrollToPosition(0);
                    adapter.notifyDataSetChanged(); //Notifico che sono stati inseriti dei dati nell'adattatore
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
    }
}
