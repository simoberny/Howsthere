package it.unitn.simob.howsthere.Fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
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

    SwipeRefreshLayout mSwipeRefreshLayout;

    static final int REQUEST_IMAGE_CAPTURE = 25;
    FirebaseDatabase database;
    DatabaseReference feed;

    public FeedFragment() { }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
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
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);
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
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setReverseLayout(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
                loadRecyclerViewData();
            }
        });

        adapter.notifyDataSetChanged(); //Dico all'adattatore che sono stati aggiunti degli elementi
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 15 && resultCode == getActivity().RESULT_OK) { //PHOTO Return
            Bundle extras = data.getExtras();
            String uri = (String) extras.get("uri");

            Feed ne = new Feed(mAuth.getCurrentUser().getDisplayName(), "A caso", uri , new Date().toString());
            mSwipeRefreshLayout.setRefreshing(true);
            DatabaseReference newPostRef = feed.push();
            newPostRef.setValue(ne);
        }
    }

    @Override
    public void onRefresh() { mSwipeRefreshLayout.setRefreshing(false); }

    private void loadRecyclerViewData() {
        // Showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);

        feed = database.getReference("feeds");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                feedList.add(dataSnapshot.getValue(Feed.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                feedList.remove(dataSnapshot.getValue(Feed.class));
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) { }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        feed.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numChildren = dataSnapshot.getChildrenCount();
                adapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                mLayoutManager.smoothScrollToPosition(recyclerView, null, 0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        feed.addChildEventListener(childEventListener);
    }
}
