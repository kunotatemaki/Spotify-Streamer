package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Raúl Feliz Alonso on 22/06/15.
 */
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
