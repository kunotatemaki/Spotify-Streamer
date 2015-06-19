package com.rukiasoft.androidapps.udacity.nanodegree.spotifystreamer.database;

import java.io.Serializable;

/**
 * Created by Raúl Feliz Alonso on 18/06/15.
 */

public class DataBaseItem implements Serializable {


    private Integer Id;
    private String savedSearch;

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(String savedSearch) {
        this.savedSearch = savedSearch;
    }
}
