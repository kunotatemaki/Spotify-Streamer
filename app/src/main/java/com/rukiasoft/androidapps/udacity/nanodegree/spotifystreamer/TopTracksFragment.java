package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.GlideCircleTransform;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.LogHelper;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracksFragment extends Fragment {

    private static final String LIST_SONGS = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.toptracksfragment.songlist";
    private static final String ARTIST_ITEM = "com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.toptracksfragment.artistitem";
    private static final String TAG = LogHelper.makeLogTag(TopTracksFragment.class);
    private TrackListAdapter tracksListAdapter;
    private SpotifyService spotify;
    private ListItem artist;

    @Bind(R.id.toolbar_top_track_list) Toolbar toolbarTopTrackList;
    @Bind(R.id.toolbar_back_image)
    RelativeLayout toolbarBackImage;
    @Bind(R.id.toolbar_subtitle) TextView toolbarSubtitle;
    @Bind(R.id.artist_item_image) ImageView artistItemImage;
    @Bind(R.id.tracks_list) RecyclerView trackList;
    @Bind(R.id.swipe_container)
    protected SwipeRefreshLayout refreshLayout;
    private Boolean loaded;

    public interface TopTracksFragmentSelectionListener {
        void onTopTracksFragmentItemSelected(ListItem item, Integer position, List<View> sharedElements);
    }

    private TopTracksFragmentSelectionListener mCallback;
    
    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save play controls state
        savedInstanceState.putParcelableArrayList(LIST_SONGS, (ArrayList<ListItem>)((TrackListAdapter) trackList.getAdapter()).getTracks());
        savedInstanceState.putParcelable(ARTIST_ITEM, artist);
        super.onSaveInstanceState(savedInstanceState);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_tracks_list, container, false);
        ButterKnife.bind(this, view);

        List<ListItem> songs = new ArrayList<>();
        if(savedInstanceState != null
                &&savedInstanceState.containsKey(ARTIST_ITEM)
                && savedInstanceState.containsKey(LIST_SONGS)) {
            songs = savedInstanceState.getParcelableArrayList(LIST_SONGS);
            artist = savedInstanceState.getParcelable(ARTIST_ITEM);
            loaded = true;
        }else{
            loaded = false;
        }

        if(null != toolbarTopTrackList) {
            if(getActivity() instanceof ToolbarAndRefreshActivity){
                ((ToolbarAndRefreshActivity) getActivity()).setToolbarInActivity(toolbarTopTrackList, false, false, false);
            }
            if(toolbarBackImage != null) {
                //make arroy+image clickable (as Whatsapp do)
                toolbarBackImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
            }

            loadArtistInformationForToolbar();
        }


        trackList.setHasFixedSize(true);
        if(tracksListAdapter == null) {
            tracksListAdapter = new TrackListAdapter();
        }
        trackList.setAdapter(tracksListAdapter);
        trackList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        trackList.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


        tracksListAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = trackList.getChildAdapterPosition(v);
                ListItem item = tracksListAdapter.getItem(position);
                List<View> sharedViews = new ArrayList<>();
                sharedViews.add(v.findViewById(R.id.track_item_image));
                sharedViews.add(v.findViewById(R.id.track_item_song));
                sharedViews.add(v.findViewById(R.id.track_item_album));
                sharedViews.add(toolbarTopTrackList.findViewById(R.id.toolbar_subtitle));
                mCallback.onTopTracksFragmentItemSelected(item, position, sharedViews);


            }
        });


        if(getActivity() instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) getActivity()).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) getActivity()).disableRefreshLayoutSwipe();
        }

        if(loaded) {
            setTopTracks(songs);
        }

        return view;
    }

    /**
     * save the list of tracks returned by the search into a local List
     * @param tracks list of tracks
     */
    public void setTopTracks(List<ListItem> tracks){

        if(tracksListAdapter != null)
            tracksListAdapter.setItems(tracks);
        if(isVisible()) {
            //go to first position
            if (trackList != null && trackList.getLayoutManager() != null)
                trackList.getLayoutManager().scrollToPosition(0);
        }
        //set tracks in service
        if(getActivity() instanceof MusicServiceActivity)
            ((MusicServiceActivity) getActivity()).sendSetSongListMessageToService(tracks, artist.getArtistId());
        loaded = true;
    }

    public void sendTopTracksToService(){
        if(getActivity() instanceof MusicServiceActivity)
            ((MusicServiceActivity) getActivity()).sendSetSongListMessageToService(((TrackListAdapter)trackList.getAdapter()).getTracks(), artist.getArtistId());
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!loaded && artist != null){
            searchTopTracks(artist);
        }
        if(trackList.getAdapter() != null)
            trackList.getAdapter().notifyDataSetChanged();

    }

    public void setArtist(ListItem artist){
        this.artist = artist;
        if(getActivity() instanceof SearchActivity && ((SearchActivity) getActivity()).mIsLargeLayout){
            searchTopTracks(this.artist);
        }
    }

    /**
     * Search for an artist's top tracks using Spotify's wrapper
     * @param artist spotify artist info
     */
    private void searchTopTracks(final ListItem artist){
        Map<String, Object> map = new HashMap<>();
        String zip = Utilities.getZipFromPreferences(getActivity());
        map.put("country", zip);

        //show indefiniteProgressBar
        if(getActivity() instanceof ToolbarAndRefreshActivity)
            ((ToolbarAndRefreshActivity) getActivity()).showRefreshLayoutSwipeProgress();

        spotify.getArtistTopTrack(artist.getArtistId(), map, new Callback<Tracks>() {

            @Override
            public void success(Tracks tracks, Response response) {
                final List<ListItem> trackItems = new ArrayList<>();
                for (int i = 0; i < tracks.tracks.size(); i++) {
                    ListItem item = new ListItem();
                    item.setArtistName(artist.getArtistName());
                    item.setArtistId(artist.getArtistId());
                    item.setTrackName(tracks.tracks.get(i).name);
                    item.setAlbumName(tracks.tracks.get(i).album.name);
                    item.setPreviewUrl(tracks.tracks.get(i).preview_url);
                    //in this case, instead of duration, we use 30s (preview) => 30000ms
                    //item.setDuration(tracks.tracks.get(i).duration_ms);
                    //item.setDuration(30000);
                    for (int j = 0; j < tracks.tracks.get(i).album.images.size(); j++) {
                        if (j == 0) {
                            item.setThumbnailLarge(tracks.tracks.get(i).album.images.get(j).url);
                            item.setThumbnailSmall(tracks.tracks.get(i).album.images.get(j).url);
                        } else if (tracks.tracks.get(i).album.images.get(j).width == 200)
                            item.setThumbnailSmall(tracks.tracks.get(i).album.images.get(j).url);
                        else if (tracks.tracks.get(i).album.images.get(j).width == 640)
                            item.setThumbnailLarge(tracks.tracks.get(i).album.images.get(j).url);
                    }
                    trackItems.add(item);
                }
                Handler mainHandler = new Handler(getActivity().getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //hide indefiniteProgressBar
                        if(getActivity() instanceof ToolbarAndRefreshActivity)
                            ((ToolbarAndRefreshActivity) getActivity()).hideRefreshLayoutSwipeProgress();
                        setTopTracks(trackItems);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO - different messages for different type of errors??
                Handler mainHandler = new Handler(getActivity().getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //hide indefiniteProgressBar
                        if(getActivity() instanceof ToolbarAndRefreshActivity)
                            ((ToolbarAndRefreshActivity) getActivity()).hideRefreshLayoutSwipeProgress();
                        Utilities.showToast(getActivity(), getResources().getString(R.string.no_response_zips));
                    }
                });

            }
        });
    }

    private void loadArtistInformationForToolbar() {
        if(artist == null)  return;
        if(toolbarSubtitle != null)
            toolbarSubtitle.setText(artist.getArtistName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*&& addTransitionListener*/) {
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
        if(artistItemImage != null) {
            Glide.with(getActivity())
                    .load(artist.getArtistPicture())
                    .error(R.drawable.default_image)
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(artistItemImage);
        }
    }

    /**
     * Load the item's full-size image into our {@link ImageView}.
     */
    private void loadFullSizeImage() {

        if(artistItemImage != null) {
            Glide.with(getActivity())
                    .load(artist.getArtistPicture())
                    .error(R.drawable.default_image)
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(artistItemImage);
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

        try {
            mCallback = (TopTracksFragmentSelectionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement SelectionListener");
        }

    }

    public List<ListItem> getItems(){
        return tracksListAdapter.getTracks();
    }

    public void setPlayingSong(Bundle bundle){
        int currentSong = bundle.getInt(MusicService.SONG_POSITION);
        String id = "";
        if(bundle.containsKey(MusicService.SONG_INFO)) {
            ListItem song = bundle.getParcelable(MusicService.SONG_INFO);
            if(song != null) id = song.getArtistId();
        }
        ((TrackListAdapter) trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_PLAYING, isVisible() & id.equals(artist.getArtistId()));
    }

    public void setPausedSong(Bundle bundle){
        int currentSong = bundle.getInt(MusicService.SONG_POSITION);
        String id = "";
        if(bundle.containsKey(MusicService.ARTIST_ID))
            id = bundle.getString(MusicService.ARTIST_ID);
        ((TrackListAdapter)trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_PAUSED, isVisible() & id.equals(artist.getArtistId()));
    }

    public void setFinishedPlayingSong(Bundle bundle) {
        int currentSong = bundle.getInt(MusicService.SONG_POSITION);
        String id = "";
        if(bundle.containsKey(MusicService.ARTIST_ID))
            id = bundle.getString(MusicService.ARTIST_ID);
        ((TrackListAdapter)trackList.getAdapter()).setItemState(currentSong, ListItem.FLAG_STOPPED, isVisible() & id.equals(artist.getArtistId()));
    }
}
