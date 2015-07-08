package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.GlideCircleTransform;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import butterknife.Bind;
import butterknife.ButterKnife;


public class FullScreenPlayerFragment extends DialogFragment {

    private static final String PREV_AVAILABLE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.fullscreenfragment.prevavailable";
    private static final String NEXT_AVAILABLE = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.fullscreenfragment.nextavailable";
    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerFragment.class);
    private ListItem song;

    @Bind(R.id.prev) ImageView mSkipPrev;
    @Bind(R.id.next) ImageView mSkipNext;
    @Bind(R.id.play_pause) ImageView mPlayPause;
    @Bind(R.id.seekBar1)
    SeekBar mSeekbar;
    @Bind(R.id.line1) TextView mLine1;
    @Bind(R.id.line2) TextView mLine2;
    @Bind(R.id.line3) TextView mLine3;
    @Bind(R.id.startText) TextView startText;
    @Bind(R.id.endText) TextView endText;
    @Bind(R.id.background_image) ImageView mBackgroundImage;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    @Bind(R.id.swipe_container)
    protected SwipeRefreshLayout refreshLayout;
    @Bind(R.id.toolbar_full_screen_player) Toolbar toolbarFullScreenPlayer;
    @Bind(R.id.toolbar_full_screen_back_arrow_on_back_image) ImageView backArrowOnFullScreen;
    @Bind(R.id.toolbar_full_screen_share_url) ImageView shareUrl;
    private boolean fragmentVisible;
    private boolean prevAvailable;
    private boolean nextAvailable;
    private boolean updateSeekbar = true;


    public FullScreenPlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save play controls state
        //savedInstanceState.putParcelableArrayList(LIST_SONGS, (ArrayList<ListItem>)((TrackListAdapter) trackList.getAdapter()).getTracks());
        if(song != null)
            savedInstanceState.putParcelable(MusicService.SONG_INFO, song);
        savedInstanceState.putBoolean(PREV_AVAILABLE, prevAvailable);
        savedInstanceState.putBoolean(NEXT_AVAILABLE, nextAvailable);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_player, container, false);
        ButterKnife.bind(this, view);

        if(mSkipPrev != null) mSkipPrev.setOnClickListener(prevListener);
        if(mSkipNext != null) mSkipNext.setOnClickListener(nextListener);
        if(mPlayPause != null) mPlayPause.setOnClickListener(playPauseListener);

        if(backArrowOnFullScreen != null) {
            //make arrow clickable
            backArrowOnFullScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((getActivity() instanceof ToolbarAndRefreshActivity) && ((ToolbarAndRefreshActivity) getActivity()).mIsLargeLayout){
                        getDialog().dismiss();
                    }else {
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        if(shareUrl != null) {
            //make arrow clickable
            shareUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent shareIntent = createShareUrlIntent();
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_with)));
                }
            });
        }

        mPauseDrawable = getActivity().getDrawable(R.drawable.ic_pause_white_48dp);
        mPlayDrawable = getActivity().getDrawable(R.drawable.ic_play_arrow_white_48dp);

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(MusicService.SONG_INFO)) {
                song = savedInstanceState.getParcelable(MusicService.SONG_INFO);
            }
            prevAvailable = savedInstanceState.getBoolean(PREV_AVAILABLE);
            nextAvailable = savedInstanceState.getBoolean(NEXT_AVAILABLE);
        }

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                startText.setText(Utilities.formatSongTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateSeekbar = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekbar = true;
                if(getActivity() instanceof MusicServiceActivity)
                    ((MusicServiceActivity)getActivity()).sendSeekToPosition(seekBar.getProgress() * 1000);
            }
        });

        loadComponents();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Utilities.setRefreshLayout(getActivity(), refreshLayout);
        fragmentVisible = true;

    }

    @Override
    public void onPause(){
        super.onPause();
        fragmentVisible = false;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        //to retain the fragment
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    public void setSong(ListItem song){
        this.song = song;
    }


    /**
     * load components for full player fragment
     */
    private void loadComponents() {

        mLine2.setText(song.getAlbumName());
        mLine1.setText(song.getTrackName());
        mLine3.setText(song.getArtistName());
        endText.setText(Utilities.formatSongTime(song.getDuration()));
        mSeekbar.setMax(song.getDuration());

        MusicServiceActivity activity;
        if(getActivity() instanceof MusicServiceActivity)  {
            activity = (MusicServiceActivity) getActivity();
            switch (activity.currentSongState) {
            case MediaControlsActivity.STATE_PAUSED:
                mPlayPause.setImageDrawable(mPlayDrawable);
                break;
            case MediaControlsActivity.STATE_PLAYING:
                mPlayPause.setImageDrawable(mPauseDrawable);
                break;
            default:
                break;
        }}
        if(nextAvailable)
            mSkipNext.setVisibility(View.VISIBLE);
        else
            mSkipNext.setVisibility(View.INVISIBLE);

        if(prevAvailable)
            mSkipPrev.setVisibility(View.VISIBLE);
        else
            mSkipPrev.setVisibility(View.INVISIBLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && addTransitionListener()) {
            // If we're running on Lollipop and we have added a listener to the shared element
            // transition, load the thumbnail. The listener will load the full-size image when
            // the transition is complete.
            loadThumbnail();
        } else {
            // If all other cases we should just load the full-size image now
            loadFullSizeImage();
        }
    }

    /**
     * Load the item's thumbnail image into our {@link ImageView}.
     */
    private void loadThumbnail() {
        if(mBackgroundImage != null) {
            Glide.with(getActivity())
                    .load(song.getThumbnailSmall())
                    .error(R.drawable.default_image)
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(mBackgroundImage);
        }
    }

    /**
     * Load the item's full-size image into our {@link ImageView}.
     */
    private void loadFullSizeImage() {

        if(mBackgroundImage != null) {
            Glide.with(getActivity())
                    .load(song.getThumbnailLarge())
                    .error(R.drawable.fullscreen_default)
                    .into(mBackgroundImage);
        }
    }

    /**
     * Try and add a {@link Transition.TransitionListener} to the entering shared element
     * {@link Transition}. We do this so that we can load the full-size image after the transition
     * has completed.
     *
     * @return true if we were successful in adding a listener to the enter transition
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {

        //final Transition transition = getActivity().getWindow().getSharedElementEnterTransition();
        final Transition transition = getSharedElementEnterTransition();

        if (transition != null) {
            // There is an entering shared element transition so add a listener to it
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    // As the transition has ended, we can now load the full-size image
                    loadFullSizeImage();

                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionStart(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");

                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    // Make sure we remove ourselves as a listener
                    LogHelper.d(TAG, "onTransitionStart");
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    LogHelper.d(TAG, "onTransitionStart");
                }
            });
            return true;
        }

        // If we reach here then we have not added a listener
        return false;
    }


    public void setPlayingSong(ListItem song, boolean prevAvailable, boolean nextAvailable){
        this.song = song;
        this.prevAvailable = prevAvailable;
        this.nextAvailable = nextAvailable;

        if(fragmentVisible)
            loadComponents();
    }

    public void setPausedSong(){
        if(fragmentVisible)
            mPlayPause.setImageDrawable(mPlayDrawable);
    }

    public void setFinishedPlayingSong() {
        if(fragmentVisible) {
            mPlayPause.setImageDrawable(mPlayDrawable);
            mSeekbar.setProgress(0);
        }
    }

    public void setSeekbarPosition(long miliseconds){
        if(fragmentVisible && updateSeekbar) {
            mSeekbar.setProgress((int) miliseconds / 1000);
            mSeekbar.refreshDrawableState();
        }
    }

    private Intent createShareUrlIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/html");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, song.getPreviewUrl());
        return shareIntent;
    }

    View.OnClickListener nextListener = new View.OnClickListener() {
        public void onClick(View v) {
            LogHelper.d(TAG, "next clicked");
            if(getActivity() instanceof MusicServiceActivity)
                ((MusicServiceActivity) getActivity()).sendSkipToNextMessageToService();
        }
    };
    View.OnClickListener prevListener = new View.OnClickListener() {
        public void onClick(View v) {
            LogHelper.d(TAG, "prev clicked");
            if(getActivity() instanceof MusicServiceActivity)
                ((MusicServiceActivity) getActivity()).sendSkipToPrevMessageToService();
        }
    };

    View.OnClickListener playPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            MusicServiceActivity activity = null;
            if(getActivity() instanceof MusicServiceActivity)
                activity = (MusicServiceActivity) getActivity();
            else{
                return;
            }
            switch (activity.currentSongState) {
                case MusicServiceActivity.STATE_STOPPED:
                case MusicServiceActivity.STATE_PAUSED:
                    activity.sendResumeMessageToService();
                    break;
                case MusicServiceActivity.STATE_PLAYING:
                    activity.sendPauseMessageToService();
                    break;
                default:
                    break;
            }
        }
    };




}
