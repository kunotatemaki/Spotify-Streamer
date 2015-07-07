package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ArtistSearchWidgetFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(ArtistSearchWidgetFragment.class);
    private static final String IS_ANIMATED = "animated";
    Boolean animated = false;
    @Bind(R.id.toolbar_search) Toolbar toolbarSearch;
    @Bind(R.id.back_arrow_on_search)
    ImageView backArrowOnSearch;
    @Bind(R.id.searchview_widget) SearchView searchView;

    public ArtistSearchWidgetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Change appearance of statusBar
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_search_dark));
        }*/
    }

    @Override
    public void onDetach() {
        //Change appearance of statusBar
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
        Utilities.hideSoftKeyboard(getActivity());
        //get the previous toolbar (ArtistListFragment) back
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            ((SearchActivity) getActivity()).getSupportActionBar().show();
        }*/
        super.onDetach();

    }

    @Override
    public void onResume(){
        super.onResume();
        //Change appearance of statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_search_dark));
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        //Change appearance of statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
        Utilities.hideSoftKeyboard(getActivity());
        //get the previous toolbar (ArtistListFragment) back
        if(getActivity() instanceof ToolbarAndRefreshActivity){
            ((SearchActivity) getActivity()).getSupportActionBar().show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            animated = savedInstanceState.getBoolean(IS_ANIMATED);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_widget, container, false);
        ButterKnife.bind(this, view);

        if(getActivity() instanceof ToolbarAndRefreshActivity){
            ((ToolbarAndRefreshActivity) getActivity()).getSupportActionBar().hide();
        }
        if(backArrowOnSearch != null) {
            //make arroy+image clickable (as Whatsapp do)
            backArrowOnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(false); // Do not iconify the widget; expand it at the first time
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if(!animated){
                        animated = true;
                    }else{
                        toolbarSearch.setVisibility(View.VISIBLE);
                        return;
                    }
                    v.removeOnLayoutChangeListener(this);
                    // get the top right corner of the view for the clipping circle
                    int cx = toolbarSearch.getLeft() + toolbarSearch.getRight();
                    int cy = toolbarSearch.getTop() + toolbarSearch.getBottom();

                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            toolbarSearch,
                            cx,
                            cy,
                            0,
                            (float) Math.hypot(toolbarSearch.getWidth(), toolbarSearch.getHeight()));

                    // Set a natural ease-in/ease-out interpolator.
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // make the view visible and start the animation
                    toolbarSearch.setVisibility(View.VISIBLE);
                    animator.start();
                }
            });
        }
        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_ANIMATED, true);
        super.onSaveInstanceState(outState);

    }

}
