package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import java.util.List;

import butterknife.ButterKnife;

public class SearchActivity extends MediaControlsActivity implements ArtistListFragment.ArtistListSearchClickListener,
ArtistListFragment.ArtistListFragmentSelectionListener, TopTracksFragment.TopTracksFragmentSelectionListener{

    private static final String TAG = LogHelper.makeLogTag(SearchActivity.class);
    private static final int FULL_SCREEN_PLAYER_CODE = 151;
    public static final String LIST_OF_SONGS = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.searchactivity.listofsongs";
    private ArtistListFragment artistListFragment;
    private TopTracksFragment topTracksFragment;
    boolean mActivityRecreated = false;
    private ArtistSearchWidgetFragment artistSearchWidgetFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        //set the toolbar
        setSupportActionBar(toolbar);

        if(toolbarBackImage != null) {
            //make arroy+image clickable (as Whatsapp do)
            toolbarBackImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        FragmentManager fm = getFragmentManager();
        artistListFragment = (ArtistListFragment) fm.findFragmentByTag(ArtistListFragment.class.getSimpleName());
        topTracksFragment = (TopTracksFragment) fm.findFragmentByTag(TopTracksFragment.class.getSimpleName());
        artistSearchWidgetFragment = (ArtistSearchWidgetFragment)fm.findFragmentById(R.id.search_fragment);

        // create the fragment and data the first time
        if (artistListFragment == null) {
            // add the artist fragment
            artistListFragment = new ArtistListFragment();
            fm.beginTransaction().add(R.id.main_container, artistListFragment, ArtistListFragment.class.getSimpleName()).commit();
            fm.executePendingTransactions();
        }

        if(mIsLargeLayout){
            // add the toptracks fragment
            topTracksFragment = new TopTracksFragment();
            fm.beginTransaction().add(R.id.toptracks_container, topTracksFragment, TopTracksFragment.class.getSimpleName()).commit();
            fm.executePendingTransactions();
        }

        //Check if the activity is recreated
        if (savedInstanceState != null) {
            // activity recreated
            mActivityRecreated = true;
        }

        //if activity receives list of tracks, it means it is recreated from notification click
        if(getIntent().hasExtra(LIST_OF_SONGS)){
            LogHelper.d(TAG, "tracks");
            List<ListItem> songs = getIntent().getParcelableArrayListExtra(LIST_OF_SONGS);
            if(topTracksFragment != null)
                topTracksFragment.setTopTracks(songs);
            //TODO comprobaaaar
        }
    }

    /**
     * handles the SearchView results
     * @param intent intent to handle
     */
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = query.trim();
            //Save recent query
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            //hide SearchView widget
            hideSearchWidget();
            //run search
            artistListFragment.searchArtist(query);
        }
    }

    /*@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save activity recreated
        savedInstanceState.putBoolean(STATE_ACTIVITY, true);

        super.onSaveInstanceState(savedInstanceState);
    }*/

    @Override
    public void onPostResume(){
        //open search byDefault if activity is first created
        super.onPostResume();
         if(!mActivityRecreated) {
            onSearchClick();
            mActivityRecreated = true;
        }
    }


    @Override
    public void onBackPressed(){

        if (hideSearchWidget()) return; //if searchview is shown, close it
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            finish();
        else
            finishAfterTransition();
    }

    /**
     * Clean the search history form the suggestions content provider
     */
    public void cleanRecentSearches(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getApplicationContext(),
                                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
                        suggestions.clearHistory();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_recent_searches_question)).setPositiveButton(
                getResources().getString(R.string.Delete), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.cancel), dialogClickListener).show();
    }

    /**
     * Show SearchView as a widget overlapping actionbar, when the magnifying glass is clicked.
     */
    @Override
    public void onSearchClick() {
        ArtistSearchWidgetFragment artistSearchWidgetFragment = new ArtistSearchWidgetFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .add(R.id.main_container, artistSearchWidgetFragment, ArtistSearchWidgetFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
        fm.executePendingTransactions();

    }

    /**
     * Hide search widget
     */
    private Boolean hideSearchWidget(){

        return getFragmentManager().popBackStackImmediate();
    }


    @Override
    public void onArtistListFragmentItemSelected(ListItem item) {
        hideSearchWidget();
        FragmentManager fm = getFragmentManager();
        topTracksFragment = (TopTracksFragment) fm.findFragmentByTag(TopTracksFragment.class.getSimpleName());

        // create the fragment and data the first time
        if (topTracksFragment == null) {
            // add the fragment
            topTracksFragment = new TopTracksFragment();
        }
        topTracksFragment.setArtist(item);

        if(mIsLargeLayout){
            //largeScreen, no fragment transaction (both are shown).
            return;
        }

        FragmentTransaction ft = fm.beginTransaction()
            .replace(R.id.main_container, topTracksFragment, TopTracksFragment.class.getSimpleName())
            .addToBackStack(null);
        ft.commit();

    }

    @Override
    public void onTopTracksFragmentItemSelected(ListItem item, Integer position, List<View> sharedElements) {
        if(!musicBound){
            //connect again and sen
            topTracksFragment.sendTopTracksToService();
        }
        sendSetCurrentSongMessageToService(position);
        sendPlayMessageToService();
        showPlaybackControls();
        //start FullPlayerScreen
        Intent fullPlayerIntent = new Intent(this, FullScreenPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MusicService.SONG_INFO, item);
        bundle.putBoolean(MusicServiceActivity.START_CONNECTION, true);
        fullPlayerIntent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,

                    // Now we provide a list of Pair items which contain the view we can transitioning
                    // from, and the name of the view it is transitioning to, in the launched activity
                    new Pair<>(sharedElements.get(0),
                            getResources().getString(R.string.track_pic_imageview)),
                    new Pair<>(sharedElements.get(1),
                            getResources().getString(R.string.song_name_textview)),
                    new Pair<>(sharedElements.get(2),
                            getResources().getString(R.string.album_name_textview)),
                    new Pair<>(sharedElements.get(3),
                            getResources().getString(R.string.artist_name_textview)));
            startActivityForResult(fullPlayerIntent, FULL_SCREEN_PLAYER_CODE, activityOptions.toBundle());
        } else {
            startActivityForResult(fullPlayerIntent, FULL_SCREEN_PLAYER_CODE);
        }
    }



    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        //musicSrv=null;
        super.onDestroy();
    }

    @Override
    public void playingSong(Bundle bundle){
        super.playingSong(bundle);
        boolean updateView;
        if(topTracksFragment != null){
            topTracksFragment.setPlayingSong(bundle);
        }

    }

    @Override
    public void pausedSong(Bundle bundle){
        super.pausedSong(bundle);
        boolean updateView;
        if(topTracksFragment != null){
            topTracksFragment.setPausedSong(bundle);
        }
    }

    @Override
    public void finishedPlayingSong(Bundle bundle){
        super.finishedPlayingSong(bundle);
        boolean updateView;
        if (topTracksFragment != null){
            topTracksFragment.setFinishedPlayingSong(bundle);
        }
    }
}
