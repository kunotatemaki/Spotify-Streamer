package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistListFragment extends Fragment {

    private RecyclerView recView;
    private ArtistListAdapter artistListAdapter;
    private SpotifyService spotify;
    private ArtistListSearchClickListener mListener;
    private static final int TOP_TRACK_REQUEST = 153;

    public ArtistListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ArtistListSearchClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists_list, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            ImageButton imageButton = (ImageButton) toolbar.findViewById(R.id.toolbar_button);
            if(imageButton != null) {
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onSearchClick();
                    }
                });
            }
        }

        recView = (RecyclerView) view.findViewById(R.id.artist_list);

        if(artistListAdapter == null) {
            artistListAdapter = new ArtistListAdapter();
        }
        recView.setAdapter(artistListAdapter);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        recView.addItemDecoration(
                new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));



        artistListAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start top track list activity
                int position = recView.getChildAdapterPosition(v);
                Intent topTracksIntent = new Intent(getActivity(), TopTracksActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("artist_item", artistListAdapter.getItem(position));
                topTracksIntent.putExtras(bundle);
                startActivityForResult(topTracksIntent, TOP_TRACK_REQUEST);
            }
        });

        return view;
    }

    /**
     * save the list of artists returned by the search into a local List
     * @param artists
     */
    private void setArtists(List<ArtistListItem> artists){

        artistListAdapter.setItems(artists);
        //go to first position
        if(recView != null && recView.getLayoutManager() != null)
            recView.getLayoutManager().scrollToPosition(0);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_artist_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_delete_recent_searches:
                if(getActivity() instanceof ArtistSearchActivity)
                    ((ArtistSearchActivity)getActivity()).cleanRecentSearches();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Search for an artist using Spotify's wrapper
     * @param query
     */
    public void searchArtist(String query){
        //search artist with spotify wrapper
        String parsedQuery = query.replace(" ", "+");
        spotify.searchArtists(parsedQuery, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<ArtistListItem> artists = new ArrayList<>();
                Handler mainHandler = new Handler(getActivity().getMainLooper());
                if(artistsPager.artists.items.size() == 0){
                    //no artists, notify it!
                    mainHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            Utilities.showToast(getActivity(), getResources().getString(R.string.no_artist_found));
                        }
                    });
                    return;
                }
                for(int i=0; i<artistsPager.artists.items.size(); i++){
                    ArtistListItem item = new ArtistListItem();
                    item.setArtistId(artistsPager.artists.items.get(i).id);
                    item.setArtistName(artistsPager.artists.items.get(i).name);
                    for(int j=0; j<artistsPager.artists.items.get(i).images.size(); j++){
                        //save 200px picture (or any other if not available)
                        if(j == 0)  item.setArtistPicture(artistsPager.artists.items.get(i).images.get(j).url);
                        else if (artistsPager.artists.items.get(i).images.get(j).width == 200) {
                            item.setArtistPicture(artistsPager.artists.items.get(i).images.get(j).url);
                            break;
                        }
                    }
                    artists.add(item);
                }
                //Run on main thread
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        setArtists(artists);
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
                        Utilities.showToast(getActivity(), getResources().getString(R.string.no_artist_found));
                    }
                });
            }
        });
    }


    public interface ArtistListSearchClickListener {
        void onSearchClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == TOP_TRACK_REQUEST) {
            // check if something went wrong
            if (resultCode == Activity.RESULT_CANCELED) {
                Utilities.showToast(getActivity(), getResources().getString(R.string.error_passing_artist));
            }
        }
    }

}
