package it.unitn.simob.howsthere.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.unitn.simob.howsthere.R;

import static android.app.Activity.RESULT_OK;

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
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction;
        transaction=manager.beginTransaction();
        transaction.add(R.id.settings, SettingsFragment.newInstance());
        transaction.commit();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                showSnackbar("Loggato!");
                return;
            } else {
                if (response == null) {
                    showSnackbar("Cancellato");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar("No network");
                    return;
                }

                showSnackbar("No network");
                Log.e("ERRORACT", "Sign-in error: ", response.getError());
            }
        }
    }

    private void showSnackbar(String string){
        Toast.makeText(this.getContext(), string, Toast.LENGTH_LONG);
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
