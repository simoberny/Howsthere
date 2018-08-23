package it.unitn.lpmt.howsthere.Fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unitn.lpmt.howsthere.Adapter.FeedAdapter;
import it.unitn.lpmt.howsthere.Adapter.MyLinearLayoutManager;
import it.unitn.lpmt.howsthere.MainActivity;
import it.unitn.lpmt.howsthere.Oggetti.Feed;
import it.unitn.lpmt.howsthere.Oggetti.Panorama;
import it.unitn.lpmt.howsthere.Oggetti.PanoramiStorage;
import it.unitn.lpmt.howsthere.R;

public class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;
    private FirebaseAuth mAuth;
    private RelativeLayout nofeed;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseFirestore db;
    FirebaseUser currentUser;

    public FeedFragment() { }

    public static FeedFragment newInstance(Bundle bun) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = bun;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            ((MainActivity)getActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        feedList = new ArrayList<Feed>();
    }

    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= 21) {
            ((MainActivity)getActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
        super.onResume();
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

            if (pan_id != null) {
                feed_to_db(uri, filename, descrizione, pan_id, posizione);
            }
            getArguments().clear();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        nofeed = view.findViewById(R.id.nofeed);
        adapter = new FeedAdapter(getActivity(), feedList); //Inizializzazione adapter per la lista

        mLayoutManager = new MyLinearLayoutManager(getActivity());
        mLayoutManager.setSmoothScrollbarEnabled(true);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);
        mSwipeRefreshLayout.setRefreshing(true);

        onRefresh();
        return view;
    }

    private void feed_to_db(String uri, String filename, String descrizione, String pan_id, String posizione) {
        mSwipeRefreshLayout.setRefreshing(true);

        Panorama p = null;
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        final SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MaxPhotoRef", 0);
        final SharedPreferences.Editor editor = pref.edit();

        final Feed ne = new Feed(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), posizione, uri, dateFormat.format(date), filename, descrizione);

        if(pan_id != null){
            PanoramiStorage panoramiStorage = PanoramiStorage.panorami_storage;
            p = panoramiStorage.getPanoramabyID(pan_id);

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

                ne.setPanoramaID(pan_id);
                ne.setP(serializedObject);
            }
        }

        //Salvo nel database
        db.collection("feeds")
                .add(ne)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        onRefresh();
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
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        mSwipeRefreshLayout.setRefreshing(true);

        db.collection("feeds").orderBy("timeStamp", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            final Feed getFromDb = document.toObject(Feed.class);
                            getFromDb.setID(document.getId());

                            if(feedList.size() == 0){
                                feedList.add(getFromDb);
                            }

                            boolean exist = false;
                            for(int i = 0; i < feedList.size(); i++){
                                if(feedList.get(i).getID().equals(getFromDb.getID())){
                                    feedList.set(i, getFromDb);
                                    exist = true;
                                }
                            }

                            if(!exist)feedList.add(0, getFromDb);
                        }
                    } else {
                        Log.w("Errorcloud", "Error getting documents.", task.getException());
                    }

                    if(feedList.size() > 0){
                        nofeed.setVisibility(View.GONE);
                    }else{
                        nofeed.setVisibility(View.VISIBLE);
                    }

                    recyclerView.getRecycledViewPool().clear();
                    adapter.notifyDataSetChanged(); //Notifico che sono stati inseriti dei dati nell'adattatore
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
    }
}
