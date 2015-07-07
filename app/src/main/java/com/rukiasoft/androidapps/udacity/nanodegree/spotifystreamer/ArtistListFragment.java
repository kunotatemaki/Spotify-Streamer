package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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

    private ArtistListAdapter artistListAdapter;
    private SpotifyService spotify;
    private ArtistListSearchClickListener mListener;
    private static final int TOP_TRACK_REQUEST = 153;

    @Bind(R.id.toolbar_artist_list) Toolbar toolbarArtistList;
    @Bind(R.id.artist_list) RecyclerView recView;
    @Bind(R.id.swipe_container)
    protected SwipeRefreshLayout refreshLayout;

    public ArtistListFragment() {
    }

    public interface ArtistListFragmentSelectionListener {
        void onArtistListFragmentItemSelected(ListItem item, List<View> sharedElements);
    }

    private ArtistListFragmentSelectionListener mCallback;

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
        ButterKnife.bind(this, view);
        if(null != toolbarArtistList) {
            if(getActivity() instanceof ToolbarAndRefreshActivity){
                ((ToolbarAndRefreshActivity) getActivity()).setToolbarInActivity(toolbarArtistList, true, true, true);
                if(!((ToolbarAndRefreshActivity) getActivity()).mIsLargeLayout)
                    ((ToolbarAndRefreshActivity) getActivity()).setMenuSettings(R.menu.menu_activity);
                else
                    ((ToolbarAndRefreshActivity) getActivity()).setMenuSettings(null);
            }
        }
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
                int position = recView.getChildAdapterPosition(v);
                ListItem item = artistListAdapter.getItem(position);
                List<View> sharedViews = new ArrayList<>();
                sharedViews.add(v.findViewById(R.id.artist_item_image));
                sharedViews.add(v.findViewById(R.id.artist_item_name));
                sharedViews.add(toolbarArtistList);
                mCallback.onArtistListFragmentItemSelected(item, sharedViews);

            }
        });

        if(getActivity() instanceof ToolbarAndRefreshActivity) {
            ((ToolbarAndRefreshActivity) getActivity()).setRefreshLayout(refreshLayout);
            ((ToolbarAndRefreshActivity) getActivity()).disableRefreshLayoutSwipe();
        }

        return view;
    }

    /**
     * save the list of artists returned by the search into a local List
     * @param artists list of artists
     */
    private void setArtists(List<ListItem> artists){

        if(artistListAdapter != null)
            artistListAdapter.setItems(artists);
        //go to first position
        if(isVisible()) {
            if (recView != null && recView.getLayoutManager() != null)
                recView.getLayoutManager().scrollToPosition(0);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_artist_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_search:
                mListener.onSearchClick();
                return true;
            case R.id.action_delete_recent_searches:
                if(getActivity() instanceof SearchActivity)
                    ((SearchActivity)getActivity()).cleanRecentSearches();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        MenuItem searchIcon = menu.findItem(R.id.action_search);

        if(getActivity() instanceof SearchActivity){
            Boolean showIcon = ((SearchActivity) getActivity()).getShowSearchIcon();
            searchIcon.setVisible(showIcon);
        }

        super.onPrepareOptionsMenu(menu);
    }

    /**
     * Search for an artist using Spotify's wrapper
     * @param query name of the artist
     */
    public void searchArtist(String query){
        //search artist with spotify wrapper
        String parsedQuery = query.replace(" ", "+");

        //show indefiniteProgressBar
        if(getActivity() instanceof ToolbarAndRefreshActivity)
            ((ToolbarAndRefreshActivity) getActivity()).showRefreshLayoutSwipeProgress();

        spotify.searchArtists(parsedQuery, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<ListItem> artists = new ArrayList<>();
                Handler mainHandler = new Handler(getActivity().getMainLooper());
                if (artistsPager.artists.items.size() == 0) {
                    //no artists, notify it!
                    mainHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            //hide indefiniteProgressBar
                            if(getActivity() instanceof ToolbarAndRefreshActivity)
                                ((ToolbarAndRefreshActivity) getActivity()).hideRefreshLayoutSwipeProgress();
                            Utilities.showToast(getActivity(), getResources().getString(R.string.no_artist_found));
                        }
                    });
                    return;
                }
                for (int i = 0; i < artistsPager.artists.items.size(); i++) {
                    ListItem item = new ListItem();
                    item.setArtistId(artistsPager.artists.items.get(i).id);
                    item.setArtistName(artistsPager.artists.items.get(i).name);
                    for (int j = 0; j < artistsPager.artists.items.get(i).images.size(); j++) {
                        //save 200px picture (or any other if not available)
                        if (j == 0)
                            item.setArtistPicture(artistsPager.artists.items.get(i).images.get(j).url);
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
                        //hide indefiniteProgressBar
                        if(getActivity() instanceof ToolbarAndRefreshActivity)
                            ((ToolbarAndRefreshActivity) getActivity()).hideRefreshLayoutSwipeProgress();
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
                        //hide indefiniteProgressBar
                        if(getActivity() instanceof ToolbarAndRefreshActivity)
                            ((ToolbarAndRefreshActivity) getActivity()).hideRefreshLayoutSwipeProgress();
                        Utilities.showToast(getActivity(), getResources().getString(R.string.no_response));
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

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        try {
            mCallback = (ArtistListFragmentSelectionListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement SelectionListener");
        }

    }

}
