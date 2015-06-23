package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistListItem implements Parcelable{
    private String artistId;
    private String artistName;
    private String artistPicture;

    public ArtistListItem(){

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

    private ArtistListItem(Parcel in){
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

    public static final Parcelable.Creator<ArtistListItem> CREATOR = new Parcelable.Creator<ArtistListItem>() {
        public ArtistListItem createFromParcel(Parcel in) {
            return new ArtistListItem(in);
        }

        public ArtistListItem[] newArray(int size) {
            return new ArtistListItem[size];
        }
    };
}
