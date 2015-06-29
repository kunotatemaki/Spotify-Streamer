package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.media.session.MediaController;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ListItem implements Parcelable{
    private final int mFlags;
    private String artistId;
    private String artistName;
    private String artistPicture;
    private String albumName;
    private String trackName;
    private String thumbnailSmall;
    private String thumbnailLarge;
    private String previewUrl;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true, value = { FLAG_BROWSABLE, FLAG_PLAYABLE })
    public @interface Flags { }

    /**
     * Flag: Indicates that the item has children of its own.
     */
    public static final int FLAG_BROWSABLE = 1 << 0;

    /**
     * Flag: Indicates that the item is playable.
     * <p>
     * The id of this item may be passed to
     * {@link MediaController.TransportControls#playFromMediaId(String, Bundle)}
     * to start playing it.
     * </p>
     */
    public static final int FLAG_PLAYABLE = 1 << 1;



    public ListItem( @Flags int flags){
        mFlags = flags;
    }

    /**
     * Gets the flags of the item.
     */
    public @Flags int getFlags() {
        return mFlags;
    }

    /**
     * Returns whether this item is browsable.
     * @see #FLAG_BROWSABLE
     */
    public boolean isBrowsable() {
        return (mFlags & FLAG_BROWSABLE) != 0;
    }

    /**
     * Returns whether this item is playable.
     * @see #FLAG_PLAYABLE
     */
    public boolean isPlayable() {
        return (mFlags & FLAG_PLAYABLE) != 0;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistPicture() {
        return artistPicture;
    }

    public void setArtistPicture(String artistPicture) {
        this.artistPicture = artistPicture;
    }
    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getThumbnailLarge() {
        return thumbnailLarge;
    }

    public void setThumbnailLarge(String thumbnailLarge) {
        this.thumbnailLarge = thumbnailLarge;
    }

    public String getThumbnailSmall() {
        return thumbnailSmall;
    }

    public void setThumbnailSmall(String thumbnailSmall) {
        this.thumbnailSmall = thumbnailSmall;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    private ListItem(Parcel in){
        this.mFlags = in.readInt();
        this.artistId = in.readString();
        this.artistName = in.readString();
        this.artistPicture= in.readString();
        this.albumName = in.readString();
        this.previewUrl = in.readString();
        this.thumbnailLarge= in.readString();
        this.thumbnailSmall = in.readString();
        this.trackName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mFlags);
        dest.writeString(getArtistId());
        dest.writeString(getArtistName());
        dest.writeString(getArtistPicture());
        dest.writeString(getAlbumName());
        dest.writeString(getPreviewUrl());
        dest.writeString(getThumbnailLarge());
        dest.writeString(getThumbnailSmall());
        dest.writeString(getTrackName());
    }

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };
}


//http://stackoverflow.com/questions/14151661/mediaplayer-service-android

//http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778