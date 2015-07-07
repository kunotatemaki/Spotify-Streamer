package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.utils.GlideCircleTransform;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ArtistViewHolder>
        implements View.OnClickListener {

    private View.OnClickListener listener;
    private List<ListItem> artists;

    public ArtistListAdapter(){
    }

    public static class ArtistViewHolder
            extends RecyclerView.ViewHolder {

        @InjectView(R.id.artist_item_name) TextView artistName;
        @InjectView(R.id.artist_item_image) ImageView artistPic;
        private final Context context;
        public ArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            context = itemView.getContext();
        }

        public void bindArtist(ListItem item) {
            artistName.setText(item.getArtistName());
            Glide.with(context)
                    .load(item.getArtistPicture())
                    .error(R.drawable.default_image)
                    .transform(new GlideCircleTransform(context))
                    .into(artistPic);
        }
    }

    public void setItems(List<ListItem> items) {
        this.artists = items;
        notifyDataSetChanged();
    }

    public ListItem getItem(Integer position){
        return artists.get(position);
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_artist, viewGroup, false);

        itemView.setOnClickListener(this);
        //android:background="?android:attr/selectableItemBackground"

        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder viewHolder, int pos) {
        ListItem item = artists.get(pos);
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