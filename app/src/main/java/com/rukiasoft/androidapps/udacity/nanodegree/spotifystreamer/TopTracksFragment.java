package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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


public class TopTracksFragment extends RefreshFragment {

    private TrackListAdapter tracksListAdapter;
    private SpotifyService spotify;
    private ListItem artist;

    @InjectView(R.id.toolbar_top_track_list) Toolbar toolbar_top_track_list;
    @InjectView(R.id.toolbar_back_image)
    RelativeLayout toolbarBAckImage;
    @InjectView(R.id.toolbar_subtitle) TextView toolbarSubtitle;
    @InjectView(R.id.artist_item_image) ImageView artistItemImage;
    @InjectView(R.id.tracks_list) RecyclerView trackList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_tracks_list, container, false);
        ButterKnife.inject(this, view);

        if(null != toolbar_top_track_list) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar_top_track_list);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            if(toolbarBAckImage != null) {
                //make arroy+image clickable (as Whatsapp do)
                toolbarBAckImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
            }

            loadArtistInformationForToolbar();
            /*//Set artist image on toolbar
            if(artistItemImage != null) {
                Glide.with(getActivity())
                        .load(artist.getArtistPicture())
                        .error(R.drawable.default_image)
                        .transform(new GlideCircleTransform(getActivity()))
                        .into(artistItemImage);
            }
            if(toolbarSubtitle != null)
                toolbarSubtitle.setText(artist.getArtistName());*/
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
                //TODO show media player in STAGE 2

            }
        });
        searchTopTracks(artist.getArtistId());

        /*//configure swipeRefreshLayout
        Utilities.setRefreshLayoutColorScheme(refreshLayoutTopTracksListFragment, getResources().getColor(R.color.color_scheme_1_1),
                getResources().getColor(R.color.color_scheme_1_2),
                getResources().getColor(R.color.color_scheme_1_3),
                getResources().getColor(R.color.color_scheme_1_4));
        Utilities.disableRefreshLayoutSwipe(refreshLayoutTopTracksListFragment);*/
        disableRefreshLayoutSwipe();
        return view;
    }

    /**
     * save the list of tracks returned by the search into a local List
     * @param tracks list of tracks
     */
    private void setTopTracks(List<ListItem> tracks){

        tracksListAdapter.setItems(tracks);
        //go to first position
        if(trackList != null && trackList.getLayoutManager() != null)
            trackList.getLayoutManager().scrollToPosition(0);

    }

    public void setArtist(ListItem artist){
        this.artist = artist;
    }

    /**
     * Search for an artist's top tracks using Spotify's wrapper
     * @param id spotify artist's id
     */
    private void searchTopTracks(String id){
        Map<String, Object> map = new HashMap<>();
        map.put("country", Locale.getDefault().getCountry());

        //show indefiniteProgressBar
        showRefreshLayoutSwipeProgress();

        spotify.getArtistTopTrack(id, map, new Callback<Tracks>() {

            @Override
            public void success(Tracks tracks, Response response) {
                final List<ListItem> trackItems = new ArrayList<>();
                for (int i = 0; i < tracks.tracks.size(); i++) {
                    ListItem item = new ListItem(ListItem.FLAG_PLAYABLE);
                    item.setTrackName(tracks.tracks.get(i).name);
                    item.setAlbumName(tracks.tracks.get(i).album.name);
                    item.setPreviewUrl(tracks.tracks.get(i).preview_url);
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
                        hideRefreshLayoutSwipeProgress();
                        setTopTracks(trackItems);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO - different messages for different type of errors??
                //hide indefiniteProgressBar
                hideRefreshLayoutSwipeProgress();
                Utilities.showToast(getActivity(), getResources().getString(R.string.no_response));
            }
        });
    }

    private void loadArtistInformationForToolbar() {
        if(toolbarSubtitle != null)
            toolbarSubtitle.setText(artist.getArtistName());

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
        final Transition transition = getActivity().getWindow().getSharedElementEnterTransition();

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
                    // No-op
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this);
                }

                @Override
                public void onTransitionPause(Transition transition) {
                    // No-op
                }

                @Override
                public void onTransitionResume(Transition transition) {
                    // No-op
                }
            });
            return true;
        }

        // If we reach here then we have not added a listener
        return false;
    }
}
