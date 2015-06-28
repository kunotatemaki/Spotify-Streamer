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

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;


/**
 * Base activity for activities that need to show a Refresh Layout and a Custom Toolbar
 */
public abstract class ToolbarActivity extends AppCompatActivity{

    private static final String TAG = LogHelper.makeLogTag(ToolbarActivity.class);

    private android.support.v7.widget.Toolbar mToolbar;



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
