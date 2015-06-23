package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchArtistActivityFragment extends Fragment {

    private RecyclerView recView;
    ArtistListAdapter artistListAdapter;
    private Toolbar toolbar;
    private SearchView searchView;
    SpotifyService spotify;
    private static int TOP_TRACK_REQUEST = 153;

    public SearchArtistActivityFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_artist, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(null != toolbar) {
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

            if(((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void setArtists(List<ArtistItem> artists){

        artistListAdapter.setItems(artists);
        //go to first position
        if(recView != null && recView.getLayoutManager() != null)
            recView.getLayoutManager().scrollToPosition(0);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_artist_fragment, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(false); // Do not iconify the widget; expand it at the first time
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryRefinementEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_delete_recent_searches:
                if(getActivity() instanceof SearchArtistActivity)
                    ((SearchArtistActivity)getActivity()).cleanRecentSearches();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Boolean isSearchViewCollapsed() {
        return searchView.isIconified();
    }

    public void setSearchViewCollapsed(Boolean collapsed){
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconified(collapsed);
    }

    public void searchArtist(String query){
        //search artist with spotify wrapper
        String parsedQuery = query.replace(" ", "+");
        spotify.searchArtists(parsedQuery, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<ArtistItem> artists = new ArrayList<>();
                for(int i=0; i<artistsPager.artists.items.size(); i++){
                    ArtistItem item = new ArtistItem();
                    item.setArtistId(artistsPager.artists.items.get(i).id);
                    item.setArtistName(artistsPager.artists.items.get(i).name);
                    for(int j=0; j<artistsPager.artists.items.get(i).images.size(); j++){
                        if(j == 0)
                            item.setArtistPicture(artistsPager.artists.items.get(i).images.get(j).url);
                        else
                        if(artistsPager.artists.items.get(i).images.get(j).width == 200){
                            item.setArtistPicture(artistsPager.artists.items.get(i).images.get(j).url);
                            break;
                        }
                    }
                    artists.add(item);
                }
                Handler mainHandler = new Handler(getActivity().getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        setArtists(artists);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO - ver el tipo de error??
                Utilities.showToast(getActivity(), getResources().getString(R.string.no_artist_found));
            }
        });
    }


}
