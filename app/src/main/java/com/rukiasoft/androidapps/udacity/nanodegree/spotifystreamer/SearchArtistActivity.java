package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;

public class SearchArtistActivity extends AppCompatActivity {

    private SearchArtistActivityFragment searchArtistActivityFragment;
    private SearchBox search;
    private Toolbar toolbar;
    MenuItem magnifyingGlass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_artist);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(null != toolbar) {
            setSupportActionBar(toolbar);

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

        FragmentManager fm = getFragmentManager();
        searchArtistActivityFragment = (SearchArtistActivityFragment) fm.findFragmentByTag("search_fragment");

        // create the fragment and data the first time
        if (searchArtistActivityFragment == null) {
            // add the fragment
            searchArtistActivityFragment = new SearchArtistActivityFragment();
            fm.beginTransaction().add(R.id.search_artist_activity_container, searchArtistActivityFragment, "search_fragment").commit();
            // load the data from the spotify web
            //searchArtistActivityFragment.setData(loadMyData());
            fm.executePendingTransactions();
        }

        search = (SearchBox)findViewById(R.id.searchbox);
        search.enableVoiceRecognition(this);

    }

    @Override
    public void onPostResume(){
        super.onPostResume();
        openSearch();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_artist, menu);
        magnifyingGlass = menu.findItem(R.id.action_search);
        if(search.getVisibility() == View.VISIBLE)
            magnifyingGlass.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                Utilities.showToast(this, getResources().getString(R.string.comming_soon));
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                openSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        switch(search.getVisibility()) {
            case View.VISIBLE:
                closeSearch();
                return;
            default:
                super.onBackPressed();
                return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (true && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openSearch() {
        if(magnifyingGlass != null)
            magnifyingGlass.setVisible(false);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        search.setLogoText(getResources().getString(R.string.search_hint));
        search.setSearchString("");
        search.revealFromMenuItem(R.id.action_search, this);

        search.setMenuListener(new SearchBox.MenuListener() {

            @Override
            public void onMenuClick() {
                // Hamburger has been clicked
                search.toggleSearch();
            }

        });
        search.setSearchListener(new SearchBox.SearchListener() {

            @Override
            public void onSearchOpened() {
                // Use this to tint the screen

            }

            @Override
            public void onSearchClosed() {
                // Use this to un-tint the screen
                closeSearch();
            }

            @Override
            public void onSearchTermChanged() {
                // React to the search term changing
                // Called after it has updated results
            }

            @Override
            public void onSearch(String searchTerm) {
                TextView result = ((TextView) toolbar.findViewById(R.id.toolbar_subtitle));
                result.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                result.setText(searchTerm);
                SearchResult option = new SearchResult(searchTerm,
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_history));
                search.addSearchable(option);
            }

            @Override
            public void onSearchCleared() {

            }

        });

    }

    protected void closeSearch() {
        search.hideCircularly(this);
        if(magnifyingGlass != null)
            magnifyingGlass.setVisible(true);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
