package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.util.List;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ArtistViewHolder>
        implements View.OnClickListener {

    private View.OnClickListener listener;
    private List<ArtistItem> artists;


    public ArtistListAdapter(){
    }

    public ArtistListAdapter(List<ArtistItem> artists) {
        this.artists = artists;
    }

    public static class ArtistViewHolder
            extends RecyclerView.ViewHolder {

        private TextView artistName;
        private ImageView artistPic;
        private Context context;
        public ArtistViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            artistName = (TextView)itemView.findViewById(R.id.artist_item_name);
            artistPic = (ImageView)itemView.findViewById(R.id.artist_item_image);
        }

        public void bindArtist(ArtistItem item) {
            artistName.setText(item.getName());
            Glide.with(context)
                    .load(item.getPicture())
                    .transform(new CircleTransform(context))
                    .into(artistPic);
            //artistPic.setText(item.getSubtitulo());
        }
    }

    public void setItems(List<ArtistItem> items) {
        this.artists = items;
        notifyDataSetChanged();
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

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName();
        }
    }
}