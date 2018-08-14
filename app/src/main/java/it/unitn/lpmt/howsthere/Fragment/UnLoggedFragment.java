package it.unitn.lpmt.howsthere.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;

import it.unitn.lpmt.howsthere.R;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class UnLoggedFragment extends Fragment {

    private static final int RC_SIGN_IN = 123;

    public UnLoggedFragment() { }

    public static UnLoggedFragment newInstance() {
        UnLoggedFragment fragment = new UnLoggedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_un_logged, container, false);

        Button login = (Button) view.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setLogo(R.drawable.icon_login)
                                .setIsSmartLockEnabled(false)
                                .setTheme(R.style.LoginTheme)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.TwitterBuilder().build()))
                                .build(),
                        RC_SIGN_IN);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                showSnackbar("Login avvenuto con successo!");
                return;
            } else {
                if (response == null) {
                    showSnackbar("Login fallito!");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar("Rete internet non disponibile!");
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

}
