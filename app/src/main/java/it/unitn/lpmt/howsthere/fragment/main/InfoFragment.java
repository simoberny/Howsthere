package it.unitn.lpmt.howsthere.fragment.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import it.unitn.lpmt.howsthere.MainActivity;
import it.unitn.lpmt.howsthere.R;

public class InfoFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.cyan_500));

        View root = inflater.inflate(R.layout.fragment_info, container, false);

        ((Button) root.findViewById(R.id.github)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InfoFragment.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/simoberny/Howsthere/tree/v2")));
            }
        });
        ((TextView) root.findViewById(R.id.simone)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) root.findViewById(R.id.matteo)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) root.findViewById(R.id.andrea)).setMovementMethod(LinkMovementMethod.getInstance());

        return root;
    }
}