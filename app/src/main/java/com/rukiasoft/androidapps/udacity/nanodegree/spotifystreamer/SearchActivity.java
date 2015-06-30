package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AlertDialog;
import android.transition.TransitionInflater;
import android.view.View;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import java.util.List;

import butterknife.ButterKnife;

public class SearchActivity extends MediaControlsActivity implements ArtistListFragment.ArtistListSearchClickListener,
ArtistListFragment.ArtistListFragmentSelectionListener, TopTracksFragment.TopTracksFragmentSelectionListener{

    private static final String TAG = LogHelper.makeLogTag(SearchActivity.class);
    private ArtistListFragment artistListFragment;
    private TopTracksFragment topTracksFragment;
    boolean mActivityRecreated = false;
    static final String STATE_ACTIVITY = "first_created";
    private Boolean showSearchIcon = true;


    public Boolean getShowSearchIcon() {
        return showSearchIcon;
    }

    public void setShowSearchIcon(Boolean showSearchIcon) {
        this.showSearchIcon = showSearchIcon;
        invalidateOptionsMenu();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);
        ButterKnife.inject(this);



        FragmentManager fm = getFragmentManager();
        artistListFragment = (ArtistListFragment) fm.findFragmentByTag(ArtistListFragment.class.getSimpleName());
        topTracksFragment = (TopTracksFragment) fm.findFragmentByTag(TopTracksFragment.class.getSimpleName());

        // create the fragment and data the first time
        if (artistListFragment == null) {
            // add the fragment
            artistListFragment = new ArtistListFragment();
            fm.beginTransaction().add(R.id.activity_list_container, artistListFragment, ArtistListFragment.class.getSimpleName()).commit();
            fm.executePendingTransactions();
        }


        //Check if the activity is recreated
        if (savedInstanceState != null) {
            // activity recreated
            mActivityRecreated = true;
        }

        //if activity receives list of tracks, it means it is recreated from notification click
        if(getIntent().hasExtra("list_of_songs")){
            LogHelper.d(TAG, "tracks");
            //TODO lanzar el segundo fragment (provisionaaal)
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
                .add(R.id.activity_list_container, artistSearchWidgetFragment, ArtistSearchWidgetFragment.class.getSimpleName())
                .addToBackStack(null)
                .commit();
        fm.executePendingTransactions();

        setShowSearchIcon(false);

    }

    /**
     * Hide search widget
     */
    private Boolean hideSearchWidget(){

        return getFragmentManager().popBackStackImmediate();
    }


    @Override
    public void onArtistListFragmentItemSelected(ListItem item, List<View> sharedElements) {
        hideSearchWidget();
        FragmentManager fm = getFragmentManager();
        topTracksFragment = (TopTracksFragment) fm.findFragmentByTag(TopTracksFragment.class.getSimpleName());

        // create the fragment and data the first time
        if (topTracksFragment == null) {
            // add the fragment
            topTracksFragment = new TopTracksFragment();
        }
        topTracksFragment.setArtist(item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            artistListFragment.setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.artist_item_transition));
            //artistListFragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));

            // Create new fragment to add (Fragment B)

            topTracksFragment.setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.artist_item_transition));
            //topTracksFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));

            // Add Fragment B
            FragmentTransaction ft = fm.beginTransaction()
                    .replace(R.id.activity_list_container, topTracksFragment, TopTracksFragment.class.getSimpleName())
                    .addToBackStack(null)
                    .addSharedElement(sharedElements.get(0), getResources().getString(R.string.artist_name_imageview))
                    .addSharedElement(sharedElements.get(1), getResources().getString(R.string.artist_name_textview))
                    .addSharedElement(sharedElements.get(2), getResources().getString(R.string.toolbar_toptracks_view));
            ft.commit();
            //TODO check why it is not working

        }
        else {
            FragmentTransaction ft = fm.beginTransaction()
                    .replace(R.id.activity_list_container, topTracksFragment, TopTracksFragment.class.getSimpleName())
                    .addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void onTopTracksFragmentItemSelected(ListItem item, Integer position, List<View> sharedElements) {

        sendSetCurrentSongMessageToService(position);
        sendSetAsForegroundMessageToService();
        sendPlayMessageToService();
        showPlaybackControls();
        //setSongInfo(item);
        /*Intent topTracksIntent = new Intent(this, FullScreenPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("artist_item", item);
        topTracksIntent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,

                    // Now we provide a list of Pair items which contain the view we can transitioning
                    // from, and the name of the view it is transitioning to, in the launched activity
                    new Pair<>(sharedElements.get(0),
                            getResources().getString(R.string.track_name_imageview)),
                    new Pair<>(sharedElements.get(1),
                            getResources().getString(R.string.song_name_textview)),
                    new Pair<View, String>(sharedElements.get(2),
                            getResources().getString(R.string.album_name_textview)));
            startActivity(topTracksIntent, activityOptions.toBundle());
        } else {
            startActivity(topTracksIntent);
        }*/
    }



    @Override
    protected void onDestroy() {
        //stopService(playIntent);
        //musicSrv=null;
        super.onDestroy();
    }

    public void setTracks(List<ListItem> tracks){
        sendSetSongListMessageToService(tracks);
    }

    @Override
    public void playingSong(Bundle bundle){
        super.playingSong(bundle);
        if(bundle.containsKey(MusicService.SONG_POSITION) && topTracksFragment != null){
            int currentSong = bundle.getInt(MusicService.SONG_POSITION);
            topTracksFragment.setPlayingSong(currentSong);
        }
    }

    @Override
    public void pausedSong(Bundle bundle){
        super.pausedSong(bundle );
        if(bundle.containsKey(MusicService.SONG_POSITION) && topTracksFragment != null){
            topTracksFragment.setPausedSong(bundle.getInt(MusicService.SONG_POSITION));
        }
    }

    @Override
    public void finishedPlayingSong(int currentSong){
        super.finishedPlayingSong(currentSong);
        if(topTracksFragment != null){
            topTracksFragment.setFinishedPlayingSong(currentSong);
        }
    }
}
