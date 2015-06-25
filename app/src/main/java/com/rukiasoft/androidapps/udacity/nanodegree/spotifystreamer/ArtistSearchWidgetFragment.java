package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ArtistSearchWidgetFragment extends Fragment {

    @InjectView(R.id.toolbar_search) Toolbar toolbar;
    @InjectView(R.id.searchview_widget)
    SearchView searchView;

    private static final String TAG = LogHelper.makeLogTag(ArtistSearchWidgetFragment.class);


    public ArtistSearchWidgetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LogHelper.d(TAG, "onAttach");
        //Change appearance of statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_search_dark));
        }
    }

    @Override
    public void onDetach() {
        //Change appearance of statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
        Utilities.hideSoftKeyboard(getActivity());
        super.onDetach();

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_widget, container, false);
        ButterKnife.inject(this, view);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(false); // Do not iconify the widget; expand it at the first time
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);
        return view;
    }


}
