package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistSearchActivity extends AppCompatActivity implements ArtistListFragment.ArtistListSearchClickListener{

    private Fragment retainedFragment;
    boolean mActivityRecreated = false;
    static final String STATE_ACTIVITY = "first_created";
    private Boolean showSearchIcon = true;
    private android.support.v7.widget.Toolbar mToolbar;

    public Boolean getShowSearchIcon() {
        return showSearchIcon;
    }

    public void setShowSearchIcon(Boolean showSearchIcon) {
        this.showSearchIcon = showSearchIcon;
        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_list);
        ButterKnife.inject(this);

        FragmentManager fm = getFragmentManager();
        retainedFragment = fm.findFragmentByTag("search_fragment");

        // create the fragment and data the first time
        if (retainedFragment == null) {
            // add the fragment
            retainedFragment = new ArtistListFragment();
            fm.beginTransaction().add(R.id.activity_artist_list_container, retainedFragment, "search_fragment").commit();

        }
        fm.executePendingTransactions();

        //Check if the activity is recreated
        if (savedInstanceState != null) {
            // Restore recreated or not
            mActivityRecreated = savedInstanceState.getBoolean(STATE_ACTIVITY);
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
            if(retainedFragment instanceof ArtistListFragment)
                ((ArtistListFragment)retainedFragment).searchArtist(query);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save activity recreated
        savedInstanceState.putBoolean(STATE_ACTIVITY, true);

        super.onSaveInstanceState(savedInstanceState);
    }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                Utilities.showToast(this, getResources().getString(R.string.coming_soon));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                .add(R.id.activity_artist_list_container, artistSearchWidgetFragment, "search_widget")
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

    /**
     * Set the toolbar included in the fragment layout as the actionbar
     * @param toolbar toolbar to be added as actionbar. If null, the toolbar variable stored will be set
     * @param backIcon  true if back arrow is wanted
     * @param showTitle true if app name has to be showed
     * @param save true if we want to store toolbar as the toolbar variable stored
     */
    public void setToolbarInActivity(Toolbar toolbar, Boolean backIcon, Boolean showTitle, Boolean save){
        if(save) mToolbar = toolbar;
        Toolbar localToolbar = toolbar == null? mToolbar : toolbar;
        if(localToolbar != null){
            setSupportActionBar(localToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(backIcon);
                getSupportActionBar().setDisplayShowTitleEnabled(showTitle);
            }
        }
    }


}
