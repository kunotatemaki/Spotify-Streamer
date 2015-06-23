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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TooManyListenersException;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private RecyclerView recView;
    TrackListAdapter tracksListAdapter;
    private Toolbar toolbar;
    SpotifyService spotify;
    ArtistItem artist;

    public TopTracksActivityFragment() {
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
        View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        TextView subtitle = (TextView)toolbar.findViewById(R.id.toolbar_subtitle);
        subtitle.setText(artist.getArtistName());

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

    public void setTopTracks(List<TrackItem> tracks){

        tracksListAdapter.setItems(tracks);
        //go to first position
        if(recView != null && recView.getLayoutManager() != null)
            recView.getLayoutManager().scrollToPosition(0);

    }

    public void setArtist(ArtistItem artist){
        this.artist = artist;
    }

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
                //TODO - ver el tipo de error??
                Utilities.showToast(getActivity(), getResources().getString(R.string.no_top_tracks_found));
            }
        });
    }



}