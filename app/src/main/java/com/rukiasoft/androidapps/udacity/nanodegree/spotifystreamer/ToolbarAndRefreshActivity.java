/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;


/**
 * Base activity for activities that need to show a Refresh Layout and a Custom Toolbar
 */
public abstract class ToolbarAndRefreshActivity extends AppCompatActivity{

    private static final String TAG = LogHelper.makeLogTag(ToolbarAndRefreshActivity.class);

    private android.support.v7.widget.Toolbar mToolbar;

    protected SwipeRefreshLayout refreshLayout;
    private Boolean showing = false;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
        //TODO dar la posibilidad de no poner menús
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


    /**
     * Set the toolbar included in the fragment layout as the actionbar
     * @param toolbar toolbar to be added as actionbar
     * @param backIcon  true if back arrow is wanted
     * @param showTitle true if app name has to be showed
     * @param save true if we want to store toolbar as the toolbar variable stored
     */
    public void setToolbarInActivity(@NonNull Toolbar toolbar, Boolean backIcon, Boolean showTitle, Boolean save){
        if(save) mToolbar = toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(backIcon);
            getSupportActionBar().setDisplayShowTitleEnabled(showTitle);
        }

    }

    /**
     * Restore the saved actionbar
     * @param backIcon true if back arrow is wanted
     * @param showTitle true if app name has to be showed
     */
    public void restorePreviousToolbar(Boolean backIcon, Boolean showTitle){
        if(mToolbar != null){
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(backIcon);
                getSupportActionBar().setDisplayShowTitleEnabled(showTitle);
            }
        }
    }

    public void setRefreshLayout(SwipeRefreshLayout _refreshLayout){
        refreshLayout = _refreshLayout;
        if (refreshLayout == null) {
            throw new IllegalStateException("Mising RefreshLayout. Cannot continue.");
        }
        //configure swipeRefreshLayout
        setRefreshLayoutColorScheme(getResources().getColor(R.color.color_scheme_1_1),
                getResources().getColor(R.color.color_scheme_1_2),
                getResources().getColor(R.color.color_scheme_1_3),
                getResources().getColor(R.color.color_scheme_1_4));
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    protected void showRefreshLayoutSwipeProgress() {
        refreshLayout.setRefreshing(true);
        showing = true;
    }

    /**
     * It shows the SwipeRefreshLayout progress if needed
     */
    protected void showRefreshLayoutSwipeProgressIfNeeded() {
        if(showing){
            refreshLayout.setRefreshing(false);
            refreshLayout.setRefreshing(true);
        }
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    protected void hideRefreshLayoutSwipeProgress() {
        refreshLayout.setRefreshing(false);
        showing = false;
    }

    /**
     * Enables swipe gesture
     */
    protected void enableRefreshLayoutSwipe() {
        refreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show
     * refreshing programatically.
     */
    protected  void disableRefreshLayoutSwipe() {
        refreshLayout.setEnabled(false);
    }

    /**
     * Set colors of refreshlayout
     */
    private void setRefreshLayoutColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        refreshLayout.setColorSchemeColors(colorRes1, colorRes2, colorRes3, colorRes4);
    }

}
