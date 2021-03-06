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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import butterknife.Bind;


/**
 * Base activity for activities that need to show a Refresh Layout and a Custom Toolbar
 */
public abstract class ToolbarAndRefreshActivity extends AppCompatActivity{

    private static final String TAG = LogHelper.makeLogTag(ToolbarAndRefreshActivity.class);

    @Nullable @Bind(R.id.toolbar)Toolbar toolbar;
    @Nullable @Bind(R.id.toolbar_back_image)
    RelativeLayout toolbarBackImage;
    @Nullable @Bind(R.id.toolbar_layout)
    RelativeLayout toolbarLayout;
    @Nullable @Bind(R.id.toolbar_subtitle)
    TextView toolbarSubtitle;
    @Nullable @Bind(R.id.toolbar_artist_item_on_back_image)
    ImageView artistItemImage;

    //private android.support.v7.widget.Toolbar mToolbar;

    protected SwipeRefreshLayout refreshLayout;
    private Boolean showing = false;
    public boolean mIsLargeLayout;     //tablet or phone

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.options, false);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

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
                Intent finalIntent = new Intent(this, SettingsActivity.class);
                startActivity(finalIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setToolbarWithCustomView(boolean value){
        getSupportActionBar().setDisplayHomeAsUpEnabled(!value);
        getSupportActionBar().setDisplayShowTitleEnabled(!value);
        if(toolbarLayout != null) {
            if(value){
                toolbarLayout.setVisibility(View.VISIBLE);
            }else{
                toolbarLayout.setVisibility(View.GONE);
            }
        }
    }



    public void setRefreshLayout(SwipeRefreshLayout _refreshLayout){
        refreshLayout = _refreshLayout;
        if (refreshLayout == null) {
            return;
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
    public void showRefreshLayoutSwipeProgress() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setRefreshing(true);
        showing = true;
    }



    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void hideRefreshLayoutSwipeProgress() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setRefreshing(false);
        showing = false;
    }

    /**
     * Enables swipe gesture
     */
    public void enableRefreshLayoutSwipe() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show
     * refreshing programatically.
     */
    public  void disableRefreshLayoutSwipe() {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setEnabled(false);
    }

    /**
     * Set colors of refreshlayout
     */
    private void setRefreshLayoutColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        if (refreshLayout == null) {
            return;
        }
        refreshLayout.setColorSchemeColors(colorRes1, colorRes2, colorRes3, colorRes4);
    }

}
