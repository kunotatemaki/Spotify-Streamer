package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistItem implements Parcelable{
    private String artistId;
    private String artistName;
    private String artistPicture;

    public ArtistItem(){

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

    public ArtistItem(Parcel in){
        this.artistId = in.readString();
        this.artistName = in.readString();
        this.artistPicture= in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getArtistId());
        dest.writeString(getArtistName());
        dest.writeString(getArtistPicture());
    }

    public static final Parcelable.Creator<ArtistItem> CREATOR = new Parcelable.Creator<ArtistItem>() {
        public ArtistItem createFromParcel(Parcel in) {
            return new ArtistItem(in);
        }

        public ArtistItem[] newArray(int size) {
            return new ArtistItem[size];
        }
    };
}
