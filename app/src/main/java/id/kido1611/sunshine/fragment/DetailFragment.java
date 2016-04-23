package id.kido1611.sunshine.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import id.kido1611.sunshine.R;

/**
 * Created by Ahmad on 4/23/2016.
 */
public class DetailFragment extends Fragment {

    public DetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();

        if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){
            ((TextView)rootView.findViewById(R.id.detail_text)).setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        }

        return rootView;
    }
}