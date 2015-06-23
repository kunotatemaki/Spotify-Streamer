package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TopTracksFragment extends Fragment {

    private RecyclerView recView;
    private TrackListAdapter tracksListAdapter;
    private SpotifyService spotify;
    private ArtistListItem artist;

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
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            RelativeLayout linearLayout = (RelativeLayout) toolbar.findViewById(R.id.toolbar_back_image);
            if(linearLayout != null) {
                //make arroy+image clickable (as Whatsapp do)
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
            }
            //Set artist image on toolbar
            ImageView imageView = (ImageView) toolbar.findViewById(R.id.artist_item_image);
            if(imageView != null) {
                Glide.with(getActivity())
                        .load(artist.getArtistPicture())
                        .error(R.mipmap.ic_launcher)
                        .transform(new GlideCircleTransform(getActivity()))
                        .into(imageView);
            }
            TextView subtitle = (TextView) toolbar.findViewById(R.id.toolbar_subtitle);
            if(subtitle != null)
                subtitle.setText(artist.getArtistName());
        }


        recView = (RecyclerView) view.findViewById(R.id.tracks_list);
        recView.setHasFixedSize(true);
        if(tracksListAdapter == null) {
            tracksListAdapter = new TrackListAdapter();
        }
        recView.setAdapter(tracksListAdapter);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        recView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));


        tracksListAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recView.getChildAdapterPosition(v);
                //TODO show media player in STAGE 2

            }
        });
        searchTopTracks(artist.getArtistId());
        return view;
    }

    /**
     * save the list of tracks returned by the search into a local List
     * @param tracks
     */
    private void setTopTracks(List<TrackItem> tracks){

        tracksListAdapter.setItems(tracks);
        //go to first position
        if(recView != null && recView.getLayoutManager() != null)
            recView.getLayoutManager().scrollToPosition(0);

    }

    public void setArtist(ArtistListItem artist){
        this.artist = artist;
    }

    /**
     * Search for an artist's top tracks using Spotify's wrapper
     * @param id
     */
    private void searchTopTracks(String id){
        Map<String, Object> map = new HashMap<>();
        map.put("country", Locale.getDefault().getCountry());
        spotify.getArtistTopTrack(id, map, new Callback<Tracks>() {

            @Override
            public void success(Tracks tracks, Response response) {
                final List<TrackItem> trackItems = new ArrayList<>();
                for (int i = 0; i < tracks.tracks.size(); i++) {
                    TrackItem item = new TrackItem();
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
                        setTopTracks(trackItems);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO - different messages for different type of errors??
                Utilities.showToast(getActivity(), getResources().getString(R.string.no_top_tracks_found));
            }
        });
    }

}
