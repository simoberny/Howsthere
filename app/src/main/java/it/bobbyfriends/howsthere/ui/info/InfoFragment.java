package it.bobbyfriends.howsthere.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import it.bobbyfriends.howsthere.MainActivity;
import it.bobbyfriends.howsthere.R;

public class InfoFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.cyan_500));

        View root = inflater.inflate(R.layout.fragment_info, container, false);

        return root;
    }
}