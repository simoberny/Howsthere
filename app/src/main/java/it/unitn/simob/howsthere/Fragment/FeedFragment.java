package it.unitn.simob.howsthere.Fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import it.unitn.simob.howsthere.Adapter.FeedAdapter;
import it.unitn.simob.howsthere.Oggetti.Feed;
import it.unitn.simob.howsthere.R;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FeedAdapter adapter;
    private List<Feed> feedList;
    private FirebaseAuth mAuth;
    private ImageView im;

    static final int REQUEST_IMAGE_CAPTURE = 25;

    public FeedFragment() {
    }

    public static FeedFragment newInstance() {
        FeedFragment fragment = new FeedFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        im = (ImageView) view.findViewById(R.id.photo);

        FloatingActionButton add_feed = view.findViewById(R.id.add_feed);
        add_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        if(currentUser == null){
            add_feed.setVisibility(View.GONE);
        }else{
            add_feed.setVisibility(View.VISIBLE);
        }

        feedList = new ArrayList<>(); //Inizializzazione lista dei feed
        adapter = new FeedAdapter(getActivity(), feedList); //Inizializzazione adapter per la lista

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        //Oggetti statici caricati nella feed
        Feed feed = new Feed("Simone Bernabè",
                "Metro Manila, Philippines",
                "https://campingselection-528047.c.cdn77.org/media/default/images/en-us/editorial-area/region-trentino.jpg",
                "3 DAYS AGO");
        feedList.add(0, feed);

        feed = new Feed("Matteo Dal Ponte",
                "Metro Manila, Philippines",
                "https://www.boscolo.com/it/viaggi/files/it_viaggi/styles/bootstrap_header/public/trentino-alto-adige-testata.jpg?itok=OEva-6rR",
                "1 DAY AGO");
        feedList.add(0, feed);

        feed = new Feed("Andrea Filippi",
                "Metro Manila, Philippines",
                "https://c1.staticflickr.com/5/4297/35852716531_e9f57be43b_b.jpg",
                "10 HOURS AGO");
        feedList.add(0, feed);

        feed = new Feed("Marco Rossi",
                "Metro Manila, Philippines",
                "https://media.istockphoto.com/photos/sun-at-the-sky-with-copy-space-picture-id155366999?k=6&m=155366999&s=612x612&w=0&h=EFpmVaOq8RHHN5q1fq1qHDhn_V_YS2Ex_Z7UDD2efEs=",
                "26 MINUTES AGO");
        feedList.add(0, feed);

        //Dico all'adattatore che sono stati aggiunti degli elementi
        adapter.notifyDataSetChanged();

        return view;
    }


    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 25 && resultCode == getActivity().RESULT_OK) { //PHOTO Return
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            im.setImageBitmap(imageBitmap);
        }
    }

}
