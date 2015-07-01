package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.GlideCircleTransform;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class FullScreenPlayerFragment extends Fragment {

    //private static final String LIST_SONGS = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.toptracksfragment.songlist";
    //private static final String ARTIST_ITEM = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.toptracksfragment.artistitem";
    private static final String TAG = LogHelper.makeLogTag(FullScreenPlayerFragment.class);
    private ListItem song;

    @InjectView(R.id.prev) ImageView mSkipPrev;
    @InjectView(R.id.next) ImageView mSkipNext;
    @InjectView(R.id.play_pause) ImageView mPlayPause;
    @InjectView(R.id.startText) TextView mStart;
    @InjectView(R.id.endText) TextView mEnd;
    @InjectView(R.id.seekBar1)
    SeekBar mSeekbar;
    @InjectView(R.id.line1) TextView mLine1;
    @InjectView(R.id.line2) TextView mLine2;
    @InjectView(R.id.line3) TextView mLine3;
    @InjectView(R.id.controllers) View mControllers;
    @InjectView(R.id.background_image) ImageView mBackgroundImage;
    @InjectView(R.id.toolbar_full_screen_player) Toolbar toolbarFullPlayer;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    @InjectView(R.id.swipe_container)
    protected SwipeRefreshLayout refreshLayout;
    private Boolean loaded;



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
        //savedInstanceState.putParcelable(ARTIST_ITEM, artist);
        super.onSaveInstanceState(savedInstanceState);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_player, container, false);
        ButterKnife.inject(this, view);

        if(null != toolbarFullPlayer) {
            if(getActivity() instanceof SearchActivity){
                ((ToolbarAndRefreshActivity) getActivity()).setToolbarInActivity(toolbarFullPlayer, true, true, true);
            }
        }

        if(getActivity() instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) getActivity()).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) getActivity()).disableRefreshLayoutSwipe();
        }

        mPauseDrawable = getActivity().getDrawable(R.drawable.ic_pause_white_48dp);
        mPlayDrawable = getActivity().getDrawable(R.drawable.ic_play_arrow_white_48dp);

        /*if(savedInstanceState != null
                &&savedInstanceState.containsKey(ARTIST_ITEM)
                && savedInstanceState.containsKey(LIST_SONGS)) {
            List<ListItem> songs = savedInstanceState.getParcelableArrayList(LIST_SONGS);
            ListItem artist = savedInstanceState.getParcelable(ARTIST_ITEM);
            setTopTracks(songs, artist.getArtistId());
            loaded = true;
        }else{
            loaded = false;
        }*/

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //TODO formatear el texto
                mStart.setText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //seekTo(seekBar.getProgress());
            }
        });

        loadComponents();

        return view;
    }


    @Override
    public void onResume(){
        super.onResume();


    }

    public void setSong(ListItem song){
        this.song = song;
    }




    private void loadComponents() {

        mLine2.setText(song.getAlbumName());
        mLine1.setText(song.getTrackName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && addTransitionListener()) {
            // If we're running on Lollipop and we have added a listener to the shared element
            // transition, load the thumbnail. The listener will load the full-size image when
            // the transition is complete.
            //loadThumbnail();
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
                    .error(R.drawable.default_image)
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

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        /*try {
            mCallback = (TopTracksFragmentSelectionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement SelectionListener");
        }*/

    }


    public void setPlayingSong(int currentSong){
        //((TrackListAdapter)trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_PLAYING);
    }

    public void setPausedSong(int currentSong){
        //((TrackListAdapter)trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_PAUSED);
    }

    public void setFinishedPlayingSong(int currentSong) {
        //((TrackListAdapter)trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_STOPPED);
    }
}
