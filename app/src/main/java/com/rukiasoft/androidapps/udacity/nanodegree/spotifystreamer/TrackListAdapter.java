package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
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
public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.TrackViewHolder>
        implements View.OnClickListener {

    private View.OnClickListener listener;
    private List<ListItem> tracks;

    public List<ListItem> getTracks() {
        return tracks;
    }

    public TrackListAdapter(){
    }

    public static class TrackViewHolder
            extends RecyclerView.ViewHolder {

        @InjectView(R.id.track_item_song) TextView trackName;
        @InjectView(R.id.track_item_album) TextView albumName;
        @InjectView(R.id.track_item_image) ImageView albumPic;

        private static ColorStateList sColorStatePlaying;
        private static ColorStateList sColorStateNotPlaying;

        private final Context context;
        public TrackViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            context = itemView.getContext();
            if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
                initializeColorStateLists(context);
            }
        }

        public void bindArtist(ListItem item) {
            trackName.setText(item.getTrackName());
            albumName.setText(item.getAlbumName());
            switch (item.getFlags()) {
                case ListItem.FLAG_STOPPED:
                    Glide.with(context)
                            .load(item.getThumbnailSmall())
                            .error(R.drawable.default_image)
                            .transform(new GlideCircleTransform(context))
                            .into(albumPic);
                    break;
                case ListItem.FLAG_PLAYING:
                    AnimationDrawable animation = (AnimationDrawable)
                            context.getDrawable(R.drawable.ic_equalizer_white_36dp);
                    albumPic.setImageDrawable(animation);
                    albumPic.setImageTintList(sColorStatePlaying);
                    if (animation != null) animation.start();
                    break;
                case ListItem.FLAG_PAUSED:
                    albumPic.setImageDrawable(
                            context.getDrawable(R.drawable.ic_equalizer1_white_36dp));
                    albumPic.setImageTintList(sColorStateNotPlaying);
                    break;
                default:
                    Glide.with(context)
                            .load(item.getThumbnailSmall())
                            .error(R.drawable.default_image)
                            .transform(new GlideCircleTransform(context))
                            .into(albumPic);
                    break;
            }

        }

        static private void initializeColorStateLists(Context ctx) {
            sColorStateNotPlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                    R.color.media_item_icon_not_playing));
            sColorStatePlaying = ColorStateList.valueOf(ctx.getResources().getColor(
                    R.color.media_item_icon_playing));
        }
    }

    public void setItems(List<ListItem> items) {
        this.tracks = items;
        notifyDataSetChanged();
    }

    public ListItem getItem(Integer position){
        return tracks.get(position);
    }


    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_track, viewGroup, false);

        itemView.setOnClickListener(this);
        //android:background="?android:attr/selectableItemBackground"

        return new TrackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder viewHolder, int pos) {
        ListItem item = tracks.get(pos);

        viewHolder.bindArtist(item);
    }

    @Override
    public int getItemCount() {
        if(tracks == null)
            return 0;
        else
            return tracks.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if(listener != null)
            listener.onClick(view);
    }

    public void setItemState(int position, int flag, boolean updateView){
        if(position < tracks.size() && position >= 0){
            tracks.get(position).setmFlags(flag);
            if(updateView)
                notifyItemChanged(position);
        }
    }

}