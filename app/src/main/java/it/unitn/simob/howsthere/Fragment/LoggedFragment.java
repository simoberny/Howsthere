package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.TwitterCore;

import it.unitn.simob.howsthere.R;

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

        TextView tx = (TextView) view.findViewById(R.id.user);
        tx.setText(currentUser.getDisplayName());
        TextView tel = (TextView) view.findViewById(R.id.telefono);
        tel.setText(currentUser.getEmail());
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

}
