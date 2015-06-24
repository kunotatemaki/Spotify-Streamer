package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;


public class ArtistSearchWidgetFragment extends Fragment {

    private static final String TAG = "SearchWidgetFragment";
    private static final String IS_ANIMATED = "animated";
    Boolean animated = false;

    public ArtistSearchWidgetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            animated = savedInstanceState.getBoolean(IS_ANIMATED);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_widget, container, false);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_search);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        SearchView searchView = (SearchView) view.findViewById(R.id.searchview_widget);
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
                        toolbar.setVisibility(View.VISIBLE);
                        return;
                    }
                    v.removeOnLayoutChangeListener(this);
                    // get the top right corner of the view for the clipping circle
                    int cx = toolbar.getLeft() + toolbar.getRight();
                    int cy = toolbar.getTop() + toolbar.getBottom();

                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            toolbar,
                            cx,
                            cy,
                            0,
                            (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight()));

                    // Set a natural ease-in/ease-out interpolator.
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    // make the view visible and start the animation
                    toolbar.setVisibility(View.VISIBLE);
                    animator.start();
                }
            });
        }
        return view;
    }

    @Override
    public void onDestroyView(){

        super.onDestroyView();
        final Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar_search);
        int cx = toolbar.getLeft() + toolbar.getRight();
        int cy = toolbar.getTop() + toolbar.getBottom();

// get the initial radius for the clipping circle
        float initialRadius = (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight());

// start the animation
        Animator animator = ViewAnimationUtils.createCircularReveal(
                toolbar,
                cx,
                cy,
                (float) Math.hypot(toolbar.getWidth(), toolbar.getHeight()),
                0);

        // Set a natural ease-in/ease-out interpolator.
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        // make the view invisible when the animation is done
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                toolbar.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_ANIMATED, true);
        super.onSaveInstanceState(outState);

    }

}
