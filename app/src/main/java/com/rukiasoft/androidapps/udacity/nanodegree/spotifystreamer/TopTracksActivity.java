package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

public class TopTracksActivity extends AppCompatActivity {

    private Fragment retainedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        //get artist item
        if(!getIntent().hasExtra("artist_item")){
            //no artist item. Finish Activity
            setResult(RESULT_CANCELED);
            finish();
        }
        ArtistListItem artist = getIntent().getExtras().getParcelable("artist_item");


        FragmentManager fm = getFragmentManager();
        retainedFragment = fm.findFragmentByTag("top_track_fragment");

        // create the fragment and data the first time
        if (retainedFragment == null) {
            // add the fragment
            retainedFragment = new TopTracksFragment();
            fm.beginTransaction().add(R.id.top_tracks_activity_container, retainedFragment, "top_track_fragment").commit();

        }
        fm.executePendingTransactions();

        if(retainedFragment instanceof TopTracksFragment)
            ((TopTracksFragment)retainedFragment).setArtist(artist);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_settings:
                Utilities.showToast(this, getResources().getString(R.string.coming_soon));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        //return normally
        setResult(RESULT_OK);
        super.onBackPressed();
    }

}
