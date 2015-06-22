package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TopTracksActivity extends AppCompatActivity {

    Fragment retainedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if(!getIntent().hasExtra("artist_item")){
            //no id. Finish Activity
            setResult(RESULT_CANCELED);
            finish();
        }
        ArtistItem artist = getIntent().getExtras().getParcelable("artist_item");


        FragmentManager fm = getFragmentManager();
        retainedFragment = fm.findFragmentByTag("top_track_fragment");

        // create the fragment and data the first time
        if (retainedFragment == null) {
            // add the fragment
            retainedFragment = new TopTracksActivityFragment();
            fm.beginTransaction().add(R.id.top_tracks_activity_container, retainedFragment, "top_track_fragment").commit();

        }
        fm.executePendingTransactions();

        if(retainedFragment instanceof TopTracksActivityFragment)
            ((TopTracksActivityFragment)retainedFragment).setArtist(artist);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                Utilities.showToast(this, getResources().getString(R.string.comming_soon));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        setResult(RESULT_OK);
        super.onBackPressed();
    }

}
