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

import android.os.Bundle;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.NetworkHelper;


/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 */
public abstract class MediaControlsActivity extends MusicServiceActivity {

    private static final String TAG = LogHelper.makeLogTag(MediaControlsActivity.class);
    private static final String STATE_CONTROLS = "state_controls";
    private Boolean showingControls = false;


    private MediaControlsFragment mControlsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogHelper.d(TAG, "Activity onCreate");
        if(savedInstanceState != null && savedInstanceState.containsKey(STATE_CONTROLS)) {
            showingControls = savedInstanceState.getBoolean(STATE_CONTROLS);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save play controls state
        savedInstanceState.putBoolean(STATE_CONTROLS, showingControls);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogHelper.d(TAG, "Activity onStart");
        mControlsFragment = (MediaControlsFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);
        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }
        if(!showingControls) hidePlaybackControls();
        else showPlaybackControls();

    }

    @Override
    protected void onDestroy() {
        mControlsFragment = null;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogHelper.d(TAG, "Activity onStop");

    }

    protected void showPlaybackControls() {
        LogHelper.d(TAG, "showPlaybackControls");
        if (NetworkHelper.isOnline(this)) {
            getFragmentManager().beginTransaction()
                .setCustomAnimations(
                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                    R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                .show(mControlsFragment)
                .commit();
            showingControls = true;
        }
    }

    protected void hidePlaybackControls() {
        LogHelper.d(TAG, "hidePlaybackControls");
        getFragmentManager().beginTransaction()
            .hide(mControlsFragment)
            .commit();
        showingControls = false;
    }

    @Override
    public void playingSong(Bundle bundle){
        super.playingSong(bundle);
        ListItem song;
        if(!bundle.containsKey(MusicService.SONG_INFO)) return;
        song = bundle.getParcelable(MusicService.SONG_INFO);
        setSongInfo(song);
        if(mControlsFragment != null){
            mControlsFragment.setPauseButton();
        }
        if(MusicServiceActivityVisible) {
            showPlaybackControls();
        }
    }

    @Override
    public void pausedSong(Bundle bundle){
        super.pausedSong(bundle);
        if(!MusicServiceActivityVisible)
            return;
        if(mControlsFragment != null){
            mControlsFragment.setPlayButton();
        }
    }

    private void setSongInfo(ListItem song){
        if(mControlsFragment != null){
            mControlsFragment.setSongInfo(song);
        }
    }

    @Override
    public void bufferingSong(){
        super.bufferingSong();
        if(mControlsFragment != null){
            mControlsFragment.buffering();
        }
    }

    @Override
    protected void finishingService(){
        super.finishingService();
        if(mControlsFragment != null){
            mControlsFragment.setPlayButton();
        }
        if(mControlsFragment.isVisible())
            hidePlaybackControls();
        else
            showingControls = false;
    }

}
