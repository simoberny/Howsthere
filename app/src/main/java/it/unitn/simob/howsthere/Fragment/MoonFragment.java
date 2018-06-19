package it.unitn.simob.howsthere.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unitn.simob.howsthere.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonFragment extends Fragment {


    public MoonFragment() {
        // Required empty public constructor
    }

    public static MoonFragment newInstance() {
        MoonFragment fragment = new MoonFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_moon, container, false);

        return view;
    }

}
