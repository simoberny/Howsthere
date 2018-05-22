package it.unitn.simob.howsthere.Fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stfalcon.frescoimageviewer.ImageViewer;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.FeedAdapter;
import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.PostFeed;
import it.unitn.simob.howsthere.R;

public class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;
    private FirebaseAuth mAuth;
    TextView nofeed;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseFirestore db;

    public FeedFragment() { }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        feedList = new ArrayList<Feed>();

        FloatingActionButton add_feed = view.findViewById(R.id.add_feed);
        add_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickImageDialog.build(new PickSetup().setPickTypes(EPickType.GALLERY, EPickType.CAMERA).setGalleryIcon(R.mipmap.gallery_colored).setCameraIcon(R.mipmap.camera_colored))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                Intent i = new Intent(getContext(), PostFeed.class);
                                Bitmap bitmap = r.getBitmap();
                                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
                                i.putExtra("photo", bs.toByteArray());
                                startActivityForResult(i, 15);
                            }
                        })
                        .setOnPickCancel(new IPickCancel() {
                            @Override
                            public void onCancelClick() { }
                        }).show(getActivity().getSupportFragmentManager());
            }
        });

        if(currentUser == null){
            add_feed.setVisibility(View.GONE);
        }else{
            add_feed.setVisibility(View.VISIBLE);
        }

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

        nofeed = view.findViewById(R.id.nofeed);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 15 && resultCode == getActivity().RESULT_OK) { //PHOTO Return
            Bundle extras = data.getExtras();
            String uri = (String) extras.get("uri");
            String filename = (String) extras.get("filename");

            Date date = new Date();

            final Feed ne = new Feed(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), "A caso", uri , DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(date), filename);
            mSwipeRefreshLayout.setRefreshing(true);

            db.collection("feeds")
                    .add(ne)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            ne.setID(documentReference.getId());
                            feedList.add(0, ne);
                            mSwipeRefreshLayout.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FAILUREWRITE+", "Error adding document", e);
                        }
                    });
        }
    }

    @Override
    public void onRefresh() { mSwipeRefreshLayout.setRefreshing(false); }

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

                        adapter.notifyDataSetChanged(); //Notifico che sono stati inseriti dei dati nell'adattatore
                        mSwipeRefreshLayout.setRefreshing(false);
                        recyclerView.getLayoutManager().scrollToPosition(0);
                    }
                });
    }
}
