package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistItem {
    private String id;
    private String name;
    private String picture;

    public ArtistItem(){

    }
    public ArtistItem(String name, String picture, String id){
        this.name = name;
        this.picture = picture;
        this.id = id;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
