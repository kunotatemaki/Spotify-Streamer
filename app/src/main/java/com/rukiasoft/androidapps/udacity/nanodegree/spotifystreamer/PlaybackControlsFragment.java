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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A class that shows the Media Queue to the user.
 */
public class PlaybackControlsFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(PlaybackControlsFragment.class);
    private static final String SONG_ITEM = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.playbackcontrolsfragment.songitem";
    private static final String BUTTON_STATE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.playbackcontrolsfragment.buttonstate";

    @InjectView(R.id.play_pause_play_controls) ImageButton mPlayPause;
    @InjectView(R.id.song_play_controls) TextView mTitle;
    @InjectView(R.id.album_play_controls) TextView mSubtitle;
    @InjectView(R.id.album_art_play_controls) ImageView mAlbumArt;
    ListItem mSong;
    Integer mPlayPauseResource = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            if (savedInstanceState.containsKey(SONG_ITEM))
                mSong = savedInstanceState.getParcelable(SONG_ITEM);
            if (savedInstanceState.containsKey(BUTTON_STATE))
                mPlayPauseResource = savedInstanceState.getInt(BUTTON_STATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save play controls state
        savedInstanceState.putParcelable(SONG_ITEM, mSong);
        if(mPlayPauseResource != null)
            savedInstanceState.putInt(BUTTON_STATE, mPlayPauseResource);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        ButterKnife.inject(this, rootView);
        mPlayPause.setEnabled(true);
        mPlayPause.setOnClickListener(mButtonListener);
        if(mPlayPauseResource == null) {
            setPlayButton();
        }else{
            mPlayPause.setImageDrawable(
                    getActivity().getDrawable(mPlayPauseResource));
        }

        if(mSong != null)
            showSongInfo();

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO lanzar la otra activity
                /*Intent intent = new Intent(getActivity(), FullScreenPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                MediaMetadata metadata = getActivity().getMediaController().getMetadata();
                if (metadata != null) {
                    intent.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION,
                        metadata.getDescription());
                }
                startActivity(intent);*/
            }
        });
        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mSong != null)
            showSongInfo();
    }
    @Override
    public void onStart() {
        super.onStart();
        LogHelper.d(TAG, "fragment.onStart");

    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.d(TAG, "fragment.onStop");

    }

    public void setPlayButton(){
        mPlayPauseResource = R.drawable.ic_play_arrow_black_36dp;
        mPlayPause.setImageDrawable(
                getActivity().getDrawable(mPlayPauseResource));
    }

    public void setPauseButton(){
        mPlayPauseResource = R.drawable.ic_pause_black_36dp;
        mPlayPause.setImageDrawable(
                getActivity().getDrawable(mPlayPauseResource));
    }

    public void setSongInfo(ListItem song, Boolean updateView){
        mSong = song;
        if(updateView) showSongInfo();
    }

    public void buffering(){
        mTitle.setText(getResources().getString(R.string.buffering));
        mSubtitle.setText("");
        Glide.with(getActivity())
                .load(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(mAlbumArt);
    }

    private void showSongInfo(){
        mTitle.setText(mSong.getTrackName());
        mSubtitle.setText(mSong.getAlbumName());
        Glide.with(getActivity())
                .load(mSong.getThumbnailSmall())
                .error(R.drawable.default_image)
                .into(mAlbumArt);
    }

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MusicServiceActivity activity;
            if(getActivity() instanceof MusicServiceActivity) activity = (MusicServiceActivity) getActivity();
            else    return;
            switch (activity.currentSongState) {
                case MediaControlsActivity.STATE_PAUSED:
                    activity.sendResumeMessageToService();
                    break;
                case MediaControlsActivity.STATE_PLAYING:
                    activity.sendPauseMessageToService();
                    break;
                default:
                    break;
            }
        }
    };


}
