package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.FeedAdapter;
import it.unitn.simob.howsthere.MainActivity;
import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.R;

public class MioFeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseFirestore db;

    public MioFeedFragment() {
    }

    public static MioFeedFragment newInstance() {
        MioFeedFragment fragment = new MioFeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        feedList = new ArrayList<Feed>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mio_feed, container, false);

        if(currentUser != null){
            adapter = new FeedAdapter(getActivity(), feedList); //Inizializzazione adapter per la lista

            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setSmoothScrollbarEnabled(true);

            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_mio);
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
        }
        return view;
    }

    @Override
    public void onRefresh() {
        feedList.clear();
        loadRecyclerViewData();
    }

    private void loadRecyclerViewData() {
        mSwipeRefreshLayout.setRefreshing(true);

        db.collection("feeds").whereEqualTo("uid", currentUser.getUid()).orderBy("timeStamp", Query.Direction.DESCENDING)
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
                        Snackbar mySnackbar = Snackbar.make(((MainActivity)getActivity()).findViewById(R.id.frame_layout), "Impossibile ottenere i feed! Riprovare fra poco...", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                    }

                    adapter.notifyDataSetChanged(); //Notifico che sono stati inseriti dei dati nell'adattatore
                    mSwipeRefreshLayout.setRefreshing(false);

                    recyclerView.getLayoutManager().scrollToPosition(0);
                }
            });
    }
}
