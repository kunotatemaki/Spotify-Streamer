package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;


/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class TrackItem{
    private String albumName;
    private String trackName;
    private String thumbnailSmall;
    private String thumbnailLarge;
    private String previewUrl;

    public TrackItem(){

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


}
