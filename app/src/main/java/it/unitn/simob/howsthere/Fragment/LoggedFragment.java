package it.unitn.simob.howsthere.Fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import it.unitn.simob.howsthere.R;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoggedFragment extends Fragment {

    private FirebaseAuth mAuth;

    public LoggedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logged,container, false);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        TextView tx = view.findViewById(R.id.nome);
        TextView em = view.findViewById(R.id.email);

        String nome_cap = "";
        if(currentUser.getDisplayName().length() > 0){
            nome_cap = toTitleCase(currentUser.getDisplayName());
        }

        tx.setText(nome_cap);
        em.setText(currentUser.getEmail());

        ImageView avatar = view.findViewById(R.id.avatar);
        Picasso.get().load(currentUser.getPhotoUrl()).transform(new CropCircleTransformation()).into(avatar);

        Button out = (Button) view.findViewById(R.id.signout);
        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, new UserFragment()).commit();
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                AuthUI.getInstance()
                        .signOut(getContext());
                AuthUI.getInstance().delete(getContext());
            }
        });

        return view;
    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }

    public static String toTitleCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
