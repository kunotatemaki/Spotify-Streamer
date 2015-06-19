package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ArtistViewHolder>
        implements View.OnClickListener {

    private View.OnClickListener listener;
    private ArrayList<ArtistItem> artists;

    public static class ArtistViewHolder
            extends RecyclerView.ViewHolder {

        private TextView artistName;
        private ImageView artistPic;
        public ArtistViewHolder(View itemView) {
            super(itemView);

            artistName = (TextView)itemView.findViewById(R.id.artist_item_name);
            artistPic = (ImageView)itemView.findViewById(R.id.artist_item_image);
        }

        public void bindArtist(ArtistItem item) {
            artistName.setText(item.getName());
            //artistPic.setText(item.getSubtitulo());
        }
    }

    public void setItems(ArrayList<ArtistItem> items) {
        this.artists = items;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.search_artist_item, viewGroup, false);

        itemView.setOnClickListener(this);
        //android:background="?android:attr/selectableItemBackground"

        ArtistViewHolder tvh = new ArtistViewHolder(itemView);

        return tvh;
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder viewHolder, int pos) {
        ArtistItem item = artists.get(pos);

        viewHolder.bindArtist(item);
    }

    @Override
    public int getItemCount() {
        if(artists == null)
            return 0;
        else
            return artists.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null)
            listener.onClick(view);
    }
}