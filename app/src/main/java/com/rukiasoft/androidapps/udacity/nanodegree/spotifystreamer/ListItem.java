package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ListItem implements Parcelable{
    private int mFlags;
    private String artistId;
    private String artistName;
    private String artistPicture;
    private String albumName;
    private String trackName;
    private String thumbnailSmall;
    private String thumbnailLarge;
    private String previewUrl;
    private long duration;

    /** @hide */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag=true, value = { FLAG_PLAYING, FLAG_PAUSED, FLAG_STOPPED })
    public @interface Flags { }

    /**
     * Flag: Indicates that the item is being played.
     */
    public static final int FLAG_PLAYING = 1 << 0;

    /**
     * Flag: Indicates that the item is paused.
     */
    public static final int FLAG_PAUSED = 1 << 1;

    /**
     * Flag: Indicates that the item is stopped.
     */
    public static final int FLAG_STOPPED = 1 << 2;



    public ListItem( ){
        mFlags = FLAG_STOPPED;
    }

    /**
     * Gets the flags of the item.
     */
    public @Flags int getFlags() {
        return mFlags;
    }

    public void setmFlags(int mFlags) {
        this.mFlags = mFlags;
    }

    /**
     * Returns whether this item is paused.
     * @see #FLAG_PAUSED
     */
    public boolean isPaused() {
        return (mFlags & FLAG_PAUSED) != 0;
    }

    /**
     * Returns whether this item is playing.
     * @see #FLAG_PLAYING
     */
    public boolean isPlaying() {
        return (mFlags & FLAG_PLAYING) != 0;
    }

    /**
     * Returns whether this item is stopped.
     * @see #FLAG_PLAYING
     */
    public boolean isStopped() {
        return (mFlags & FLAG_STOPPED) != 0;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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
        this.duration = in.readLong();
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
        dest.writeLong(getDuration());
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