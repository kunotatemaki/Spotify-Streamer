package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */
public class ArtistItem {
    private String name;
    private String path;

    public ArtistItem(String name, String path){
        name = name;
        path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
