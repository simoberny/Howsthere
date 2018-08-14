package it.unitn.lpmt.howsthere.Fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.unitn.lpmt.howsthere.MainActivity;
import it.unitn.lpmt.howsthere.R;

public class UserFragment extends Fragment {
    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 123;

    public UserFragment() {}

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 21) {
            ((MainActivity)getActivity()).getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction;
        transaction=manager.beginTransaction();
        transaction.add(R.id.settings, SettingsFragment.newInstance());
        transaction.commit();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        return view;
    }

    private void updateUI(final FirebaseUser user){
        if(user == null){
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.user, new UnLoggedFragment()).commit();
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.user, new LoggedFragment()).commit();
        }
    }
}
