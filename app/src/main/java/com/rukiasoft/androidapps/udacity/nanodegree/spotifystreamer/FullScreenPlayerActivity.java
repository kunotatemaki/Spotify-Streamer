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

import android.app.FragmentManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.view.View;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import butterknife.ButterKnife;

/**
 * A full screen player that shows the current playing music with a background image
 * depicting the album art. The activity also has controls to seek/pause/play the audio.
 */
public class FullScreenPlayerActivity extends MusicServiceActivity {
    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerActivity.class);

    private final ScheduledExecutorService mExecutorService =
        Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackState mLastPlaybackState;

    private FullScreenPlayerFragment fullScreenPlayerFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        ButterKnife.inject(this);

        FragmentManager fm = getFragmentManager();
        fullScreenPlayerFragment = (FullScreenPlayerFragment) fm.findFragmentByTag(FullScreenPlayerFragment.class.getSimpleName());

        // create the fragment and data the first time
        if (fullScreenPlayerFragment == null) {
            // add the fragment
            fullScreenPlayerFragment = new FullScreenPlayerFragment();
            fm.beginTransaction().add(R.id.activity_full_player_container, fullScreenPlayerFragment, FullScreenPlayerFragment.class.getSimpleName()).commit();
            fm.executePendingTransactions();
        }
        //get artist item
        if(!getIntent().hasExtra(MusicService.SONG_INFO)){
            //no artist item. Finish Activity
            setResult(RESULT_CANCELED);
            finish();
        }
        ListItem song = getIntent().getExtras().getParcelable(MusicService.SONG_INFO);
        fullScreenPlayerFragment.setSong(song);

    }

    @Override
    protected void pausedSong(Bundle bundle){
        super.pausedSong(bundle);
        if(fullScreenPlayerFragment != null)
            fullScreenPlayerFragment.setPausedSong();
    }

    @Override
    protected void playingSong(Bundle bundle){
        super.playingSong(bundle);
        if(fullScreenPlayerFragment != null && bundle.containsKey(MusicService.SONG_INFO)){
            ListItem song = bundle.getParcelable(MusicService.SONG_INFO);
            boolean prevAvailable = true;
            boolean nextAvailable = true;
            if(bundle.containsKey(MusicService.FIRST_SONG)) prevAvailable = false;
            if(bundle.containsKey(MusicService.LAST_SONG)) nextAvailable = false;
            fullScreenPlayerFragment.setPlayingSong(song, prevAvailable, nextAvailable);
        }
    }

    @Override
    protected void finishedPlayingSong(Bundle bundle){
        super.finishedPlayingSong(bundle);
        if(fullScreenPlayerFragment != null)
            fullScreenPlayerFragment.setFinishedPlayingSong();
    }

    @Override
    protected void seekBarPositionReceived(int mseconds){
        super.seekBarPositionReceived(mseconds);
        if(fullScreenPlayerFragment != null)
            fullScreenPlayerFragment.setSeekbarPosition(mseconds);
    }

    public void onPlayPauseClicked(View v){
        LogHelper.d(TAG, "playpause clicked");
        switch (currentSongState) {
            case MediaControlsActivity.STATE_PAUSED:
                sendResumeMessageToService();
                break;
            case MediaControlsActivity.STATE_PLAYING:
                sendPauseMessageToService();
                break;
            default:
                break;
        }
    }

    public void onPrevClicked(View v){
        LogHelper.d(TAG, "prev clicked");
        sendSkipToPrevMessageToService();
    }

    public void onNextClicked(View v){
        LogHelper.d(TAG, "prev clicked");
        sendSkipToNextMessageToService();
    }

}
